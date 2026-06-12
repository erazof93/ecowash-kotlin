package com.example.ecowash_washer.feature.pedidos.data.repository

import com.example.ecowash_washer.feature.pedidos.data.datasource.PedidosApiService
import com.example.ecowash_washer.feature.pedidos.domain.model.Pedido
import com.example.ecowash_washer.feature.pedidos.domain.repository.PedidosRepository

class PedidosRepositoryImpl(
    private val apiService: PedidosApiService
) : PedidosRepository {

    override suspend fun getPedidosCercanos(
        latitud: String,
        longitud: String,
        radioKm: String
    ): List<Pedido> {
        return apiService.getPedidosCercanos(latitud, longitud, radioKm).map { it.toDomain() }
    }

    override suspend fun getPedidos(): List<Pedido> {
        return apiService.getPedidos().map { it.toDomain() }
    }

    override suspend fun aceptarPedido(pedidoId: String) {
        apiService.aceptarPedido(pedidoId)
    }

    override suspend fun cancelarPedido(pedidoId: String) {
        apiService.cancelarPedido(pedidoId)
    }

    override suspend fun marcarEnCamino(pedidoId: String) {
        apiService.marcarEnCamino(pedidoId)
    }

    override suspend fun marcarEnSitio(pedidoId: String) {
        apiService.marcarEnSitio(pedidoId)
    }

    override suspend fun iniciarLavado(pedidoId: String) {
        apiService.iniciarLavado(pedidoId)
    }

    override suspend fun finalizarPedido(pedidoId: String) {
        apiService.finalizarPedido(pedidoId)
    }

    private fun com.example.ecowash_washer.feature.pedidos.data.model.PedidoDto.toDomain() = Pedido(
        id = id,
        clienteId = cliente_id,
        lavadorId = lavador_id,
        precioServicioId = precio_servicio_id,
        estado = estado,
        direccionTexto = direccion_texto,
        precioTotal = precio_total,
        comisionCalculada = comision_calculada,
        lavadoIniciadoAt = lavado_iniciado_at,
        creadoAt = creado_at,
        actualizadoAt = actualizado_at
    )
}
