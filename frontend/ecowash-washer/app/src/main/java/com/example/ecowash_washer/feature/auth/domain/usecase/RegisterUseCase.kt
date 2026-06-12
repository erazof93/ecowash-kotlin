package com.example.ecowash_washer.feature.auth.domain.usecase

import com.example.ecowash_washer.feature.auth.domain.model.Usuario
import com.example.ecowash_washer.feature.auth.domain.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        correo: String,
        contrasena: String,
        nombreCompleto: String,
        telefono: String
    ): Usuario {
        return repository.registrar(correo, contrasena, nombreCompleto, telefono)
    }
}
