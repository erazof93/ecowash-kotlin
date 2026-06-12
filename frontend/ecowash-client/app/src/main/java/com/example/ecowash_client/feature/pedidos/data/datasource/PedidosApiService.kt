package com.example.ecowash_client.feature.pedidos.data.datasource

import com.example.ecowash_client.feature.pedidos.data.model.CreatePedidoRequest
import com.example.ecowash_client.feature.pedidos.data.model.PedidoDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PedidosApiService {

    @POST("pedidos")
    suspend fun crearPedido(@Body request: CreatePedidoRequest): PedidoDto

    @GET("pedidos")
    suspend fun getPedidos(): List<PedidoDto>

    @PATCH("pedidos/{id}/cancelar")
    suspend fun cancelarPedido(@Path("id") id: String): Response<Unit>

    @GET("pedidos/cercanos")
    suspend fun getPedidosCercanos(
        @Query("latitud") latitud: String,
        @Query("longitud") longitud: String,
        @Query("radioKm") radioKm: String
    ): List<PedidoDto>
}
