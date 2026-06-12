package com.example.ecowash_washer.feature.auth.data.repository

import com.example.ecowash_washer.feature.auth.data.datasource.AuthApiService
import com.example.ecowash_washer.feature.auth.data.datasource.AuthLocalDataSource
import com.example.ecowash_washer.feature.auth.data.model.LoginRequest
import com.example.ecowash_washer.feature.auth.data.model.RegisterRequest
import com.example.ecowash_washer.feature.auth.domain.model.Usuario
import com.example.ecowash_washer.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val localDataSource: AuthLocalDataSource
) : AuthRepository {

    override suspend fun login(correo: String, contrasena: String): Usuario {
        val response = apiService.login(LoginRequest(correo, contrasena))
        localDataSource.saveToken(response.token)
        localDataSource.saveUserId(response.usuario.id)
        return Usuario(
            id = response.usuario.id,
            nombre = response.usuario.nombre,
            email = response.usuario.correo,
            telefono = response.usuario.telefono ?: "",
            rol = response.usuario.rol
        )
    }

    override fun getSavedToken(): Flow<String?> {
        return localDataSource.getToken()
    }

    override fun getUserId(): Flow<String?> {
        return localDataSource.getUserId()
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
        val response = apiService.register(
            RegisterRequest(correo, contrasena, nombreCompleto, telefono)
        )
        localDataSource.saveToken(response.token)
        localDataSource.saveUserId(response.usuario.id)
        return Usuario(
            id = response.usuario.id,
            nombre = response.usuario.nombre,
            email = response.usuario.correo,
            telefono = response.usuario.telefono ?: telefono,
            rol = response.usuario.rol
        )
    }
}
