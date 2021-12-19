package edu.nitt.delta.auth.models

data class jwt(
    val header: Header,
    val data: Data
)

data class Header (
    val typ: String,
    val kid: String,
    val alg: String
)

data class Data (
    val sub: Long,
    val nonce: String,
    val authTime: String,
    val email: String,
    val emailVerified: Boolean,
    val name: String,
    val aud: String,
    val iss: String,
    val iat: Long,
    val exp: Long
)
