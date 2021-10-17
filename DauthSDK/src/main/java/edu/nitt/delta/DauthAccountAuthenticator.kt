package edu.nitt.delta

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import edu.nitt.delta.api.RetrofitInstance
import edu.nitt.delta.helpers.toDate
import edu.nitt.delta.helpers.toFormatString
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class DauthAccountAuthenticator(context: Context) : AbstractAccountAuthenticator(context) {
    private val mContext = context
    private val accountManager = AccountManager.get(mContext)
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
        val intent = Intent(mContext, DAuthAuthenticatorActivity::class.java)
        intent.putExtra("ACCOUNT_TYPE", accountType)
        intent.putExtra("AUTH_TYPE", authTokenType)
        intent.putExtra("IS_ADDING_ACCOUNT", true)
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

    private fun returnAuthToken(account: Account?, response: AccountAuthenticatorResponse?){
        val bundle = Bundle()
        val authToken = accountManager.getUserData(
            account,
            AccountManager.KEY_AUTHTOKEN
        )
        bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, account?.type)
        bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account?.name)
        bundle.putString(AccountManager.KEY_AUTHTOKEN, authToken)
        response?.onResult(bundle)
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        val dueDateString = accountManager.getUserData(account, AccountManager.KEY_LAST_AUTHENTICATED_TIME)
        val dueDate = dueDateString.toDate("dd/MM/yyyy")
        val currentDate = Date()
        if (currentDate < dueDate) {
            returnAuthToken(account, response)
            return Bundle()
        }else{
            if (account == null){
                response?.onError(404, "Account Not Found")
                return Bundle()
            }
            RetrofitInstance.api.getCookie(
                account.name,
                accountManager.getPassword(account)
            ).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    retrofitResponse: Response<ResponseBody>
                ) {
                    if (retrofitResponse.isSuccessful) {
                        var cookie=""
                        for(i in retrofitResponse.headers()["Set-Cookie"].toString()){
                            if(i==';')
                                break
                            else cookie+=i
                        }

                        val calendar = Calendar.getInstance()
                        calendar.add(Calendar.DAY_OF_YEAR, 30)
                        val dueDate: String = calendar.time.toFormatString("dd/MM/yyyy")
                        accountManager.setUserData(account, AccountManager.KEY_LAST_AUTHENTICATED_TIME, dueDate)
                        accountManager.setUserData(account, AccountManager.KEY_AUTHTOKEN, cookie)
                        returnAuthToken(account, response)
                    } else {
                        response?.onError(510, "Invalid Credentials")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    response?.onError(510, "Invalid Credentials")
                }
            }
            )
        }
        return Bundle()
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
