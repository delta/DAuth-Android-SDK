package edu.nitt.delta.models

data class TokenRequest(
    val client_id:String,
    val client_secret:String,
    val grant_type:String,
    val code: String,
    val redirect_uri: String
)
