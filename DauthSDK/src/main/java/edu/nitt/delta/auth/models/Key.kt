package edu.nitt.delta.auth.models

data class Key(
    val e: String,
    val n: String,
    val kty: String,
    val kid: String,
    val alg: String,
    val use: String
)
