package edu.nitt.delta.models

data class Token(
    val access_token: String,
    val state: String,
    val id_token : String
)
