package com.example.ecowash_client.feature.pedidos.domain.usecase

import com.example.ecowash_client.feature.pedidos.domain.model.Pedido
import com.example.ecowash_client.feature.pedidos.domain.repository.PedidosRepository

class GetPedidosUseCase(private val repository: PedidosRepository) {
    suspend operator fun invoke(): List<Pedido> {
        return repository.getPedidos()
    }
}
