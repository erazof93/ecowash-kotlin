package com.example.ecowash_washer.feature.auth.domain.model

data class Usuario(
    val id: String,
    val nombre: String,
    val email: String,
    val telefono: String,
    val rol: String
)
