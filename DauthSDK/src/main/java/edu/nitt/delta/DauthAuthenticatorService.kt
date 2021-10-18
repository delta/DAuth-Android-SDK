package edu.nitt.delta

import android.app.Service
import android.content.Intent
import android.os.IBinder

class DauthAuthenticatorService : Service() {
    /**
     * Value Instance of [DauthAccountAuthenticator] class
     * @param context
     */
    override fun onBind(intent: Intent?): IBinder? {
        val authenticator = DauthAccountAuthenticator(this)
        return authenticator.iBinder
    }
}
