package edu.nitt.delta.auth.models

data class AuthorizationRequest(
    val response_type: ResponseType,
    val grant_type: GrantType,
    val state: String,
    val scopes: List<Scope>,
    val nonce: String
)
