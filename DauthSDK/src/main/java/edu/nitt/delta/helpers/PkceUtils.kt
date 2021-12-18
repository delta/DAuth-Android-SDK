package edu.nitt.delta.helpers

import android.util.Base64
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

internal class PkceUtil {
    /**
     * encodeSettings [encodeSettings] that stores constraints for encoding to string as int variable
     */
    private val encodeSettings = Base64.NO_WRAP or Base64.NO_PADDING or Base64.URL_SAFE

    /**
     *generates code verifier
     *
     * @return code verifier as string
     */
    fun generateCodeVerifier(): String {
        val secureRandom = SecureRandom()
        val codeVerifier = ByteArray(32)
        secureRandom.nextBytes(codeVerifier)
        return Base64.encodeToString(codeVerifier, encodeSettings)
    }

    /**
     * utility function to generate code challenge
     *
     * @param codeVerifier string for which code challenge has to be generated
     * @param algorithm String that describes the algorithm used to generate code challenge
     * @return code challenge as a string
     */
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

    /**
     * utility function to get algorithm for generating code challenge
     *
     * @return code challenge method as a string
     */
    fun getCodeChallengeMethod(): String {
        return try {
            MessageDigest.getInstance("SHA-256")
            "S256"
        } catch (e: NoSuchAlgorithmException) {
            "plain"
        }
    }
}
