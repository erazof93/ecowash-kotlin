package com.example.ecowash_washer.feature.pedidos.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PedidoDto(
    val id: String,
    val cliente_id: String,
    val lavador_id: String? = null,
    val precio_servicio_id: String,
    val estado: String,
    val direccion_texto: String,
    val precio_total: Double,
    val comision_calculada: Double,
    val lavado_iniciado_at: String? = null,
    val creado_at: String,
    val actualizado_at: String,
    val latitud: Double? = null,
    val longitud: Double? = null
)
