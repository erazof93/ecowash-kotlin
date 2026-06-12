package com.example.ecowash_client.feature.pedidos.domain.usecase

import com.example.ecowash_client.feature.pedidos.domain.model.Pedido
import com.example.ecowash_client.feature.pedidos.domain.repository.PedidosRepository

class CreatePedidoUseCase(private val repository: PedidosRepository) {
    suspend operator fun invoke(
        precioServicioId: String,
        direccionTexto: String,
        latitud: Double,
        longitud: Double
    ): Pedido {
        return repository.crearPedido(precioServicioId, direccionTexto, latitud, longitud)
    }
}
