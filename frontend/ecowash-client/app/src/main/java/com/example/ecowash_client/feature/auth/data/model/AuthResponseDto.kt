package com.example.ecowash_client.feature.auth.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponseDto(
    @SerializedName("token") val token: String?,
    @SerializedName("usuario") val usuario: UsuarioDto?
)

data class UsuarioDto(
    @SerializedName("id") val id: String?,
    @SerializedName("correo") val correo: String?,
    @SerializedName("nombre_completo") val nombre_completo: String?,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("foto_url") val foto_url: String?,
    @SerializedName("rol") val rol: String?
)
