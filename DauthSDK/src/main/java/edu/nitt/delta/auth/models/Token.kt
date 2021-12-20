package edu.nitt.delta.auth.models

data class Token(
    val access_token: String,
    val state: String,
    val id_token : String
)
