package edu.nitt.delta.interfaces

import edu.nitt.delta.models.User

interface FetchUserDetailsListener {
    fun onSuccess(user: User)
    fun onFailure(exception: Exception)
}