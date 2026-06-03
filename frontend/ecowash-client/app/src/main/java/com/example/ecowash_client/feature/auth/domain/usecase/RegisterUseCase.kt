package com.example.ecowash_client.feature.auth.domain.usecase

import com.example.ecowash_client.feature.auth.domain.model.Usuario
import com.example.ecowash_client.feature.auth.domain.repository.AuthRepository

class RegisterUseCase(private val repository: AuthRepository) {

    // 👈 Agregamos 'telefono: String' a la firma del caso de uso
    suspend operator fun invoke(
        correo: String,
        contrasena: String,
        nombreCompleto: String,
        telefono: String
    ): Usuario {
        // Ahora sí, pasamos todas las variables requeridas al repositorio
        return repository.registrar(correo, contrasena, nombreCompleto, telefono)
    }
}