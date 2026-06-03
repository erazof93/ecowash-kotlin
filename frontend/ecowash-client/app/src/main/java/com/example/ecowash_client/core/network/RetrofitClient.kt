package com.example.ecowash_client.core.network

import android.content.Context
import com.example.ecowash_client.feature.auth.data.datasource.AuthApiService
import com.example.ecowash_client.feature.auth.data.datasource.AuthLocalDataSource
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object RetrofitClient {

    private val json = Json { ignoreUnknownKeys = true }
    private val contentType = "application/json".toMediaType()

    fun createAuthApiService(): AuthApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        /*  val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()  */

        /* ================= Empieza =================*/

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            // 👇 AGREGADO: Interceptor para saltar la advertencia de VS Code en el Auth
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-Tunnel-Skip-AntiPhishing-Threshold", "true")
                    .build()
                chain.proceed(request)
            }
            .build()

        /* ================= termina =================*/

        return Retrofit.Builder()
            .baseUrl(NetworkConstants.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(AuthApiService::class.java)
    }

    // Este método creará el cliente para los demás servicios protegidos (ej. Pedidos)
    fun createAuthenticatedHttpClient(context: Context): OkHttpClient {
        val localDataSource = AuthLocalDataSource(context)
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                // runBlocking es una buena práctica aquí porque los interceptores de OkHttp
                // corren en hilos de fondo y necesitamos bloquear síncronamente la petición
                // hasta recuperar el token del DataStore.
                val token = runBlocking { localDataSource.getToken().first() }
                val requestBuilder = chain.request().newBuilder()

                // ===================== empieza simplemente aca se elimina =========
                requestBuilder.addHeader("X-Tunnel-Skip-AntiPhishing-Threshold", "true")
                // ======================= termina =========================

                if (!token.isNullOrEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }
            .build()
    }
}