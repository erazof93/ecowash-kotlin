package com.example.ecowash_client.feature.auth.data.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("correo") val correo: String,
    @SerializedName("contrasena") val contrasena: String,
    @SerializedName("nombre_completo") val nombre_completo: String,
    @SerializedName("telefono") val telefono: String
)
