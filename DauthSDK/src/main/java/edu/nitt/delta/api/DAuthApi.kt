package edu.nitt.delta.api

import edu.nitt.delta.models.Token
import edu.nitt.delta.models.User
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface DAuthApi {

    @GET(ApiRouteConstants.KEY_ROUTE)
    suspend fun getKey(): String

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST(ApiRouteConstants.TOKEN_ROUTE)
    fun getToken(
        @Query("client_id") client_id: String,
        @Query("client_secret") client_secret: String,
        @Query("grant_type") grant_type: String,
        @Query("code") code: String,
        @Query("redirect_uri") redirect_uri: String
    ): Call<Token>

    @POST(ApiRouteConstants.USER_ROUTE)
    fun getUser(@Header("Authorization") accessToken: String): Call<User>
}
