package edu.nitt.delta

import edu.nitt.delta.models.*

class DAuth {

    lateinit var currentUser: User

    // to request for authorization use authorizationRequest members as query parameters
    fun requestAuthorization(authorizationRequest: AuthorizationRequest): AuthorizationResponse{
        TODO("make a request and return code and state")
    }

    //to request token use tokenRequest members as query parameters
    fun requestToken(tokenRequest: TokenRequest): Token{
        TODO("make a request and token will be obtained as response")
    }

    // signIn and store the current user
    fun signInWithClient(){
        TODO("To be implemented")
    }

    fun getLoggedUser(): User{
        TODO("return current User")
    }


    fun registerWithClient(){
        TODO("To be implemented")
    }

    // check if accountManager already has it
    fun checkIfUserExists(): Boolean{
        TODO("To be implemented")
    }

    // adds user in accountManager
    fun addUser(){
        TODO("To be implemented")
    }

    
}