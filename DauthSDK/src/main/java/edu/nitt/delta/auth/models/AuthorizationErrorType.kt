package edu.nitt.delta.auth.models

enum class AuthorizationErrorType {
    NetworkError,
    UserDismissed,
    InternalError,
    AuthorizationDenied,
    ServerDownError,
    UnableToGenerateCodeVerifier
}
