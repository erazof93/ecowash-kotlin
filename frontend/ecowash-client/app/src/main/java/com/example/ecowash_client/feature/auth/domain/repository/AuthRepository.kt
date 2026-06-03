package com.example.ecowash_client.feature.auth.domain.repository

import com.example.ecowash_client.feature.auth.domain.model.Usuario
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, username: String): Usuario
    fun getSavedToken(): Flow<String?>
    suspend fun logout()
}