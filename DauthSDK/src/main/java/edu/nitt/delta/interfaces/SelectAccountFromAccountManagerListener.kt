package edu.nitt.delta.interfaces

internal interface SelectAccountFromAccountManagerListener{
    fun onSelect(cookie: String)
    fun onCreateNewAccount()
    fun onUserDismiss()
}
