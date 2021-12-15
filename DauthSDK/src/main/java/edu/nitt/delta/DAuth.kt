package edu.nitt.delta

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.app.AlertDialog
import android.net.Uri
import android.util.Base64
import com.google.gson.GsonBuilder
import edu.nitt.delta.api.RetrofitInstance
import edu.nitt.delta.constants.AccountManagerConstants
import edu.nitt.delta.constants.ErrorMessageConstants
import edu.nitt.delta.constants.WebViewConstants.BaseAuthority
import edu.nitt.delta.constants.WebViewConstants.BaseUrl
import edu.nitt.delta.constants.WebViewConstants.Scheme
import edu.nitt.delta.helpers.PkceUtil
import edu.nitt.delta.helpers.isNetworkAvailable
import edu.nitt.delta.helpers.openWebView
import edu.nitt.delta.helpers.toMap
import edu.nitt.delta.interfaces.ResultListener
import edu.nitt.delta.models.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/**
 * Main DAuth object whose instance is to be created to make use of DAuth sign in functionalities
 */
object DAuth {

    /**
     * currentUser [User] that stores the details of current logged user
     * clientCreds [ClientCredentials] storing the credentials obtained after client registration in auth.delta.nitt.edu
     */
    private var currentUser: User? = null
    private var codeVerifier:String? = null
    private val clientCreds: ClientCredentials = ClientCredentials(
        BuildConfig.CLIENT_ID,
        BuildConfig.REDIRECT_URI,
        BuildConfig.CLIENT_SECRET
    )

    /**
     * Wrapper function for sign-in functionality for java consumer
     *
     * @param activity Activity
     * @param authorizationRequest AuthorizationRequest
     * @param signInListener ResultListener<Result>
     */
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

