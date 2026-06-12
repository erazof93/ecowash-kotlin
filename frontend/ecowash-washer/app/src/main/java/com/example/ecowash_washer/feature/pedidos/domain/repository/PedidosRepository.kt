package com.example.ecowash_washer.feature.pedidos.domain.repository

import com.example.ecowash_washer.feature.pedidos.domain.model.Pedido

interface PedidosRepository {
    suspend fun getPedidosCercanos(latitud: String, longitud: String, radioKm: String): List<Pedido>
    suspend fun getPedidos(): List<Pedido>
    suspend fun aceptarPedido(pedidoId: String)
    suspend fun cancelarPedido(pedidoId: String)
    suspend fun marcarEnCamino(pedidoId: String)
    suspend fun marcarEnSitio(pedidoId: String)
    suspend fun iniciarLavado(pedidoId: String)
    suspend fun finalizarPedido(pedidoId: String)
}
