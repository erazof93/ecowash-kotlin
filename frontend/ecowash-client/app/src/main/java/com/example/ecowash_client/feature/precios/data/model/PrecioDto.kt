package com.example.ecowash_client.feature.precios.data.model

import com.google.gson.annotations.SerializedName

data class PrecioServicioDto(
    @SerializedName("id") val id: String?,
    @SerializedName("categoria_id") val categoria_id: String?,
    @SerializedName("servicio_id") val servicio_id: String?,
    @SerializedName("precio") val precio: Double?,
    @SerializedName("descripcion_especifica") val descripcion_especifica: String?,
    @SerializedName("categoria") val categoria: CategoriaDto?,
    @SerializedName("servicio") val servicio: ServicioDto?
)

data class CategoriaDto(
    @SerializedName("id") val id: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("descripcion") val descripcion: String?
)

data class ServicioDto(
    @SerializedName("id") val id: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("descripcion") val descripcion: String?
)
