package edu.nitt.delta.helpers

import android.R
import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import org.json.JSONObject

internal fun openWebView(
    context: Context,
    uri: Uri,
    cookie: String? = null,
    onFailure: () -> Unit,
    shouldLoadUrl: (String) -> Boolean,
): Dialog = openWebView(context, uri, cookie, onFailure, shouldLoadUrl, null)

internal fun openWebView(
    context: Context,
    uri: Uri,
    cookie: String? = null,
    onFailure: () -> Unit,
    shouldLoadUrl: (String) -> Boolean,
    onLogin: ((String, String) -> Unit)? = null,
): Dialog {
    val progressBar = ProgressBar(context, null, R.attr.progressBarStyleHorizontal)
    val webView = object : WebView(context) {
        override fun onCheckIsTextEditor() = true
    }
    val alertDialog = Dialog(context, R.style.Theme_Material_NoActionBar_Fullscreen)
    val postRequests = object {
        private val payloadMap: MutableMap<String, String> = mutableMapOf()

        @JavascriptInterface
        fun recordPayload(
            url: String,
            payload: String
        ) {
            payloadMap[url] = payload
        }

        fun getPayload(url: String) = payloadMap[url]
    }
    webView.addJavascriptInterface(postRequests, "postRequest")

    webView.webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            val loadURL = shouldLoadUrl(url)
            if (loadURL)
                view.loadUrl(url)
            if (!loadURL) {
                alertDialog.setOnDismissListener { }
                alertDialog.dismiss()
            }
            return true
        }

        override fun shouldInterceptRequest(
            view: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            if (onLogin != null) {
                if (request?.url.toString().contentEquals("https://auth.delta.nitt.edu/api/auth/login")) {
                    val payload = postRequests.getPayload(request?.url.toString())
                    val json = JSONObject(payload.toString())
                    onLogin(json.getString("email"), json.getString("password"))
                }
            }
            return super.shouldInterceptRequest(view, request)
        }

        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            if (!isReload && url != null) {
                val dismiss = !shouldLoadUrl(url)
                if (dismiss) {
                    alertDialog.setOnDismissListener { }
                    alertDialog.dismiss()
                }
            }
            if (isDarkThemeOn(context)) {
                view?.evaluateJavascript(
                    "window.localStorage.setItem('DAuth-theme', 'dark');",
                    null
                )
            } else {
                view?.evaluateJavascript(
                    "window.localStorage.setItem('DAuth-theme', 'light');",
                    null
                )
            }
            if (onLogin != null) {
                view?.evaluateJavascript(
                    """
                    if(!XMLHttpRequest.prototype.origOpen)
                        XMLHttpRequest.prototype.origOpen = XMLHttpRequest.prototype.open;
                    XMLHttpRequest.prototype.open = function(method, url, async, user, password) {
                        // these will be the key to retrieve the payload
                        this.recordedMethod = method;
                        this.recordedUrl = url;
                        this.origOpen(method, url, async, user, password);
                    };
                    if(!XMLHttpRequest.prototype.origSend)
                        XMLHttpRequest.prototype.origSend = XMLHttpRequest.prototype.send;
                    XMLHttpRequest.prototype.send = function(body) {
                        // interceptor is a Kotlin interface added in WebView
                        if(body && this.recordedMethod === 'POST') postRequest.recordPayload(this.recordedUrl, body);
                        this.origSend(body);
                    };
                    """.trimIndent(), null
                )
            }
            super.doUpdateVisitedHistory(view, url, isReload)
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            alertDialog.setOnDismissListener {  }
            alertDialog.cancel()
            onFailure()
        }
    }

    val webSettings: WebSettings = webView.settings
    webSettings.javaScriptEnabled = true
    webSettings.domStorageEnabled = true
    webView.isFocusableInTouchMode = true
    webView.isFocusable = true

    webView.webChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            progressBar.visibility = View.VISIBLE
            progressBar.progress = newProgress

            if (newProgress == 100) {
                progressBar.visibility = View.GONE
            }
            super.onProgressChanged(view, newProgress)
        }
    }
    val layWrap = LinearLayout(context)
    layWrap.orientation = LinearLayout.VERTICAL
    layWrap.addView(progressBar)
    progressBar.visibility = View.INVISIBLE
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
    insertCookie(cookie, "${uri.scheme}://${uri.encodedAuthority}")

    webView.loadUrl(uri.toString())
    return alertDialog
}

internal fun retrieveCookie(url: String): String {
    val cookieManager: CookieManager = CookieManager.getInstance()
    return cookieManager.getCookie(url)
}

internal fun deleteCookie() {
    val cookieManager: CookieManager = CookieManager.getInstance()
    cookieManager.setAcceptCookie(true)
    cookieManager.removeAllCookies(null)
    cookieManager.flush()
}

internal fun insertCookie(cookie: String?, url: String) {
    deleteCookie()
    if (cookie?.isNotBlank()?.and(cookie.isNotEmpty()) == true) {
        val cookieManager: CookieManager = CookieManager.getInstance()
        cookieManager.setCookie(url, cookie)
    }
}

internal fun isDarkThemeOn(context: Context) =
    context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
