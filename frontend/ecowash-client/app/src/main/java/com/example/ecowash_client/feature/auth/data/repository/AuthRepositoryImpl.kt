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
        val response = apiService.login(LoginRequest(correo, contrasena))

        val token = response.token
            ?: throw IllegalStateException("El servidor no devolvio un token")
        val usuario = response.usuario
            ?: throw IllegalStateException("El servidor no devolvio datos del usuario")

        localDataSource.saveToken(token)

        return Usuario(
            id = usuario.id.orEmpty(),
            nombre = usuario.nombre_completo.orEmpty(),
            email = usuario.correo.orEmpty(),
            telefono = usuario.telefono.orEmpty()
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
        val response = apiService.register(RegisterRequest(correo, contrasena, nombreCompleto, telefono))

        val token = response.token
            ?: throw IllegalStateException("El servidor no devolvio un token")
        val usuario = response.usuario
            ?: throw IllegalStateException("El servidor no devolvio datos del usuario")

        localDataSource.saveToken(token)

        return Usuario(
            id = usuario.id.orEmpty(),
            nombre = usuario.nombre_completo.orEmpty(),
            email = usuario.correo.orEmpty(),
            telefono = usuario.telefono ?: telefono
        )
    }
}
