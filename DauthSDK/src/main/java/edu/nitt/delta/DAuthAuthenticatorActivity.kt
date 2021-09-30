package edu.nitt.delta

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import edu.nitt.delta.helpers.DAuthConstants
import edu.nitt.delta.helpers.openWebView
import edu.nitt.delta.helpers.retrieveCookie
import java.time.LocalDate

class DAuthAuthenticatorActivity : Activity() {
    private lateinit var email: String;
    private lateinit var password: String

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dauth_authenticator)
        val uri: Uri = Uri.Builder()
            .scheme(DAuthConstants.SCHEME)
            .authority(DAuthConstants.BASE_AUTHORITY)
            .build()
        val alertDialog = openWebView(this, uri, null, { url ->
            val uri: Uri = Uri.parse(url)
            if (!(uri.scheme + "://" + uri.encodedAuthority).contentEquals(DAuthConstants.BASE_URL)) {
                return@openWebView false
            }
            if (uri.path.contentEquals("/dashboard")) {
                val accountManager = AccountManager.get(this)
                val account =
                    Account(email, DAuthConstants.ACCOUNT_TYPE)
                val bundle = Bundle()
                bundle.putString(
                    AccountManager.KEY_AUTHTOKEN,
                    retrieveCookie(uri.scheme + "://" + uri.encodedAuthority)
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    bundle.putString(
                        AccountManager.KEY_LAST_AUTHENTICATED_TIME,
                        LocalDate.now().toString()
                    )
                }
                accountManager.addAccountExplicitly(account, password, bundle)
                finish()
                return@openWebView false
            }
            return@openWebView true
        }, { email, password ->
            this.email = email
            this.password = password
        })
        alertDialog.setOnDismissListener { finish() }
    }
}