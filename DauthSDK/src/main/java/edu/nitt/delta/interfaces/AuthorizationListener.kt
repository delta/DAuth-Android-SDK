package edu.nitt.delta.interfaces

import edu.nitt.delta.models.AuthorizationErrorType
import edu.nitt.delta.models.AuthorizationResponse

interface AuthorizationListener {
    fun onSuccess(authorizationResponse: AuthorizationResponse)
    fun onFailure(authorizationErrorType: AuthorizationErrorType)
}
