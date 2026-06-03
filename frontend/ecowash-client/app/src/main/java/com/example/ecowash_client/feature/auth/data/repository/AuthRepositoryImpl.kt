package com.example.ecowash_client.feature.auth.data.repository

import com.example.ecowash_client.feature.auth.data.datasource.AuthApiService
import com.example.ecowash_client.feature.auth.data.datasource.AuthLocalDataSource
import com.example.ecowash_client.feature.auth.data.model.LoginRequest
import com.example.ecowash_client.feature.auth.domain.model.Usuario
import com.example.ecowash_client.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val localDataSource: AuthLocalDataSource
) : AuthRepository {

    override suspend fun login(correo: String, contrasena: String): Usuario {
        // 1. Enviamos el DTO con los campos exactos que tu NestJS ('correo' y 'contrasena') espera
        val response = apiService.login(LoginRequest(correo, contrasena))

        // 2. Persistimos el JWT recibido de NestJS en el DataStore local
        localDataSource.saveToken(response.token)

        // 3. Convertimos (mapeamos) el DTO de infraestructura a la Entidad Pura de dominio
        return Usuario(
            id = response.usuario.id,
            nombre = response.usuario.nombre_completo, // Mapea 'nombre_completo' de tu tabla al 'nombre' del dominio
            email = response.usuario.correo
        )
    }

    override fun getSavedToken(): Flow<String?> {
        return localDataSource.getToken()
    }

    override suspend fun logout() {
        localDataSource.clearAuth()
    }
}