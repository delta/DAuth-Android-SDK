package edu.nitt.delta.api

import edu.nitt.delta.models.AuthorizationResponse
import edu.nitt.delta.models.Token
import edu.nitt.delta.models.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query


interface DAuthApi {


    @GET("/authorize")
    fun authorize(@Query("client_id") client_id:String,@Query("redirect_uri") redirect_uri:String
                  ,@Query("response_type") response_type:String,@Query("grant_type") grant_type:String
                   ,@Query("state") state:String,@Query("scope") scope:String,@Query("nonce") nonce:String): AuthorizationResponse

    @POST("/api/oauth/token")
    fun getToken(@Query("client_id")client_id:String,@Query("client_secret")client_secret:String,@Query("grant_type")grant_type:String
                 ,@Query("code") code:String,@Query("redirect_uri")redirect_uri:String):Token

    @GET("/api/oauth/oidc/key")
    fun getKey():String

    @POST("/api/resources/user")
    fun getUser(@Header("Authorization") accessToken:String):User
}