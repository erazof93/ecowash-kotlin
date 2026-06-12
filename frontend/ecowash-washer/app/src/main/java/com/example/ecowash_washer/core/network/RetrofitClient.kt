package com.example.ecowash_washer.core.network

import android.content.Context
import com.example.ecowash_washer.feature.auth.data.datasource.AuthApiService
import com.example.ecowash_washer.feature.auth.data.datasource.AuthLocalDataSource
import com.example.ecowash_washer.feature.pedidos.data.datasource.PedidosApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object RetrofitClient {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val contentType = "application/json".toMediaType()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun createAuthApiService(): AuthApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(NetworkConstants.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(AuthApiService::class.java)
    }

    fun createPedidosApiService(context: Context): PedidosApiService {
        val localDataSource = AuthLocalDataSource(context)
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val token = runBlocking { localDataSource.getToken().first() }
                val requestBuilder = chain.request().newBuilder()
                if (!token.isNullOrEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(NetworkConstants.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(PedidosApiService::class.java)
    }
}
