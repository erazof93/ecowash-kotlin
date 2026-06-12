package com.example.ecowash_washer.feature.pedidos.data.datasource

import com.example.ecowash_washer.feature.pedidos.data.model.PedidoDto
import retrofit2.http.GET
import retrofit2.http.Query

interface PedidosApiService {

    @GET("pedidos/cercanos")
    suspend fun getPedidosCercanos(
        @Query("latitud") latitud: String,
        @Query("longitud") longitud: String,
        @Query("radioKm") radioKm: String
    ): List<PedidoDto>

    @GET("pedidos")
    suspend fun getPedidos(): List<PedidoDto>
}
