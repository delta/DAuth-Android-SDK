package edu.nitt.delta

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import edu.nitt.delta.helpers.AuthorizationState.AuthorizationErrorState
import edu.nitt.delta.helpers.AuthorizationState.AuthorizationStateListener
import edu.nitt.delta.models.AuthorizationRequest
import edu.nitt.delta.models.AuthorizationResponse
import edu.nitt.delta.models.GrantType
import edu.nitt.delta.models.ResponseType
import edu.nitt.delta.models.Scope

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val dAuth = DAuth()
        val signInButton:Button = findViewById(R.id.sign_in_button)
        signInButton.setOnClickListener {
            dAuth.requestAuthorization(this, AuthorizationRequest("xobh.KPYVvLXhGum","https://www.google.com",
                ResponseType.Code, GrantType.AuthorizationCode,"1ww12", List<Scope>(1){Scope.OpenID}, "ncsasd"),object : AuthorizationStateListener{
                override fun onFailure(authorizationErrorState: AuthorizationErrorState) {
                    Log.d("dtest",authorizationErrorState.toString())
                }

                override fun onSuccess(authorizationResponse: AuthorizationResponse) {
                    Log.d("dtest",authorizationResponse.toString())
                }
            })
        }

    }
}
