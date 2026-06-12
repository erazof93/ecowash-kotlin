package com.example.ecowash_client.feature.pedidos.data.model

import com.google.gson.annotations.SerializedName

data class CreatePedidoRequest(
    @SerializedName("precio_servicio_id") val precio_servicio_id: String,
    @SerializedName("direccion_texto") val direccion_texto: String,
    @SerializedName("latitud") val latitud: Double,
    @SerializedName("longitud") val longitud: Double
)

data class PedidoDto(
    @SerializedName("id") val id: String?,
    @SerializedName("cliente_id") val cliente_id: String?,
    @SerializedName("lavador_id") val lavador_id: String?,
    @SerializedName("precio_servicio_id") val precio_servicio_id: String?,
    @SerializedName("estado") val estado: String?,
    @SerializedName("direccion_texto") val direccion_texto: String?,
    @SerializedName("precio_total") val precio_total: Double?,
    @SerializedName("comision_calculada") val comision_calculada: Double?,
    @SerializedName("lavado_iniciado_at") val lavado_iniciado_at: String?,
    @SerializedName("creado_at") val creado_at: String?,
    @SerializedName("actualizado_at") val actualizado_at: String?
)
