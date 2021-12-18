package edu.nitt.delta.models

data class AuthorizationResponse(
    val authorizationCode : String,
    val state : String,
    val codeVerifier: String?,
    val isPkceEnabled: Boolean
)
