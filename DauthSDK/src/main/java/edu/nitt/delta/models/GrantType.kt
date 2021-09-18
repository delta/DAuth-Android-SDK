package edu.nitt.delta.models

enum class GrantType {
    AuthorizationCode {
        override fun toString(): String {
            return "authorization_code"
        }};
}
