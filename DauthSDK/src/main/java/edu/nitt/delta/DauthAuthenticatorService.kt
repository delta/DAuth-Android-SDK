package edu.nitt.delta

import android.app.Service
import android.content.Intent
import android.os.IBinder

class DauthAuthenticatorService : Service() {
    //Service to initiate the authenticator
    override fun onBind(intent: Intent?): IBinder? {
        val authenticator = DauthAccountAuthenticator(this)
        return authenticator.iBinder
    }
}
