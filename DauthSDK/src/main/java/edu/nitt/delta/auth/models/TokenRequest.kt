package edu.nitt.delta.auth.models

data class TokenRequest(
    val grant_type:String,
    val code: String,
    val code_verifier: String?
)
