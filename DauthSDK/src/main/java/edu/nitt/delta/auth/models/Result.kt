package edu.nitt.delta.auth.models

data class Result(
    val user: User?,
    val jwt:jwt?,
    val idToken:String?,
)
