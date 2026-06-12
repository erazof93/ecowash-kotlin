package com.example.ecowash_client.feature.precios.domain.repository

import com.example.ecowash_client.feature.precios.domain.model.Categoria
import com.example.ecowash_client.feature.precios.domain.model.Precio

interface PreciosRepository {
    suspend fun getMatrizPrecios(): List<Precio>
    suspend fun getCategorias(): List<Categoria>
}
