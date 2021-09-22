package edu.nitt.delta

import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import android.os.Handler

class  AccountsManager {
    fun AccountsManager(context: Context) {
        val am: AccountManager = AccountManager.get(context)
        val options = Bundle()
//        am.getAuthToken(
//            ,                     // Account retrieved using getAccountsByType()
//            "Manage your tasks",            // Auth scope
//            options,                        // Authenticator-specific options
//            context,                           // Your activity
//            OnTokenAcquired(),              // Callback called when a token is successfully acquired
//            Handler(OnError())              // Callback called if an error occurs
//        )


    }

}
