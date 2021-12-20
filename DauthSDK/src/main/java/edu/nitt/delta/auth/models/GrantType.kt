package edu.nitt.delta.auth.models

enum class GrantType {
    AuthorizationCode {
        override fun toString(): String {
            return "authorization_code"
        }};
}
