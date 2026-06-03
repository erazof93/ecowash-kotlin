package com.example.ecowash_client.feature.auth.domain.usecase

import com.example.ecowash_client.feature.auth.domain.model.Usuario
import com.example.ecowash_client.feature.auth.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(correo: String, contrasena: String): Usuario {
        return repository.login(correo, contrasena)
    }
}