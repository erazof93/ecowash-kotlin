package com.example.ecowash_client.feature.precios.domain.model

data class Precio(
    val id: String,
    val categoriaId: String,
    val servicioId: String,
    val precio: Double,
    val descripcion: String?,
    val categoriaNombre: String,
    val servicioNombre: String
)
