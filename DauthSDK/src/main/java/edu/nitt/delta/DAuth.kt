package edu.nitt.delta

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import edu.nitt.delta.api.RetrofitInstance
import edu.nitt.delta.helpers.DAuthConstants
import edu.nitt.delta.helpers.DAuthConstants.BASE_AUTHORITY
import edu.nitt.delta.helpers.DAuthConstants.BASE_URL
import edu.nitt.delta.helpers.DAuthConstants.SCHEME
import edu.nitt.delta.helpers.isNetworkAvailable
import edu.nitt.delta.helpers.openWebView
import edu.nitt.delta.helpers.toMap
import edu.nitt.delta.interfaces.SignInListener
import edu.nitt.delta.models.AuthorizationErrorType
import edu.nitt.delta.models.AuthorizationRequest
import edu.nitt.delta.models.AuthorizationResponse
import edu.nitt.delta.models.ClientCredentials
import edu.nitt.delta.models.Scope
import edu.nitt.delta.models.Token
import edu.nitt.delta.models.TokenRequest
import edu.nitt.delta.models.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


object DAuth {

    private var currentUser: User? = null
    private val clientCreds: ClientCredentials = ClientCredentials(
        BuildConfig.CLIENT_ID,
        BuildConfig.REDIRECT_URI,
        BuildConfig.CLIENT_SECRET
    )

    fun signIn(
        context: Context,
        authRequest: AuthorizationRequest,
        signInListener: SignInListener
    ) = requestAuthorization(
        context,
        authRequest,
        onFailure = { errorState -> signInListener.onFailure(Exception(errorState.toString())) }
    ) { authResponse ->
        fetchToken(
            TokenRequest(
                client_id = clientCreds.clientId,
                client_secret = clientCreds.clientSecret,
                grant_type = authRequest.grant_type.toString(),
                code = authResponse.authorizationCode,
                redirect_uri = clientCreds.redirectUri
            ),
            onFailure = { e -> signInListener.onFailure(e) }
        ) { token ->
            fetchUserDetails(
                token.access_token,
                onFailure = { e -> signInListener.onFailure(e) }
            ) { user ->
                currentUser = user
                signInListener.onSuccess(user)
                // TODO: 9/22/2021 Store User object locally
            }
        }
    }

    // to request for authorization use authorizationRequest members as query parameters
    private fun requestAuthorization(
        context: Context,
        authRequest: AuthorizationRequest,
        onFailure: (AuthorizationErrorType) -> Unit,
        onSuccess: (AuthorizationResponse) -> Unit
    ) = if (isNetworkAvailable(context)) selectAccount(
        context,
        onFailure = { onFailure(AuthorizationErrorType.InternalError) },
        onUserDismiss = { onFailure(AuthorizationErrorType.UserDismissed) }
    ) { cookie ->
        val uri: Uri = Uri.Builder()
            .scheme(SCHEME)
            .authority(BASE_AUTHORITY)
            .appendPath("authorize")
            .appendQueryParameter("client_id", clientCreds.clientId)
            .appendQueryParameter("redirect_uri", clientCreds.redirectUri)
            .appendQueryParameter("response_type", authRequest.response_type.toString())
            .appendQueryParameter("grant_type", authRequest.grant_type.toString())
            .appendQueryParameter("state", authRequest.state)
            .appendQueryParameter("scope", Scope.combineScopes(authRequest.scopes))
            .appendQueryParameter("nonce", authRequest.nonce)
            .build()
        val alertDialog = openWebView(context, uri, cookie) { url ->
            val uri: Uri = Uri.parse(url)
            if (url.startsWith(clientCreds.redirectUri)) {
                if (uri.query.isNullOrBlank() or uri.query.isNullOrEmpty()) {
                    onFailure(AuthorizationErrorType.AuthorizationDenied)
                } else {
                    val authorizationResponse = AuthorizationResponse(
                        uri.getQueryParameter("code") ?: "",
                        uri.getQueryParameter("state") ?: ""
                    )
                    onSuccess(authorizationResponse)
                }
                return@openWebView false
            }
            if (!(uri.scheme + "://" + uri.encodedAuthority).contentEquals(BASE_URL)) {
                onFailure(AuthorizationErrorType.InternalError)
                return@openWebView false
            }
            if (uri.path == "/dashboard") {
                onFailure(AuthorizationErrorType.InternalError)
                return@openWebView false
            }
            return@openWebView true
        }
        alertDialog.setOnDismissListener {
            onFailure(AuthorizationErrorType.UserDismissed)
        }
    } else onFailure(AuthorizationErrorType.NetworkError)

