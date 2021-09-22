package edu.nitt.delta.interfaces

import edu.nitt.delta.models.User

interface SignInListener {

    fun onSuccess(user: User)

    fun onFailure(e: Exception)
}
