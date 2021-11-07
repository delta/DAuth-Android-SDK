package edu.nitt.delta

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.app.AlertDialog
import android.net.Uri
import android.util.Base64
import edu.nitt.delta.api.RetrofitInstance
import edu.nitt.delta.constants.AccountManagerConstants
import edu.nitt.delta.constants.ErrorMessageConstants
import edu.nitt.delta.constants.WebViewConstants.BaseAuthority
import edu.nitt.delta.constants.WebViewConstants.BaseUrl
import edu.nitt.delta.constants.WebViewConstants.Scheme
import edu.nitt.delta.helpers.isNetworkAvailable
import edu.nitt.delta.helpers.openWebView
import edu.nitt.delta.helpers.toMap
import edu.nitt.delta.interfaces.ResultListener
import edu.nitt.delta.models.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

object DAuth {

    private var currentUser: User? = null
    private val clientCreds: ClientCredentials = ClientCredentials(
        BuildConfig.CLIENT_ID,
        BuildConfig.REDIRECT_URI,
        BuildConfig.CLIENT_SECRET
    )

    fun signIn(
        activity: Activity,
        authorizationRequest: AuthorizationRequest,
        signInListener: ResultListener<Result>
    ) {
        signIn(
            activity,
            authorizationRequest,
            onSuccess = { result -> signInListener.onSuccess(result) },
            onFailure = { exception -> signInListener.onFailure(exception) }
        )
    }

    fun signIn(
        activity: Activity,
        authorizationRequest: AuthorizationRequest,
        onSuccess: (Result) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        requestAuthorization(
            activity,
            authorizationRequest,
            onFailure = { errorState -> onFailure(Exception(errorState.toString())) },
            onSuccess = { authorizationResponse ->
                if (authorizationResponse.state == authorizationRequest.state) {
                    fetchToken(
                        TokenRequest(
                            client_id = clientCreds.clientId,
                            client_secret = clientCreds.clientSecret,
                            grant_type = authorizationRequest.grant_type.toString(),
                            code = authorizationResponse.authorizationCode,
                            redirect_uri = clientCreds.redirectUri
                        ),
                        onFailure = { e -> onFailure(e) },
                        onSuccess = { token ->
                            if (authorizationRequest.scopes.contains(Scope.User)) {
                                fetchUserDetails(
                                    token.access_token,
                                    onFailure = { e -> onFailure(e) }
                                ) { user ->
                                    currentUser = user
                                    onSuccess(Result(user,authorizationRequest.scopes))
                                }
                            } else {
                                if (token.id_token != null) {
                                    fetchFromJwt(
                                        authorizationRequest,
                                        token.id_token,
                                        onFailure = { e -> onFailure(e) }
                                    ) { user ->
                                        currentUser = user
                                        onSuccess(Result(user,authorizationRequest.scopes))
                                    }
                                } else {
                                    onFailure(Exception(ErrorMessageConstants.OpenIdScopeMissing))
                                }
                            }
                        }
                    )
                } else {
                    onFailure(Exception(AuthorizationErrorType.AuthorizationDenied.toString()))
                }
            }
        )
    }

    fun requestAuthorization(
        activity: Activity,
        authorizationRequest: AuthorizationRequest,
        authorizationListener: ResultListener<AuthorizationResponse>
    ) {
        requestAuthorization(
            activity,
            authorizationRequest,
            onFailure = { authorizationErrorType -> authorizationListener.onFailure(Exception("$authorizationErrorType")) },
            onSuccess = { authorizationResponse ->
                authorizationListener.onSuccess(
                    authorizationResponse
                )
            }
        )
    }

