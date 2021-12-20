package edu.nitt.delta.auth

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Service to initialize the authenticator
 */

internal class DauthAuthenticatorService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        val authenticator = DauthAccountAuthenticator(this)
        return authenticator.iBinder
    }
}
