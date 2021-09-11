package edu.nitt.delta.models

data class AuthorizationResponse(
    val authorizationCode : String,
    val state : String
)
