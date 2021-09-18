package edu.nitt.delta.helpers

import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import edu.nitt.delta.helpers.WebViewConstants.HEIGHT
import edu.nitt.delta.helpers.WebViewConstants.SCALE
import edu.nitt.delta.helpers.WebViewConstants.WIDTH
import edu.nitt.delta.interfaces.ShouldOverrideURLListener

internal fun openWebViewWithUriAndCookie(context: Context, uri: Uri, shouldOverrideURLListener: ShouldOverrideURLListener, cookie: String? = null): AlertDialog {


    val webView = object : WebView(context){
        override fun onCheckIsTextEditor(): Boolean {
            return true
        }
    }
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
    webView.isFocusableInTouchMode = true
    webView.isFocusable = true


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
        width = WIDTH *scale/ SCALE
        height = screenHeight
    }

    val params = FrameLayout.LayoutParams(width, height)
    webView.layoutParams = params
    webView.setInitialScale(scale)
    alertDialog.show()
    alertDialog.window!!.setLayout(width, height)
    insertCookie(cookie, uri.scheme + "://" + uri.encodedAuthority)

    webView.loadUrl(uri.toString())
    return alertDialog
}

internal fun retrieveCookie(url: String):String{
    val cookieManager: CookieManager = CookieManager.getInstance()
    return cookieManager.getCookie(url)
}

internal fun deleteCookie(){
    val cookieManager: CookieManager = CookieManager.getInstance()
    cookieManager.setAcceptCookie(true)
    cookieManager.removeAllCookies(null)
    cookieManager.flush()
}

internal fun insertCookie(cookie: String?, url: String){
    deleteCookie()
    if (cookie?.isNotBlank()?.and(cookie.isNotEmpty()) == true) {
        val cookieManager: CookieManager = CookieManager.getInstance()
        cookieManager.setCookie(url, cookie)
    }
}

internal fun isDarkThemeOn(context: Context): Boolean {
    return context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}
