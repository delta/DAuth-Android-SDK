package edu.nitt.delta

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.nitt.delta.deltaButton.DeltaButton
import edu.nitt.delta.models.AuthorizationRequest
import edu.nitt.delta.models.GrantType
import edu.nitt.delta.models.ResponseType
import edu.nitt.delta.models.Scope
import edu.nitt.delta.models.User

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val signInButton: DeltaButton = findViewById(R.id.sign_in_button)
        signInButton.setOnClickListener {
            DAuth.signIn(
                activity = this,
                authorizationRequest = AuthorizationRequest(
                    ResponseType.Code,
                    GrantType.AuthorizationCode,
                    "1ww12",
                    listOf(Scope.OpenID, Scope.User, Scope.Email, Scope.Profile),
                    "ncsasd"
                ),
                onSuccess = { user: User ->
                        println("Success: $user")
                },
                onFailure = { exception: Exception ->
                    exception.printStackTrace()
                }
            )
        }

    }
}
