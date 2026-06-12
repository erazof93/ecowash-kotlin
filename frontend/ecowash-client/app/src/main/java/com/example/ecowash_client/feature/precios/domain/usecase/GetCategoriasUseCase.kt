package com.example.ecowash_client.feature.precios.domain.usecase

import com.example.ecowash_client.feature.precios.domain.model.Categoria
import com.example.ecowash_client.feature.precios.domain.repository.PreciosRepository

class GetCategoriasUseCase(private val repository: PreciosRepository) {
    suspend operator fun invoke(): List<Categoria> {
        return repository.getCategorias()
    }
}
