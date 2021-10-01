package edu.nitt.delta.api

import edu.nitt.delta.models.Token
import edu.nitt.delta.models.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST


interface DAuthApi {

    @GET(ApiRouteConstants.KEY_ROUTE)
    suspend fun getKey(): String

    @FormUrlEncoded
    @POST(ApiRouteConstants.TOKEN_ROUTE)
    fun getToken(@FieldMap map: Map<String, String>): Call<Token>

    @POST(ApiRouteConstants.USER_ROUTE)
    fun getUser(@Header("Authorization") accessToken: String): Call<User>

    @FormUrlEncoded
    @POST("/api/auth/login")
    fun getCookie(@Field("email")email: String, @Field("password")password:String): Call<ResponseBody>
}
