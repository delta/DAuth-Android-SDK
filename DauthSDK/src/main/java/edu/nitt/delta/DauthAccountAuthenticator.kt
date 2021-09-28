package edu.nitt.delta
import edu.nitt.delta.R
import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.text.TextUtils




class DauthAccountAuthenticator(context: Context) : AbstractAccountAuthenticator(context) {
    private val TAG = "DauthAccountAuthenticat"
    val mContext = context
    override fun editProperties(
        response: AccountAuthenticatorResponse?,
        accountType: String?
    ): Bundle {
        TODO("Not yet implemented")
    }

    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle {
        val accountManager = AccountManager.get(mContext)
        val account =
            Account("pranav123456", "auth.delta.nitt.edu")
        val added: Boolean = accountManager.addAccountExplicitly(account,"Pranav1811" , Bundle())
        Log.d(TAG, "addAccount: $added")
        val intent = Intent(mContext, DAuthAuthenticatorActivity::class.java)
        intent.putExtra("accountType", accountType)
        intent.putExtra("authTokenType", authTokenType)
        intent.putExtra("is_adding_new_account", true)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        options: Bundle?
    ): Bundle {
        TODO("Not yet implemented")
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        // Extract the username and password from the Account Manager, and ask
        // the server for an appropriate AuthToken.
        // Extract the username and password from the Account Manager, and ask
        // the server for an appropriate AuthToken.
        val am = AccountManager.get(mContext)

        var authToken = am.peekAuthToken(account, authTokenType)

        // Lets give another try to authenticate the user

        // Lets give another try to authenticate the user
        if (TextUtils.isEmpty(authToken)) {
            val password = am.getPassword(account)
            if (password != null) {
//                authToken = sServerAuthenticate.userSignIn(account!!.name, password, authTokenType)
            }
        }

        // If we get an authToken - we return it

        // If we get an authToken - we return it
        if (!TextUtils.isEmpty(authToken)) {
            val result = Bundle()
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account!!.name)
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account!!.type)
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken)
            return result
        }

        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity.

        // If we get here, then we couldn't access the user's password - so we
        // need to re-prompt them for their credentials. We do that by creating
        // an intent to display our AuthenticatorActivity.
        val intent = Intent(mContext, DAuthAuthenticatorActivity::class.java)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        intent.putExtra("accountType", account!!.type)
        intent.putExtra("authTokenType", authTokenType)
        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }

    override fun getAuthTokenLabel(authTokenType: String?): String {
        TODO("Not yet implemented")
    }

    override fun updateCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        TODO("Not yet implemented")
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        features: Array<out String>?
    ): Bundle {
        TODO("Not yet implemented")
    }
}