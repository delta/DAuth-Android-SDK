package edu.nitt.delta.api

import edu.nitt.delta.models.Token
import edu.nitt.delta.models.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

internal interface DAuthApi {

    @FormUrlEncoded
    @POST(ApiRoutes.Token)
    fun getToken(@FieldMap map: Map<String, String>): Call<Token>

    @POST(ApiRoutes.User)
    fun getUser(@Header("Authorization") accessToken: String): Call<User>

    @FormUrlEncoded
    @POST("/api/auth/login")
    fun getCookie(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<ResponseBody>
}
