package com.example.ecowash_client.core.network

import android.content.Context
import com.example.ecowash_client.feature.auth.data.datasource.AuthApiService
import com.example.ecowash_client.feature.auth.data.datasource.AuthLocalDataSource
import com.example.ecowash_client.feature.location.data.NominatimApiService
import com.example.ecowash_client.feature.pedidos.data.datasource.PedidosApiService
import com.example.ecowash_client.feature.precios.data.datasource.PreciosApiService
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val gson = GsonBuilder().create()
    private val contentType = "application/json".toMediaType()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private fun createClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun createAuthenticatedClient(context: Context): OkHttpClient {
        val localDataSource = AuthLocalDataSource(context)
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val token = runBlocking { localDataSource.getToken().first() }
                val requestBuilder = chain.request().newBuilder()
                if (!token.isNullOrEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun createAuthApiService(): AuthApiService {
        return Retrofit.Builder()
            .baseUrl(NetworkConstants.BASE_URL)
            .client(createClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AuthApiService::class.java)
    }

    fun createPreciosApiService(): PreciosApiService {
        return Retrofit.Builder()
            .baseUrl(NetworkConstants.BASE_URL)
            .client(createClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(PreciosApiService::class.java)
    }

    fun createPedidosApiService(context: Context): PedidosApiService {
        return Retrofit.Builder()
            .baseUrl(NetworkConstants.BASE_URL)
            .client(createAuthenticatedClient(context))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(PedidosApiService::class.java)
    }

    fun createNominatimApiService(): NominatimApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "EcoWashApp/1.0")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(NominatimApiService::class.java)
    }
}
