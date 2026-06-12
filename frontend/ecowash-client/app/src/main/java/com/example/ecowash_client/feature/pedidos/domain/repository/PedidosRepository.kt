package com.example.ecowash_client.feature.pedidos.domain.repository

import com.example.ecowash_client.feature.pedidos.domain.model.Pedido

interface PedidosRepository {
    suspend fun crearPedido(precioServicioId: String, direccionTexto: String, latitud: Double, longitud: Double): Pedido
    suspend fun getPedidos(): List<Pedido>
    suspend fun getPedidosCercanos(latitud: String, longitud: String, radioKm: String): List<Pedido>
    suspend fun cancelarPedido(pedidoId: String)
}
