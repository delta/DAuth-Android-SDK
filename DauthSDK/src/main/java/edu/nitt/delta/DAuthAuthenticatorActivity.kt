package edu.nitt.delta

import android.accounts.Account
import android.accounts.AccountAuthenticatorActivity
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import edu.nitt.delta.helpers.DAuthConstants
import edu.nitt.delta.helpers.openWebView
import edu.nitt.delta.helpers.retrieveCookie

class DAuthAuthenticatorActivity : Activity() {
//    private val TAG = "authactivity"
//    @SuppressLint("CommitPrefEdits")
private  val TAG = "DAuthAuthenticatorActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dauth_authenticator)
    val uri: Uri = Uri.Builder()
        .scheme(DAuthConstants.SCHEME)
        .authority(DAuthConstants.BASE_AUTHORITY)
        .build()
    val alertDialog = openWebView(this, uri) { url ->
        val uri: Uri = Uri.parse(url)
        if (!(uri.scheme + "://" + uri.encodedAuthority).contentEquals(DAuthConstants.BASE_URL)) {
//            onFailure()
            return@openWebView false
        }
        if (uri.path.contentEquals("/dashboard")) {
//            onSuccess(retrieveCookie(uri.scheme + "://" + uri.encodedAuthority))
            return@openWebView false
        }
        return@openWebView true
    }
    alertDialog.setOnDismissListener{}
//    alertDialog.setOnDismissListener { onUserDismiss() }
    }
}