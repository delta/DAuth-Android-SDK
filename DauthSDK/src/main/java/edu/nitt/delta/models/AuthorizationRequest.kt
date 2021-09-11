package edu.nitt.delta.models

data class AuthorizationRequest(
    val client_id:String,
    val redirect_uri:String,
    val response_type:String,
    val grant_type:String,
    val state:String,
    val scope:String,
    val nonce:String
)
