package edu.nitt.delta.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton Object containing the Retrofit api instance
 */
internal object RetrofitInstance {
    /**
     * Base url of the Delta OAuth2 Service
     */
    private const val BaseUrl = "https://auth.delta.nitt.edu"

    /**
     * An [interceptor][HttpLoggingInterceptor] which logs request and response information
     */
    private val logger = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

    /**
     * [Http client][OkHttpClient] used by retrofit
     */
    private val okHttpClient = OkHttpClient.Builder().addInterceptor(logger)

    private val retrofit = Retrofit.Builder().baseUrl(BaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient.build())
        .build()

    /**
     * Value Instance of the [DAuthApi] interface
     */
    val api: DAuthApi = retrofit.create(DAuthApi::class.java)
}
