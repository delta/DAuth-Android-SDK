package edu.nitt.delta

import android.content.Context
import android.net.Uri
import edu.nitt.delta.helpers.AuthorizationState
import edu.nitt.delta.helpers.DAuthConstants
import edu.nitt.delta.helpers.DAuthConstants.BASE_AUTHORITY
import edu.nitt.delta.helpers.DAuthConstants.BASE_URL
import edu.nitt.delta.helpers.DAuthConstants.SCHEME
import edu.nitt.delta.helpers.openWebViewWithUriAndCookie
import edu.nitt.delta.helpers.retrieveCookie
import edu.nitt.delta.interfaces.SelectAccountFromAccountManagerListener
import edu.nitt.delta.interfaces.SelectAccountListener
import edu.nitt.delta.interfaces.ShouldOverrideURLListener
import edu.nitt.delta.models.AuthorizationRequest
import edu.nitt.delta.models.AuthorizationResponse
import edu.nitt.delta.models.Scope
import edu.nitt.delta.models.Token
import edu.nitt.delta.models.TokenRequest
import edu.nitt.delta.models.User

class DAuth {

    lateinit var currentUser: User

    // to request for authorization use authorizationRequest members as query parameters
    fun requestAuthorization(context: Context, authorizationRequest: AuthorizationRequest, authorizationStateListener: AuthorizationState.AuthorizationStateListener){
        selectAccount(context, object : SelectAccountListener {
            override fun onSuccess(cookie: String) {
                val uri: Uri = Uri.Builder()
                    .scheme(SCHEME)
                    .authority(BASE_AUTHORITY)
                    .appendPath("authorize")
                    .appendQueryParameter("client_id", authorizationRequest.client_id)
                    .appendQueryParameter("redirect_uri", authorizationRequest.redirect_uri)
                    .appendQueryParameter("response_type", authorizationRequest.response_type.toString())
                    .appendQueryParameter("grant_type",authorizationRequest.grant_type.toString())
                    .appendQueryParameter("state",authorizationRequest.state)
                    .appendQueryParameter("scopes", Scope.combineScopes(authorizationRequest.scopes))
                    .appendQueryParameter("nonce",authorizationRequest.nonce)
                    .build()
                val alertDialog = openWebViewWithUriAndCookie(
                    context,
                    uri,
                    object : ShouldOverrideURLListener {
                        override fun shouldLoadUrl(url: String): Boolean {
                            val uri: Uri = Uri.parse(url)
                            if (url.startsWith(authorizationRequest.redirect_uri)) {
                                if (uri.query.isNullOrBlank() or uri.query.isNullOrEmpty()){
                                    authorizationStateListener.onFailure(AuthorizationState.AuthorizationErrorState.AuthorizationDenied)
                                }else {
                                    val authorizationResponse = AuthorizationResponse(uri.getQueryParameter("code") ?: "", uri.getQueryParameter("state") ?: "")
                                    authorizationStateListener.onSuccess(authorizationResponse)
                                }
                                return false
                            }
                            if (!(uri.scheme + "://" + uri.encodedAuthority).contentEquals(BASE_URL)){
                                authorizationStateListener.onFailure(AuthorizationState.AuthorizationErrorState.InternalError)
                                return false
                            }
                            if (uri.path == "/dashboard"){
                                authorizationStateListener.onFailure(AuthorizationState.AuthorizationErrorState.InternalError)
                                return false
                            }
                            return true
                        } },
                    cookie)
                alertDialog.setOnDismissListener {
                    authorizationStateListener.onFailure(AuthorizationState.AuthorizationErrorState.UserDismissed)
                }
            }

            override fun onFailure() {
                authorizationStateListener.onFailure(AuthorizationState.AuthorizationErrorState.InternalError)
            }

            override fun onUserDismiss() {
                authorizationStateListener.onFailure(AuthorizationState.AuthorizationErrorState.UserDismissed)
            }
        })
    }

    //to request token use tokenRequest members as query parameters
    fun requestToken(tokenRequest: TokenRequest): Token {
        TODO("make a request and token will be obtained as response")
    }

    fun getLoggedUser(): User{
        TODO("return current User")
    }

    private fun selectAccount(context: Context, selectAccountListener: SelectAccountListener){
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
                    object : ShouldOverrideURLListener{
                        override fun shouldLoadUrl(url: String): Boolean {
                            val uri: Uri = Uri.parse(url)
                            if (!(uri.scheme + "://" + uri.encodedAuthority).contentEquals(DAuthConstants.BASE_URL)) {
                                selectAccountListener.onFailure()
                                return false
                            }
                            if (uri.path.contentEquals("/dashboard")){
                                selectAccountListener.onSuccess(retrieveCookie(uri.scheme + "://" + uri.encodedAuthority))
                                return false
                            }
                            return true
                        } })
                alertDialog.setOnDismissListener {
                    selectAccountListener.onUserDismiss()
                }
            }

            override fun onUserDismiss() {
                selectAccountListener.onUserDismiss()
            }
        })
    }

    private fun selectAccountFromAccountManager(context: Context, selectAccountFromAccountManagerListener: SelectAccountFromAccountManagerListener){
        selectAccountFromAccountManagerListener.onCreateNewAccount()
        //TODO("Account Selection UI for testing uncomment the previous")
    }

    fun registerWithClient(){
        TODO("To be implemented")
    }

    // check if accountManager already has it
    fun checkIfUserExists(): Boolean{
        TODO("To be implemented")
    }

    // adds user in accountManager
    fun addUser(){
        TODO("To be implemented")
    }
}
