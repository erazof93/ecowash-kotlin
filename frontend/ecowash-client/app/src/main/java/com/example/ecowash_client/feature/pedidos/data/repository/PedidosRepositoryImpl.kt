package com.example.ecowash_client.feature.pedidos.data.repository

import com.example.ecowash_client.feature.pedidos.data.datasource.PedidosApiService
import com.example.ecowash_client.feature.pedidos.data.model.CreatePedidoRequest
import com.example.ecowash_client.feature.pedidos.domain.model.Pedido
import com.example.ecowash_client.feature.pedidos.domain.repository.PedidosRepository

class PedidosRepositoryImpl(
    private val apiService: PedidosApiService
) : PedidosRepository {

    override suspend fun crearPedido(
        precioServicioId: String,
        direccionTexto: String,
        latitud: Double,
        longitud: Double
    ): Pedido {
        val dto = apiService.crearPedido(
            CreatePedidoRequest(precioServicioId, direccionTexto, latitud, longitud)
        )
        return dto.toDomain()
    }

    override suspend fun getPedidos(): List<Pedido> {
        return apiService.getPedidos().map { it.toDomain() }
    }

    override suspend fun getPedidosCercanos(
        latitud: String,
        longitud: String,
        radioKm: String
    ): List<Pedido> {
        return apiService.getPedidosCercanos(latitud, longitud, radioKm).map { it.toDomain() }
    }

    override suspend fun cancelarPedido(pedidoId: String) {
        apiService.cancelarPedido(pedidoId)
    }

    private fun com.example.ecowash_client.feature.pedidos.data.model.PedidoDto.toDomain() = Pedido(
        id = id.orEmpty(),
        clienteId = cliente_id.orEmpty(),
        lavadorId = lavador_id,
        precioServicioId = precio_servicio_id.orEmpty(),
        estado = estado.orEmpty(),
        direccionTexto = direccion_texto.orEmpty(),
        precioTotal = precio_total ?: 0.0,
        comisionCalculada = comision_calculada ?: 0.0,
        lavadoIniciadoAt = lavado_iniciado_at,
        creadoAt = creado_at.orEmpty(),
        actualizadoAt = actualizado_at.orEmpty()
    )
}