    // to request for authorization use authorizationRequest members as query parameters
    fun requestAuthorization(
        activity: Activity,
        authorizationRequest: AuthorizationRequest,
        onFailure: (AuthorizationErrorType) -> Unit,
        onSuccess: (AuthorizationResponse) -> Unit
    ) {
        if (!isNetworkAvailable(activity)) {
            onFailure(AuthorizationErrorType.NetworkError)
        }
        selectAccount(
            activity,
            onFailure = { onFailure(AuthorizationErrorType.InternalError) },
            onUserDismiss = { onFailure(AuthorizationErrorType.UserDismissed) },
            onSuccess = { cookie ->
                val uri: Uri = Uri.Builder()
                    .scheme(Scheme)
                    .authority(BaseAuthority)
                    .appendPath("authorize")
                    .appendQueryParameter("client_id", clientCreds.clientId)
                    .appendQueryParameter("redirect_uri", clientCreds.redirectUri)
                    .appendQueryParameter(
                        "response_type",
                        authorizationRequest.response_type.toString()
                    )
                    .appendQueryParameter("grant_type", authorizationRequest.grant_type.toString())
                    .appendQueryParameter("state", authorizationRequest.state)
                    .appendQueryParameter("scope", Scope.combineScopes(authorizationRequest.scopes))
                    .appendQueryParameter("nonce", authorizationRequest.nonce)
                    .build()
                val alertDialog = openWebView(
                    activity,
                    uri,
                    cookie,
                    onFailure = { onFailure(AuthorizationErrorType.ServerDownError) }
                ) { url ->
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
                    if (!(uri.scheme + "://" + uri.encodedAuthority).contentEquals(BaseUrl)) {
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
            }
        )
    }

    fun fetchToken(
        request: TokenRequest,
        fetchTokenListener: ResultListener<Token>
    ) {
        fetchToken(
            request,
            onFailure = { exception -> fetchTokenListener.onFailure(exception) },
            onSuccess = { token -> fetchTokenListener.onSuccess(token) }
        )
    }

    //to request token use tokenRequest members as query parameters
    fun fetchToken(
        request: TokenRequest,
        onFailure: (Exception) -> Unit,
        onSuccess: (Token) -> Unit
    ) {
        RetrofitInstance.api.getToken(request.toMap()).enqueue(object : Callback<Token> {
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
    }

    fun fetchUserDetails(
        accessToken: String,
        fetchUserDetailsListener: ResultListener<User>
    ) {
        fetchUserDetails(
            accessToken,
            onFailure = { exception -> fetchUserDetailsListener.onFailure(exception) },
            onSuccess = { user -> fetchUserDetailsListener.onSuccess(user) }
        )
    }

    fun fetchUserDetails(
        accessToken: String,
        onFailure: (Exception) -> Unit,
        onSuccess: (User) -> Unit
    ) {
        RetrofitInstance.api.getUser("Bearer $accessToken").enqueue(object : Callback<User> {
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
    }

    fun getCurrentUser(): User? = currentUser

    private fun selectAccount(
        activity: Activity,
        onFailure: () -> Unit,
        onUserDismiss: () -> Unit,
        onSuccess: (cookie: String) -> Unit
    ) {
        selectAccountFromAccountManager(
            activity,
            onCreateNewAccount = {
                val accountManager = AccountManager.get(activity)
                accountManager.addAccount(
                    AccountManagerConstants.AccountType,
                    null,
                    null,
                    null,
                    activity,
                    { Result ->
                        try {
                            val account = Account(
                                Result!!.result.getString(AccountManager.KEY_ACCOUNT_NAME)!!,
                                AccountManagerConstants.AccountType
                            )
                            accountManager.getAuthToken(
                                account, AccountManager.KEY_AUTHTOKEN, null, activity,
                                { Result ->
                                    try {
                                        if (Result.result != null) {
                                            val authToken =
                                                Result!!.result.getString(AccountManager.KEY_AUTHTOKEN)!!
                                            accountManager.invalidateAuthToken(
                                                AccountManagerConstants.AccountType,
                                                authToken
                                            )
                                            onSuccess(authToken)
                                        }
                                    } catch (e: Exception) {
                                        if (e.message.equals(ErrorMessageConstants.UserDisMiss))
                                            onUserDismiss()
                                        else
                                            onFailure()
                                    }
                                }, null
                            )
                        } catch (e: Exception) {
                            onFailure()
                        }
                    },
                    null
                )
            },
            onUserDismiss = onUserDismiss,
            onSelect = onSuccess,
            onFailure = onFailure
        )
    }

    private fun selectAccountFromAccountManager(
        activity: Activity,
        onCreateNewAccount: () -> Unit,
        onUserDismiss: () -> Unit,
        onSelect: (cookie: String) -> Unit,
        onFailure: () -> Unit
    ) {
        val accountManager = AccountManager.get(activity)
        val items = accountManager.getAccountsByType(AccountManagerConstants.AccountType)
        if (items.isEmpty()) {
            onCreateNewAccount()
            return
        }
        val accountNames: Array<String> = Array(items.size) { "null" }
        val alertBuilder = AlertDialog.Builder(activity)
        alertBuilder.setTitle("Select an account")
        for (i in items.indices) {
            accountNames[i] = items[i].name
        }
        alertBuilder.setItems(accountNames) { _, index ->
            val account = Account(accountNames[index], AccountManagerConstants.AccountType)
            accountManager.getAuthToken(
                account, AccountManager.KEY_AUTHTOKEN, null, activity,
                { Result ->
                    try {
                        if (Result.result != null) {
                            val authToken =
                                Result!!.result.getString(AccountManager.KEY_AUTHTOKEN)!!
                            accountManager.invalidateAuthToken(
                                AccountManagerConstants.AccountType,
                                authToken
                            )
                            onSelect(authToken)
                        }

                    } catch (e: Exception) {
                        if (e.message.equals(ErrorMessageConstants.InvalidCredentials))
                            onCreateNewAccount()
                        else
                            onFailure()
                    }
                }, null
            )
        }
        alertBuilder.setPositiveButton("Create new account") { _, _ ->
            onCreateNewAccount()
        }
        alertBuilder.setOnCancelListener { onUserDismiss() }
        alertBuilder.create().show()
    }

    private fun fetchFromJwt(
        authorizationRequest: AuthorizationRequest,
        idToken: String,
        onFailure: (Exception) -> Unit,
        onSuccess: (User) -> Unit
    ) {
        RetrofitInstance.api.getJwks().enqueue(object : Callback<jwks> {
            override fun onResponse(call: Call<jwks>, response: Response<jwks>) {
                if (!response.isSuccessful) {
                    onFailure(Exception(response.code().toString()))
                    return
                }
                response.body()?.let {
                    validateIdToken(
                        authorizationRequest,
                        it,
                        idToken,
                        onSuccess = onSuccess,
                        onFailure = onFailure
                    )
                }

            }

            override fun onFailure(call: Call<jwks>, t: Throwable) {
                onFailure(Exception(t.message))
            }
        })

    }

    private fun validateIdToken(
        authorizationRequest: AuthorizationRequest,
        jwks: jwks,
        idToken: String,
        onSuccess: (User) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val split: List<String> = idToken.split(".")
            val headers = JSONObject(String(Base64.decode(split[0], Base64.URL_SAFE)))
            val data = JSONObject(String(Base64.decode(split[1], Base64.URL_SAFE)))
            if (!headers.getString("kid")
                    .equals(jwks.key[0].kid) || !authorizationRequest.nonce.equals(data.getString("nonce"))
            ) {
                onFailure(Exception(ErrorMessageConstants.InvalidIdToken))
                return
            }
            val user = User(
                null,
                if (data.has("email")) data.get("email").toString() else null,
                if (data.has("name")) data.get("name").toString() else null,
                null
            )
            currentUser = user
            onSuccess(user)
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}
