package edu.nitt.delta

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlin.math.log

class DauthAuthenticatorService : Service() {
    val TAG = "SERVICE"

    init {
        Log.d(TAG, "Service is runnning")
    }
    override fun onBind(intent: Intent?): IBinder? {
        val authenticator = DauthAccountAuthenticator(this)
        return authenticator.iBinder
    }
}