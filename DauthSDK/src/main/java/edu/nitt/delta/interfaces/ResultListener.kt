package edu.nitt.delta.interfaces

interface ResultListener<T> {
    fun onSuccess(value: T)
    fun onFailure(e: Exception)
}
