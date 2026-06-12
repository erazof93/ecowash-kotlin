package com.example.ecowash_client.feature.precios.data.repository

import com.example.ecowash_client.feature.precios.data.datasource.PreciosApiService
import com.example.ecowash_client.feature.precios.domain.model.Categoria
import com.example.ecowash_client.feature.precios.domain.model.Precio
import com.example.ecowash_client.feature.precios.domain.repository.PreciosRepository

class PreciosRepositoryImpl(
    private val apiService: PreciosApiService
) : PreciosRepository {

    override suspend fun getMatrizPrecios(): List<Precio> {
        return apiService.getMatrizPrecios().map { dto ->
            Precio(
                id = dto.id.orEmpty(),
                categoriaId = dto.categoria_id.orEmpty(),
                servicioId = dto.servicio_id.orEmpty(),
                precio = dto.precio ?: 0.0,
                descripcion = dto.descripcion_especifica,
                categoriaNombre = dto.categoria?.nombre.orEmpty(),
                servicioNombre = dto.servicio?.nombre.orEmpty()
            )
        }
    }

    override suspend fun getCategorias(): List<Categoria> {
        return apiService.getCategorias().map { dto ->
            Categoria(
                id = dto.id.orEmpty(),
                nombre = dto.nombre.orEmpty(),
                descripcion = dto.descripcion
            )
        }
    }
}
