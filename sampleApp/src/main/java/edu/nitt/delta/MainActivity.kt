package edu.nitt.delta

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import edu.nitt.delta.interfaces.SignInListener
import edu.nitt.delta.models.AuthorizationRequest
import edu.nitt.delta.models.GrantType
import edu.nitt.delta.models.ResponseType
import edu.nitt.delta.models.Scope
import edu.nitt.delta.models.User

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val signInButton: Button = findViewById(R.id.sign_in_button)
        signInButton.setOnClickListener {
            DAuth.signIn(
                context = this,
                authRequest = AuthorizationRequest(
                    "PwLV_Z_GGJSZECg1",
                    "https://github.com/",
                    ResponseType.Code,
                    GrantType.AuthorizationCode,
                    "1ww12",
                    listOf(Scope.OpenID, Scope.User, Scope.Email, Scope.Profile),
                    "ncsasd"
                ),
                clientSecret = "cRaxHJSugJeo8YJOO5GisYgDv6x53Uxs",
                signInListener = object : SignInListener {
                    override fun onSuccess(user: User) {
                        println("Success: $user")
                    }

                    override fun onFailure(e: Exception) {
                        e.printStackTrace()
                    }
                })
        }
    }
}
