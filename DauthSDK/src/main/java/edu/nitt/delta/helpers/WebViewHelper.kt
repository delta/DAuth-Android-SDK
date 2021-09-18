package edu.nitt.delta.helpers

import android.R
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import edu.nitt.delta.interfaces.ShouldOverrideURLListener

internal fun openWebViewWithUriAndCookie(context: Context, uri: Uri, shouldOverrideURLListener: ShouldOverrideURLListener, cookie: String? = null): Dialog {

    val progressBar= ProgressBar(context,null, R.attr.progressBarStyleHorizontal)
    val webView = object : WebView(context){
        override fun onCheckIsTextEditor(): Boolean {
            return true
        }
    }
    val alertDialog = Dialog(context,android.R.style.Theme_Material_NoActionBar_Fullscreen)
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

    val webSettings: WebSettings = webView.settings
    webSettings.javaScriptEnabled = true
    webSettings.domStorageEnabled = true
    webView.isFocusableInTouchMode = true
    webView.isFocusable = true

    webView.webChromeClient = object : WebChromeClient(){
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            progressBar.visibility = View.VISIBLE
            progressBar.setProgress(newProgress)

            if(newProgress==100){
                progressBar.visibility=View.GONE
            }
            super.onProgressChanged(view, newProgress)
        }
    }
    val layWrap = LinearLayout(context)
    layWrap.orientation = LinearLayout.VERTICAL
    layWrap.addView(progressBar)
    progressBar.visibility=View.INVISIBLE
    val layoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.MATCH_PARENT
    )
    val linearlayoutParams = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT
    )
    webView.layoutParams = layoutParams
    layWrap.layoutParams = linearlayoutParams

    layWrap.addView(webView)

    alertDialog.setContentView(layWrap)
    alertDialog.show()
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
