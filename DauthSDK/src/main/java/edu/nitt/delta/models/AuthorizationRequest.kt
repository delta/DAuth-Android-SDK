package edu.nitt.delta.models

data class AuthorizationRequest(
    val client_id: String,
    val redirect_uri: String,
    val response_type: ResponseType,
    val grant_type: GrantType,
    val state: String,
    val scopes: List<Scope>,
    val nonce: String
)
