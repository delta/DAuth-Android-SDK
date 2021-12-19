package edu.nitt.delta.auth.models

data class ClientCredentials(
    val clientId: String,
    val redirectUri: String,
    val clientSecret: String
)
