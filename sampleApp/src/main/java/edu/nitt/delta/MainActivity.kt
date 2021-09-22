package edu.nitt.delta

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import edu.nitt.delta.interfaces.SignInListener
import edu.nitt.delta.models.User

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val dAuth = DAuth()
        val signInButton: Button = findViewById(R.id.sign_in_button)
        signInButton.setOnClickListener {
            dAuth.signIn(this, object : SignInListener {
                override fun onSuccess(user: User) {
                    println(user)
                }

                override fun onFailure(e: Exception) {
                    e.printStackTrace()
                }
            })
        }
    }
}
