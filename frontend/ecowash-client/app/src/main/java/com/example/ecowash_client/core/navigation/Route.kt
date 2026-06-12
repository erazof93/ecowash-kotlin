package com.example.ecowash_client.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    data object Splash : Route

    @Serializable
    data object Login : Route

    @Serializable
    data object Home : Route

    @Serializable
    data object Register : Route

    @Serializable
    data object Precios : Route

    @Serializable
    data class CrearPedido(
        val precioServicioId: String,
        val precio: Double,
        val nombreServicio: String,
        val nombreCategoria: String
    ) : Route

    @Serializable
    data object MisPedidos : Route
}
