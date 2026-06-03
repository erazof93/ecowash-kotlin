package com.example.ecowash_client.feature.auth.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseDto(
    val token: String,
    val usuario: UsuarioDto
)

@Serializable
data class UsuarioDto(
    val id: String,
    val correo: String,
    val nombre_completo: String,
    val telefono: String? = null,
    val foto_url: String? = null,
    val rol: String
)

// =========== Lo que me devuelve NestJS