package edu.nitt.delta.models

data class AuthorizationRequest(
    val response_type: ResponseType,
    val grant_type: GrantType,
    val state: String,
    val scopes: List<Scope>,
    val nonce: String
)