    /**
     * Signs the user in
     *
     * @param activity Activity
     * @param authorizationRequest AuthorizationRequest
     * @param onSuccess Lambda Function that is called on successfull login taking Result as member and returns unit
     * @param onFailure Lambda Function that is called on failure taking Exception as member and returns unit
     */
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
                        authorizationRequest,
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
                                    if(authorizationRequest.scopes.contains(Scope.OpenID)){
                                        fetchFromJwt(
                                            authorizationRequest,
                                            token.id_token,
                                            onFailure = { e -> onFailure(e) }
                                        ){jwt ->
                                            onSuccess(Result(user,jwt,token.id_token))
                                        }
                                    } else {
                                        onSuccess(Result(user, null, null))
                                    }
                                }
                            } else {
                                if (authorizationRequest.scopes.contains(Scope.OpenID)) {
                                    fetchFromJwt(
                                        authorizationRequest,
                                        token.id_token,
                                        onFailure = { e -> onFailure(e) }
                                    ) { jwt ->
                                        onSuccess(Result(null,jwt,token.id_token))
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

    /**
     * Wrapper function to request authorization for java consumers
     *
     * @param activity Activity
     * @param authorizationRequest AuthorizationRequest
     * @param authorizationListener ResultListener<AuthorizationResponse>
     */
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

    /**
     * Requests authorization for the user to log in
     *
     * @param activity Activity
     * @param authorizationRequest AuthorizationRequest
     * @param onFailure Lambda function called on failure taking AuthorizationErrorType as member and returns unit
     * @param onSuccess Lambda function called on successful authorization taking AuthorizationResponse as member and returns unit
     */
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
                val pkceUtil =PkceUtil()
                val uriBuilder = Uri.Builder()
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
                if(authorizationRequest.isPkceEnabled){
                    try {
                        codeVerifier = pkceUtil.generateCodeVerifier()
                        uriBuilder.appendQueryParameter("code_challenge",pkceUtil.generateCodeChallenge(
                            codeVerifier!!,pkceUtil.getCodeChallengeMethod()))
                        uriBuilder.appendQueryParameter("code_challenge_method",pkceUtil.getCodeChallengeMethod())
                    }catch (e: Exception){
                        onFailure(AuthorizationErrorType.UnableToGenerateCodeVerifier)
                    }

                }
                val alertDialog = openWebView(
                    activity,
                    uriBuilder.build(),
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

    /**
     * Wrapper function to fetch the auth token for java consumers
     *
     * @param authorizationRequest AuthorizationRequest
     * @param request TokenRequest
     * @param fetchTokenListener ResultListener<Token>
     */
    fun fetchToken(
        authorizationRequest: AuthorizationRequest,
        request: TokenRequest,
        fetchTokenListener: ResultListener<Token>
    ) {
        fetchToken(
            authorizationRequest,
            request,
            onFailure = { exception -> fetchTokenListener.onFailure(exception) },
            onSuccess = { token -> fetchTokenListener.onSuccess(token) }
        )
    }

    /**
     * Fetches the auth token
     *
     * @param authorizationRequest AuthorizationRequest
     * @param request TokenRequest
     * @param onFailure Lambda function called on failure taking [Exception] as member and returns unit
     * @param onSuccess Lambda function called after fetching token successfully taking [Token] as member and returns unit
     */
    fun fetchToken(
        authorizationRequest: AuthorizationRequest,
        request: TokenRequest,
        onFailure: (Exception) -> Unit,
        onSuccess: (Token) -> Unit
    ) {
        var requestAsMap :Map<String,String> = request.toMap()
        if(authorizationRequest.isPkceEnabled) {
            requestAsMap = requestAsMap.plus(Pair("code_verifier", codeVerifier!!))
            requestAsMap = requestAsMap.minus("client_secret")
        }
        RetrofitInstance.api.getToken(requestAsMap).enqueue(object : Callback<Token> {
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

    /**
     * Wrapper function for fetching details of the user for java consumers
     *
     * @param accessToken string id for a user obtained from the auth token
     * @param fetchUserDetailsListener ResultListener<User>
     */
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

    /**
     * Fetches the details of the user
     *
     * @param accessToken string id for a user obtained from the auth Token
     * @param onFailure Lambda function called on failure taking [Exception] as member and returns unit
     * @param onSuccess Lambda function called after fetching token successfully taking [User] as member and returns unit
     */
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

    /**
     * Returns the current logged in user
     *
     * @return [User]
     */
    fun getCurrentUser(): User? = currentUser

    /**
     * Wrapper function for selecting account
     *
     * @param activity Activity
     * @param onFailure Lambda function called after failure
     * @param onUserDismiss Lambda function called if user dismissed the process
     * @param onSuccess Lambda function called after successfully selectiong account taking cookie[String] as member and returns unit
     */
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

    /**
     * Selects account from Account Manager in android settings
     *
     * @param activity Activity
     * @param onCreateNewAccount Lambda function to create a new account
     * @param onUserDismiss Lambda function called when user dismisses the process
     * @param onSelect Lambda function called on selecting an account taking cookie[String] as member and returns unit
     * @param onFailure Lambda function called after failure
     */
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

    /**
     * Wrapper function to fetch jwks from jwt for java consumers
     *
     * @param authorizationRequest AuthorizationRequest
     * @param idToken id token that is decoded as jwt after verification
     * @param fetchJwtListener ResultListener<jwt>
     */
    fun fetchFromJwt(
        authorizationRequest: AuthorizationRequest,
        idToken: String,
        fetchJwtListener: ResultListener<jwt>
    ){
        fetchFromJwt(
            authorizationRequest,
            idToken,
            onFailure = { exception -> fetchJwtListener.onFailure(exception) },
            onSuccess = { jwt -> fetchJwtListener.onSuccess(jwt) }
        )
    }


    /**
     * Fetches jwt after verifying with jwks
     *
     * @param authorizationRequest AuthorizationRequest
     * @param idToken id token that is decoded as jwt after verification
     * @param onFailure Lambda function called on failure taking [Exception] as member and returns unit
     * @param onSuccess Lambda function called after successfully fetching jwt taking [jwt] as member and returns unit
     */
    fun fetchFromJwt(
        authorizationRequest: AuthorizationRequest,
        idToken: String,
        onFailure: (Exception) -> Unit,
        onSuccess: (jwt) -> Unit
    ) {
        RetrofitInstance.api.getJwks().enqueue(object : Callback<jwks> {
            override fun onResponse(call: Call<jwks>, response: Response<jwks>) {
                if (!response.isSuccessful) {
                    onFailure(Exception(response.code().toString()))
                    return
                }
                response.body()?.let {
                    verifyOpenIdToken(
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

    /**
     * verifies the id token with JWKS and decodes the id token as jwt
     *
     * @param authorizationRequest Authorization
     * @param jwks JSON web key set used to verify id token
     * @param idToken id token that is decoded as jwt after verification
     * @param onSuccess lambda function called after successful decoding of jwt from id token taking [jwt] as a parameter and returns unit
     * @param onFailure Lambda function called on failure taking [Exception] as member and returns unit
     */
    private fun verifyOpenIdToken(
        authorizationRequest: AuthorizationRequest,
        jwks: jwks,
        idToken: String,
        onSuccess: (jwt) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val split: List<String> = idToken.split(".")
            val gson = GsonBuilder().create()
            val headers = gson.fromJson(String(Base64.decode(split[0], Base64.URL_SAFE)),Header::class.java)
            val data = gson.fromJson(String(Base64.decode(split[1], Base64.URL_SAFE)),Data::class.java)
            if (!(headers.kid
                    .equals(jwks.key[0].kid)) || !(authorizationRequest.nonce.equals(data.nonce))
            ) {
                onFailure(Exception(ErrorMessageConstants.InvalidIdToken))
                return
            }
            onSuccess(jwt(headers, data))
        } catch (e: Exception) {
            onFailure(e)
        }
    }
}
