package edu.nitt.delta

import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
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
        selectAccount(context, object :SelectAccountListener{
            override fun onSuccess(cookie: String) {
                val uri: Uri = Uri.Builder()
                    .scheme(SCHEMA)
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
                    object : ShouldOverrideURLListener{
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
                            if (!(uri.scheme + "://" + uri.encodedAuthority).contentEquals(Companion.BASE_URL)){
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
        selectAccountFromAccountManager(context, object : SelectAccountFromAccountManagerListener{
            override fun onSelect(cookie: String) {
                selectAccountListener.onSuccess(cookie)
            }

            override fun onCreateNewAccount() {
                val uri: Uri = Uri.Builder()
                    .scheme("https")
                    .authority("auth.delta.nitt.edu")
                    .build()

                val alertDialog = openWebViewWithUriAndCookie(
                    context,
                    uri,
                    object : ShouldOverrideURLListener{
                        override fun shouldLoadUrl(url: String): Boolean {
                            val uri: Uri = Uri.parse(url)
                            if (!(uri.scheme + "://" + uri.encodedAuthority).contentEquals(Companion.BASE_URL)) {
                                selectAccountListener.onFailure()
                                return false
                            }
                            if (uri.path.contentEquals("/dashboard")){
                                selectAccountListener.onSuccess(retrieveCookie())
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

    private fun retrieveCookie():String{
        val cookieManager: CookieManager = CookieManager.getInstance()
        return cookieManager.getCookie(Companion.BASE_URL)
    }

    private fun deleteCookie(){
        val cookieManager: CookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
    }

    private fun insertCookie(cookie: String?){
        deleteCookie()
        if (cookie?.isNotBlank()?.and(cookie.isNotEmpty()) == true) {
            val cookieManager: CookieManager = CookieManager.getInstance()
            cookieManager.setCookie(Companion.BASE_URL, cookie)
        }
    }

    private fun isDarkThemeOn(context: Context): Boolean {
        return context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES
    }

    private fun openWebViewWithUriAndCookie(context: Context, uri: Uri, shouldOverrideURLListener: ShouldOverrideURLListener, cookie: String? = null):AlertDialog{
        val webView = WebView(context)
        val alertDialog = AlertDialog.Builder(context).create()
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                val loadURL = shouldOverrideURLListener.shouldLoadUrl(url)
                if (loadURL) {
                    view.loadUrl(url)
                }
                if (!loadURL){
                    alertDialog.setOnDismissListener {  }
                    alertDialog.dismiss()
                }
                return true
            }

            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                if (!isReload && url != null){
                    val dismiss = !shouldOverrideURLListener.shouldLoadUrl(url)
                    if (dismiss){
                        alertDialog.setOnDismissListener {  }
                        alertDialog.dismiss()
                    }
                }
                if (isDarkThemeOn(context)) {
                    view?.evaluateJavascript("window.localStorage.setItem('DAuth-theme', 'dark');", null)
                }
                else{
                    view?.evaluateJavascript("window.localStorage.setItem('DAuth-theme', 'light');", null)
                }
                super.doUpdateVisitedHistory(view, url, isReload)
            }
        }
        alertDialog.setView(webView)
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true

        webView.setPadding(0,0,0,0)

        val screenWidth = context.resources.displayMetrics.widthPixels
        val screenHeight = context.resources.displayMetrics.heightPixels
        var width = WIDTH
        var height = HEIGHT
        var scale = SCALE
        if ( screenWidth < width){
            scale *= screenWidth/width
            height *= scale/ SCALE
            width = screenWidth
        }
        if (screenHeight < height){
            scale *= screenHeight/height
            width = WIDTH*scale/SCALE
            height = screenHeight
        }

        val params = FrameLayout.LayoutParams(width, height)
        webView.layoutParams = params
        webView.setInitialScale(scale)
        alertDialog.show()
        alertDialog.window!!.setLayout(width, height)
        insertCookie(cookie)

        webView.loadUrl(uri.toString())
        return alertDialog
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

    private interface ShouldOverrideURLListener{
        fun shouldLoadUrl(url: String): Boolean
    }

    private interface SelectAccountFromAccountManagerListener{
        fun onSelect(cookie: String)
        fun onCreateNewAccount()
        fun onUserDismiss()
    }

    private interface SelectAccountListener{
        fun onSuccess(cookie: String)
        fun onFailure()
        fun onUserDismiss()
    }

    object AuthorizationState {
        enum class AuthorizationErrorState{
            NetworkError,
            UserDismissed,
            InternalError,
            AuthorizationDenied
        }
        interface AuthorizationStateListener {
            fun onFailure(authorizationErrorState: AuthorizationErrorState)
            fun onSuccess(authorizationResponse: AuthorizationResponse)
        }
    }

    companion object {
        private const val SCHEMA = "https"
        private const val BASE_AUTHORITY = "auth.delta.nitt.edu"
        private const val BASE_URL ="https://auth.delta.nitt.edu"
        private const val WIDTH = 800
        private const val HEIGHT = 1485
        private const val SCALE = 200
    }
}