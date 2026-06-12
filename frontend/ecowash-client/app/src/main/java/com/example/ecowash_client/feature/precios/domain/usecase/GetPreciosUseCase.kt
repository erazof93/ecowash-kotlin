package com.example.ecowash_client.feature.precios.domain.usecase

import com.example.ecowash_client.feature.precios.domain.model.Precio
import com.example.ecowash_client.feature.precios.domain.repository.PreciosRepository

class GetPreciosUseCase(private val repository: PreciosRepository) {
    suspend operator fun invoke(): List<Precio> {
        return repository.getMatrizPrecios()
    }
}
