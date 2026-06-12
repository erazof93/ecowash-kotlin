package com.example.ecowash_washer.feature.auth.domain.repository

import com.example.ecowash_washer.feature.auth.domain.model.Usuario
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, username: String): Usuario
    suspend fun registrar(correo: String, contrasena: String, nombreCompleto: String, telefono: String): Usuario
    fun getSavedToken(): Flow<String?>
    fun getUserId(): Flow<String?>
    suspend fun logout()
}
