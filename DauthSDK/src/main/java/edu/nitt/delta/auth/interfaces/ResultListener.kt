package edu.nitt.delta.auth.interfaces

interface ResultListener<T> {
    fun onSuccess(value: T)
    fun onFailure(e: Exception)
}
