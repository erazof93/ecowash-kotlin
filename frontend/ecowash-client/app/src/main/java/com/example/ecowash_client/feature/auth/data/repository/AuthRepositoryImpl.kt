package com.example.ecowash_client.feature.auth.data.repository

import com.example.ecowash_client.feature.auth.data.datasource.AuthApiService
import com.example.ecowash_client.feature.auth.data.datasource.AuthLocalDataSource
import com.example.ecowash_client.feature.auth.data.model.LoginRequest
import com.example.ecowash_client.feature.auth.data.model.RegisterRequest
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
            nombre = response.usuario.nombre_completo,
            email = response.usuario.correo,
            telefono = response.usuario.telefono ?: "" // 👈 Agregado aquí (si es null, pone texto vacío)
        )
    }

    override fun getSavedToken(): Flow<String?> {
        return localDataSource.getToken()
    }

    override suspend fun logout() {
        localDataSource.clearAuth()
    }

    override suspend fun registrar(
        correo: String,
        contrasena: String,
        nombreCompleto: String,
        telefono: String
    ): Usuario {
        // 1. Enviamos los datos estructurados a NestJS
        val response = apiService.register(RegisterRequest(correo, contrasena, nombreCompleto, telefono))

        // 2. Persistimos el JWT en DataStore (Auto-login)
        localDataSource.saveToken(response.token)

        // 3. Devolvemos la entidad pura con sus 4 campos obligatorios
        return Usuario(
            id = response.usuario.id,
            nombre = response.usuario.nombre_completo,
            email = response.usuario.correo,
            telefono = response.usuario.telefono ?: telefono // Usa el del backend o el ingresado por el usuario
        )
    }
}