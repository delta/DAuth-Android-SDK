package edu.nitt.delta.models

data class ClientCredentials(
    val clientId: String,
    val redirectUri: String,
    val clientSecret: String
)
