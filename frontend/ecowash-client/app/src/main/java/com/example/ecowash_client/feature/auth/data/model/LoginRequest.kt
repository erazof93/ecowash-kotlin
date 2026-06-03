package com.example.ecowash_client.feature.auth.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val correo: String,
    val contrasena: String
)
// ========= ENVIAMOS A NEST