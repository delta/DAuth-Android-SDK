package edu.nitt.delta.auth.models

data class User(
    val id: Int,
    val email:String,
    val name:String,
    val phoneNumber: String,
)
