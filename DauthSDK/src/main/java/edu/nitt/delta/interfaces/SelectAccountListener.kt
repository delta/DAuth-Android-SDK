package edu.nitt.delta.interfaces

internal interface SelectAccountListener{
    fun onSuccess(cookie: String)
    fun onFailure()
    fun onUserDismiss()
}
