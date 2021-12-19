package edu.nitt.delta.auth.models

data class AuthorizationResponse(
    val authorizationCode : String,
    val state : String,
    val codeVerifier: String?,
    val isPkceEnabled: Boolean
)
