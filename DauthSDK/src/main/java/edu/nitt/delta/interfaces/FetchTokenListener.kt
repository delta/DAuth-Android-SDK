package edu.nitt.delta.interfaces

import edu.nitt.delta.models.Token

interface FetchTokenListener {
    fun onSuccess(token: Token)
    fun onFailure(exception: Exception)
}
