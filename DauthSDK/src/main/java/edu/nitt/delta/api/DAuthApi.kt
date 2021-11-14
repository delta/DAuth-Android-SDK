package edu.nitt.delta.api

import edu.nitt.delta.models.Token
import edu.nitt.delta.models.User
import edu.nitt.delta.models.jwks
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

/**
 * A [Retrofit][retrofit2.Retrofit] interface to communicate with the [Delta OAuth2 Service](https://auth.delta.nitt.edu/)
 */
internal interface DAuthApi {

    /**
     * Fetches the [Token] from the [token api route][ApiRoutes.Token]
     *
     * @param map named key/value pairs for a form-encoded request
     * @return [Call]<[Token]>
     */
    @FormUrlEncoded
    @POST(ApiRoutes.Token)
    fun getToken(@FieldMap map: Map<String, String>): Call<Token>

    /**
     * Fetches the [User data][User] from the [user api route][ApiRoutes.User]
     * @param accessToken access token required to retrieve user data
     * @return [Call][User]
     */
    @POST(ApiRoutes.User)
    fun getUser(@Header("Authorization") accessToken: String): Call<User>

    /**
     * Fetches the [ResponseBody] from the [login api route][ApiRoutes.Login]
     * @param email email of the user
     * @param password password of the user
     * @return [Call]<[ResponseBody]>
     */
    @FormUrlEncoded
    @POST(ApiRoutes.Login)
    fun getCookie(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<ResponseBody>

    /**
     * Fetches the [jwks] from the [key api route][ApiRoutes.Key]
     * @return [Call]<[jwks]>
     */
    @GET(ApiRoutes.Key)
    fun getJwks():Call<jwks>
}
