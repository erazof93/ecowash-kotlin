package com.example.ecowash_washer.core.navigation

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
    data class PedidoDetalle(val pedidoId: String) : Route

    @Serializable
    data object PedidoActivo : Route
}
