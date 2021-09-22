package edu.nitt.delta

import android.content.Context
import android.net.Uri
import edu.nitt.delta.api.RetrofitInstance
import edu.nitt.delta.models.AuthorizationErrorType
import edu.nitt.delta.helpers.DAuthConstants
import edu.nitt.delta.helpers.DAuthConstants.BASE_AUTHORITY
import edu.nitt.delta.helpers.DAuthConstants.BASE_URL
import edu.nitt.delta.helpers.DAuthConstants.SCHEME
import edu.nitt.delta.helpers.isNetworkAvailable
import edu.nitt.delta.helpers.openWebViewWithUriAndCookie
import edu.nitt.delta.helpers.retrieveCookie
import edu.nitt.delta.interfaces.SelectAccountFromAccountManagerListener
import edu.nitt.delta.interfaces.SelectAccountListener
import edu.nitt.delta.interfaces.ShouldOverrideURLListener
import edu.nitt.delta.interfaces.SignInListener
import edu.nitt.delta.models.AuthorizationRequest
import edu.nitt.delta.models.AuthorizationResponse
import edu.nitt.delta.models.GrantType
import edu.nitt.delta.models.ResponseType
import edu.nitt.delta.models.Scope
import edu.nitt.delta.models.Token
import edu.nitt.delta.models.TokenRequest
import edu.nitt.delta.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DAuth {

    private var currentUser: User? = null

    fun signIn(context: Context, signInListener: SignInListener) = requestAuthorization(
        context,
        AuthorizationRequest(
            "xobh.KPYVvLXhGum",
            "https://www.google.com",
            ResponseType.Code,
            GrantType.AuthorizationCode,
            "1ww12",
            listOf(Scope.OpenID),
            "ncsasd"
        ),
        onFailure = { errorState -> signInListener.onFailure(Exception(errorState.toString())) }
    ) { authResponse ->
        fetchToken(
            TokenRequest(
                client_id = "xobh.KPYVvLXhGum",
                client_secret = "https://www.google.com",
                grant_type = GrantType.AuthorizationCode.toString(),
                code = authResponse.authorizationCode,
                redirect_uri = "https://www.google.com"
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
        authorizationRequest: AuthorizationRequest,
        onFailure: (AuthorizationErrorType) -> Unit,
        onSuccess: (AuthorizationResponse) -> Unit
    ) = if (isNetworkAvailable(context)) {
        selectAccount(context, object : SelectAccountListener {
            override fun onSuccess(cookie: String) {
                val uri: Uri = Uri.Builder()
                    .scheme(SCHEME)
                    .authority(BASE_AUTHORITY)
                    .appendPath("authorize")
                    .appendQueryParameter("client_id", authorizationRequest.client_id)
                    .appendQueryParameter("redirect_uri", authorizationRequest.redirect_uri)
                    .appendQueryParameter(
                        "response_type",
                        authorizationRequest.response_type.toString()
                    )
                    .appendQueryParameter(
                        "grant_type",
                        authorizationRequest.grant_type.toString()
                    )
                    .appendQueryParameter("state", authorizationRequest.state)
                    .appendQueryParameter(
                        "scopes",
                        Scope.combineScopes(authorizationRequest.scopes)
                    )
                    .appendQueryParameter("nonce", authorizationRequest.nonce)
                    .build()
                val alertDialog = openWebViewWithUriAndCookie(
                    context,
                    uri,
                    object : ShouldOverrideURLListener {
                        override fun shouldLoadUrl(url: String): Boolean {
                            val uri: Uri = Uri.parse(url)
                            if (url.startsWith(authorizationRequest.redirect_uri)) {
                                if (uri.query.isNullOrBlank() or uri.query.isNullOrEmpty()) {
                                    onFailure(AuthorizationErrorType.AuthorizationDenied)
                                } else {
                                    val authorizationResponse = AuthorizationResponse(
                                        uri.getQueryParameter("code") ?: "",
                                        uri.getQueryParameter("state") ?: ""
                                    )
                                    onSuccess(authorizationResponse)
                                }
                                return false
                            }
                            if (!(uri.scheme + "://" + uri.encodedAuthority).contentEquals(
                                    BASE_URL
                                )
                            ) {
                                onFailure(AuthorizationErrorType.InternalError)
                                return false
                            }
                            if (uri.path == "/dashboard") {
                                onFailure(AuthorizationErrorType.InternalError)
                                return false
                            }
                            return true
                        }
                    },
                    cookie
                )
                alertDialog.setOnDismissListener {
                    onFailure(AuthorizationErrorType.UserDismissed)
                }
            }

            override fun onFailure() {
                onFailure(AuthorizationErrorType.InternalError)
            }

            override fun onUserDismiss() {
                onFailure(AuthorizationErrorType.UserDismissed)
            }
        })
    } else onFailure(AuthorizationErrorType.NetworkError)

    //to request token use tokenRequest members as query parameters
    private fun fetchToken(
        request: TokenRequest,
        onFailure: (Exception) -> Unit,
        onSuccess: (Token) -> Unit
    ) {
        RetrofitInstance.api.getToken(
            client_id = request.client_id,
            client_secret = request.client_secret,
            grant_type = request.grant_type,
            code = request.code,
            redirect_uri = request.redirect_uri
        ).enqueue(object : Callback<Token> {
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

    private fun fetchUserDetails(
        accessToken: String,
        onFailure: (Exception) -> Unit,
        onSuccess: (User) -> Unit
    ) {
        RetrofitInstance.api.getUser(accessToken).enqueue(object : Callback<User> {
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

    private fun selectAccount(context: Context, selectAccountListener: SelectAccountListener) {
        selectAccountFromAccountManager(context, object : SelectAccountFromAccountManagerListener {
            override fun onSelect(cookie: String) {
                selectAccountListener.onSuccess(cookie)
            }

            override fun onCreateNewAccount() {
                val uri: Uri = Uri.Builder()
                    .scheme(SCHEME)
                    .authority(BASE_AUTHORITY)
                    .build()

                val alertDialog = openWebViewWithUriAndCookie(
                    context,
                    uri,
                    object : ShouldOverrideURLListener {
                        override fun shouldLoadUrl(url: String): Boolean {
                            val uri: Uri = Uri.parse(url)
                            if (!(uri.scheme + "://" + uri.encodedAuthority).contentEquals(
                                    DAuthConstants.BASE_URL
                                )
                            ) {
                                selectAccountListener.onFailure()
                                return false
                            }
                            if (uri.path.contentEquals("/dashboard")) {
                                selectAccountListener.onSuccess(retrieveCookie(uri.scheme + "://" + uri.encodedAuthority))
                                return false
                            }
                            return true
                        }
                    })
                alertDialog.setOnDismissListener {
                    selectAccountListener.onUserDismiss()
                }
            }

            override fun onUserDismiss() {
                selectAccountListener.onUserDismiss()
            }
        })
    }

    private fun selectAccountFromAccountManager(
        context: Context,
        selectAccountFromAccountManagerListener: SelectAccountFromAccountManagerListener
    ) {
        selectAccountFromAccountManagerListener.onCreateNewAccount()
        //TODO("Account Selection UI for testing uncomment the previous")
    }

    fun registerWithClient() {
        TODO("To be implemented")
    }

    // adds user in accountManager
    fun addUser() {
        TODO("To be implemented")
    }
}