    //to request token use tokenRequest members as query parameters
    private fun fetchToken(
        request: TokenRequest,
        onFailure: (Exception) -> Unit,
        onSuccess: (Token) -> Unit
    ) = RetrofitInstance.api.getToken(request.toMap()).enqueue(object : Callback<Token> {
        override fun onResponse(call: Call<Token>, response: Response<Token>) {
            if (!response.isSuccessful) {
                onFailure(Exception(response.code().toString()))
                return
            }

            response.body()?.let { onSuccess(it) }
        }

        override fun onFailure(call: Call<Token>, t: Throwable) {
            onFailure(Exception(t.message))
        }
    })

    private fun fetchUserDetails(
        accessToken: String,
        onFailure: (Exception) -> Unit,
        onSuccess: (User) -> Unit
    ) = RetrofitInstance.api.getUser("Bearer $accessToken").enqueue(object : Callback<User> {
        override fun onResponse(call: Call<User>, response: Response<User>) {
            if (!response.isSuccessful) {
                onFailure(Exception(response.code().toString()))
                return
            }
            response.body()?.let { onSuccess(it) }
        }

        override fun onFailure(call: Call<User>, t: Throwable) {
            onFailure(Exception(t.message))
        }
    })

    fun getCurrentUser(): User? = currentUser

    private fun selectAccount(
        context: Context,
        onFailure: () -> Unit,
        onUserDismiss: () -> Unit,
        onSuccess: (cookie: String) -> Unit
    ) = selectAccountFromAccountManager(
        context,
        onCreateNewAccount = {
            val accountManager = AccountManager.get(context)
            accountManager.addAccount(
                DAuthConstants.ACCOUNT_TYPE,
                null,
                null,
                null,
                context as Activity?,
                { Result ->
                    onSuccess(
                        accountManager.getUserData(
                            Account(
                                Result.result.getString(AccountManager.KEY_ACCOUNT_NAME),
                                DAuthConstants.ACCOUNT_TYPE
                            ), AccountManager.KEY_AUTHTOKEN
                        )
                    )
                },
                null
            )
        },
        onUserDismiss = onUserDismiss,
        onSelect = onSuccess,
        onFailure = onFailure
    )

    private fun selectAccountFromAccountManager(
        context: Context,
        onCreateNewAccount: () -> Unit,
        onUserDismiss: () -> Unit,
        onSelect: (cookie: String) -> Unit,
        onFailure: () -> Unit
    ) {
        try {
            val accountManager = AccountManager.get(context)
            val items = accountManager.getAccountsByType(DAuthConstants.ACCOUNT_TYPE)
            if (items.isNotEmpty()) {
                val accountNames: Array<String> = Array(items.size) { "null" }
                val alertBuilder = AlertDialog.Builder(context)
                alertBuilder.setTitle("Select an account")
                for (i in items.indices) {
                    accountNames[i] = items[i].name
                }
                alertBuilder.setItems(accountNames) { _, whichButton ->
                    val account = Account(accountNames[whichButton], DAuthConstants.ACCOUNT_TYPE)
                    val duedate =
                        accountManager.getUserData(
                            account,
                            AccountManager.KEY_LAST_AUTHENTICATED_TIME
                        )
                    val formatter = SimpleDateFormat("dd/MM/yyyy");
                    val currentdate = Date();
                    if (currentdate.compareTo(formatter.parse(duedate))<0)

                    {
                        onSelect(
                            accountManager.getUserData(
                                account,
                                AccountManager.KEY_AUTHTOKEN
                            )
                        )
                    } else {
                        RetrofitInstance.api.getCookie(
                            account.name,
                            accountManager.getPassword(account)
                        ).enqueue(object : Callback<ResponseBody> {
                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {
                                if (response.isSuccessful) {
                                    var cookie=""
                                    for(i in response.headers().get("Set-Cookie").toString()){
                                        if(i==';')
                                            break
                                        else cookie+=i
                                    }
                                    var df = SimpleDateFormat("dd/MM/yyyy")
                                    val c1 = Calendar.getInstance()
                                    c1.add(Calendar.DAY_OF_YEAR, 30)
                                    df = SimpleDateFormat("dd/MM/yyyy")
                                    val resultDate = c1.time
                                    val dueDate: String = df.format(resultDate)
                                    accountManager.setUserData(
                                        account, AccountManager.KEY_LAST_AUTHENTICATED_TIME,
                                    dueDate)
                                    accountManager.setUserData(
                                        account, AccountManager.KEY_AUTHTOKEN,
                                        cookie
                                    )

                                    onSelect(
                                        accountManager.getUserData(
                                            account,
                                            AccountManager.KEY_AUTHTOKEN
                                        )
                                    )
                                } else {
                                    onUserDismiss()
                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                onFailure()
                            }
                        }
                        )
                    }

                }
                alertBuilder.setPositiveButton(
                    "Create new account"
                ) { _, _ ->
                    onCreateNewAccount()
                }
                alertBuilder.create().show()
            } else {
                onCreateNewAccount()
            }
        } catch (e: Exception) {
            onCreateNewAccount()
        }
    }
}
