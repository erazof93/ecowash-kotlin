package com.example.ecowash_washer.feature.pedidos.domain.model

data class Pedido(
    val id: String,
    val clienteId: String,
    val lavadorId: String?,
    val precioServicioId: String,
    val estado: String,
    val direccionTexto: String,
    val precioTotal: Double,
    val comisionCalculada: Double,
    val lavadoIniciadoAt: String?,
    val creadoAt: String,
    val actualizadoAt: String
)
