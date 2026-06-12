package com.example.ecowash_washer.feature.auth.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val correo: String,
    val contrasena: String,
    val nombre_completo: String,
    val telefono: String
)
