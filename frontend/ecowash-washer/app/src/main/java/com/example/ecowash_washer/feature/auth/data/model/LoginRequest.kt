package com.example.ecowash_washer.feature.auth.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val correo: String,
    val contrasena: String
)
