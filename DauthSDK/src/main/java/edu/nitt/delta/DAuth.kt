package edu.nitt.delta

import android.content.Context
import android.net.Uri
import edu.nitt.delta.api.RetrofitInstance
import edu.nitt.delta.helpers.DAuthConstants.BASE_AUTHORITY
import edu.nitt.delta.helpers.DAuthConstants.BASE_URL
import edu.nitt.delta.helpers.DAuthConstants.SCHEME
import edu.nitt.delta.helpers.isNetworkAvailable
import edu.nitt.delta.helpers.openWebView
import edu.nitt.delta.helpers.retrieveCookie
import edu.nitt.delta.helpers.toMap
import edu.nitt.delta.interfaces.SignInListener
import edu.nitt.delta.models.AuthorizationErrorType
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
            "PwLV_Z_GGJSZECg1",
            "https://github.com/",
            ResponseType.Code,
            GrantType.AuthorizationCode,
            "1ww12",
            listOf(Scope.OpenID, Scope.User, Scope.Email, Scope.Profile),
            "ncsasd"
        ),
        onFailure = { errorState -> signInListener.onFailure(Exception(errorState.toString())) }
    ) { authResponse ->
        fetchToken(
            TokenRequest(
                client_id = "PwLV_Z_GGJSZECg1",
                client_secret = "cRaxHJSugJeo8YJOO5GisYgDv6x53Uxs",
                grant_type = GrantType.AuthorizationCode.toString(),
                code = authResponse.authorizationCode,
                redirect_uri = "https://github.com/"
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
            .appendQueryParameter("client_id", authRequest.client_id)
            .appendQueryParameter("redirect_uri", authRequest.redirect_uri)
            .appendQueryParameter("response_type", authRequest.response_type.toString())
            .appendQueryParameter("grant_type", authRequest.grant_type.toString())
            .appendQueryParameter("state", authRequest.state)
            .appendQueryParameter("scope", Scope.combineScopes(authRequest.scopes))
            .appendQueryParameter("nonce", authRequest.nonce)
            .build()
        val alertDialog = openWebView(context, uri, cookie) { url ->
            val uri: Uri = Uri.parse(url)
            if (url.startsWith(authRequest.redirect_uri)) {
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
            val uri: Uri = Uri.Builder()
                .scheme(SCHEME)
                .authority(BASE_AUTHORITY)
                .build()

            val alertDialog = openWebView(context, uri) { url ->
                val uri: Uri = Uri.parse(url)
                if (!(uri.scheme + "://" + uri.encodedAuthority).contentEquals(BASE_URL)) {
                    onFailure()
                    return@openWebView false
                }
                if (uri.path.contentEquals("/dashboard")) {
                    onSuccess(retrieveCookie(uri.scheme + "://" + uri.encodedAuthority))
                    return@openWebView false
                }
                return@openWebView true
            }
            alertDialog.setOnDismissListener { onUserDismiss() }
        },
        onUserDismiss = onUserDismiss,
        onSelect = onSuccess
    )

    private fun selectAccountFromAccountManager(
        context: Context,
        onCreateNewAccount: () -> Unit,
        onUserDismiss: () -> Unit,
        onSelect: (cookie: String) -> Unit
    ) {
        onCreateNewAccount()
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
