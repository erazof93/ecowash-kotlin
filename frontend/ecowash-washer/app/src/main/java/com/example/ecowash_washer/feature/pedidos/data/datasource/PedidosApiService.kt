package com.example.ecowash_washer.feature.pedidos.data.datasource

import com.example.ecowash_washer.feature.pedidos.data.model.PedidoDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
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

    @PATCH("pedidos/{id}/aceptar")
    suspend fun aceptarPedido(@Path("id") id: String): Response<Unit>

    @PATCH("pedidos/{id}/cancelar")
    suspend fun cancelarPedido(@Path("id") id: String): Response<Unit>

    @PATCH("pedidos/{id}/en-camino")
    suspend fun marcarEnCamino(@Path("id") id: String): Response<Unit>

    @PATCH("pedidos/{id}/en-sitio")
    suspend fun marcarEnSitio(@Path("id") id: String): Response<Unit>

    @PATCH("pedidos/{id}/iniciar-lavado")
    suspend fun iniciarLavado(@Path("id") id: String): Response<Unit>

    @PATCH("pedidos/{id}/finalizar")
    suspend fun finalizarPedido(@Path("id") id: String): Response<Unit>
}
