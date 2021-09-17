package edu.nitt.delta.helpers

import edu.nitt.delta.models.AuthorizationResponse

object AuthorizationState {
    enum class AuthorizationErrorState{
        NetworkError,
        UserDismissed,
        InternalError,
        AuthorizationDenied
    }
    interface AuthorizationStateListener {
        fun onFailure(authorizationErrorState: AuthorizationErrorState)
        fun onSuccess(authorizationResponse: AuthorizationResponse)
    }
}
