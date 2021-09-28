package edu.nitt.delta

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import edu.nitt.delta.interfaces.SignInListener
import edu.nitt.delta.models.AuthorizationRequest
import edu.nitt.delta.models.GrantType
import edu.nitt.delta.models.ResponseType
import edu.nitt.delta.models.Scope
import edu.nitt.delta.models.User

class MainActivity : AppCompatActivity() ,LoginDialog.LoginDialogListener{
    private  val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val signInButton: Button = findViewById(R.id.sign_in_button)
        signInButton.setOnClickListener {
//            openDialog()
            }
            DAuth.signIn(
                context = this,
                authRequest = AuthorizationRequest(
                    ResponseType.Code,
                    GrantType.AuthorizationCode,
                    "1ww12",
                    listOf(Scope.OpenID, Scope.User, Scope.Email, Scope.Profile),
                    "ncsasd"
                ),
                signInListener = object : SignInListener {
                    override fun onSuccess(user: User) {
                        println("Success: $user")

//                        Log.d(TAG, "onSuccess: ")
                    }

                    override fun onFailure(e: Exception) {
                        e.printStackTrace()
                    }
                })
        }

    override fun applyTexts(username: String?, password: String?) {
//        textViewUsername.setText(username)
//        textViewPassword.setText(password)
    }
    fun openDialog() {
        val exampleDialog = LoginDialog()
        exampleDialog.show(supportFragmentManager, "login dialog")
    }
}
