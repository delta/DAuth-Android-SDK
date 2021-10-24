package edu.nitt.delta

import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import edu.nitt.delta.constants.AccountManagerConstants
import edu.nitt.delta.constants.WebViewConstants
import edu.nitt.delta.helpers.toFormatString
import edu.nitt.delta.helpers.openWebView
import edu.nitt.delta.helpers.retrieveCookie
import java.util.*

/**
 * Activity to add a new account to account manager
 */
internal class DAuthAuthenticatorActivity : Activity() {
    /**
     *  email [String] of the user
     *  password [String] of the user
     */
    private lateinit var email: String
    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dauth_authenticator)
        val response =
            intent.getParcelableExtra<AccountAuthenticatorResponse>(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)

        val uri: Uri = Uri.Builder()
            .scheme(WebViewConstants.Scheme)
            .authority(WebViewConstants.BaseAuthority)
            .build()
        val alertDialog = openWebView(
            this,
            uri,
            null,
            onFailure = {
                response?.onError(404,"Server Down error")
                finish()
                        }, { url ->
            val uri: Uri = Uri.parse(url)
            if (!(uri.scheme + "://" + uri.encodedAuthority).contentEquals(WebViewConstants.BaseUrl)) {
                return@openWebView false
            }
            if (uri.path.contentEquals("/dashboard")) {
                val accountManager = AccountManager.get(this)
                val account =
                    Account(email, AccountManagerConstants.AccountType)
                val bundle = Bundle()
                bundle.putString(AccountManager.KEY_AUTHTOKEN, retrieveCookie(uri.scheme + "://" + uri.encodedAuthority))
                val c1 = Calendar.getInstance()
                c1.add(Calendar.DAY_OF_YEAR, 30)
                val resultDate = c1.time
                val dueDate: String = resultDate.toFormatString("dd/MM/yyyy")
                bundle.putString(AccountManager.KEY_LAST_AUTHENTICATED_TIME, dueDate)
                bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
                bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
                if (accountManager.addAccountExplicitly(account, password, bundle)) {
                    response?.onResult(bundle)
                }
                else if (account in accountManager.accounts) {
                    accountManager.setUserData(
                        account,
                        AccountManager.KEY_AUTHTOKEN,
                        bundle.getString(AccountManager.KEY_AUTHTOKEN)
                    )
                    response?.onResult(bundle)
                }
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
