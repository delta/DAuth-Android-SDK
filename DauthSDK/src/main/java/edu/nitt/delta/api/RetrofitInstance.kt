package edu.nitt.delta.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal object RetrofitInstance {
    private const val BaseUrl = "https://auth.delta.nitt.edu"

    private val logger = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private val okHttpClient = OkHttpClient.Builder().addInterceptor(logger)

    private val retrofit = Retrofit.Builder().baseUrl(BaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient.build()).build()

    val api: DAuthApi = retrofit.create(DAuthApi::class.java)
}
