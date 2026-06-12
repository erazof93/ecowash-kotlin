package com.example.ecowash_client.feature.precios.data.datasource

import com.example.ecowash_client.feature.precios.data.model.CategoriaDto
import com.example.ecowash_client.feature.precios.data.model.PrecioServicioDto
import retrofit2.http.GET

interface PreciosApiService {

    @GET("precios")
    suspend fun getMatrizPrecios(): List<PrecioServicioDto>

    @GET("precios/categorias")
    suspend fun getCategorias(): List<CategoriaDto>
}
