package com.example.ecowash_washer.feature.pedidos.domain.usecase

import com.example.ecowash_washer.feature.pedidos.domain.model.Pedido
import com.example.ecowash_washer.feature.pedidos.domain.repository.PedidosRepository

class GetCercanosUseCase(private val repository: PedidosRepository) {
    suspend operator fun invoke(latitud: String, longitud: String, radioKm: String): List<Pedido> {
        return repository.getPedidosCercanos(latitud, longitud, radioKm)
    }
}
