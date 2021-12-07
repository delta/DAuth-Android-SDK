package edu.nitt.delta.helpers

import android.util.Base64
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

class PkceUtil {

    private val encodeSettings = Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE

    fun generateCodeVerifier(): String {
        val secureRandom = SecureRandom()
        val codeVerifier = ByteArray(32)
        secureRandom.nextBytes(codeVerifier)
        return Base64.encodeToString(codeVerifier, encodeSettings)
    }

    fun generateCodeChallenge(codeVerifier: String, algorithm: String): String {
        return if (algorithm == "S256") {
            val bytes = codeVerifier.toByteArray(charset("US-ASCII"))
            val messageDigest = MessageDigest.getInstance("SHA-256")
            messageDigest.update(bytes, 0, bytes.size)
            val digest = messageDigest.digest()
            Base64.encodeToString(digest, encodeSettings)
        } else {
            codeVerifier
        }
    }

    fun getCodeChallengeMethod(): String {
        return try {
            MessageDigest.getInstance("SHA-256")
            "S256"
        } catch (e: NoSuchAlgorithmException) {
            "plain"
        }
    }
}
