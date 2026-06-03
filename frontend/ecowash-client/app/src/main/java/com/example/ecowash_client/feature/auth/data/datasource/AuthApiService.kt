package com.example.ecowash_client.feature.auth.data.datasource


import com.example.ecowash_client.feature.auth.data.model.AuthResponseDto
import com.example.ecowash_client.feature.auth.data.model.LoginRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): AuthResponseDto
}