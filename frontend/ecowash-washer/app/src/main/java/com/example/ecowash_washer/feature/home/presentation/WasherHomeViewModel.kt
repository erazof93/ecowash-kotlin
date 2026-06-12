package com.example.ecowash_washer.feature.home.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowash_washer.core.network.RetrofitClient
import com.example.ecowash_washer.core.websocket.SocketManager
import com.example.ecowash_washer.feature.auth.data.datasource.AuthLocalDataSource
import com.example.ecowash_washer.feature.pedidos.data.repository.PedidosRepositoryImpl
import com.example.ecowash_washer.feature.pedidos.domain.model.Pedido
import com.example.ecowash_washer.feature.pedidos.domain.usecase.GetCercanosUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject

class WasherHomeViewModel(context: Context) : ViewModel() {

    private val repository = PedidosRepositoryImpl(RetrofitClient.createPedidosApiService(context))
    private val getCercanosUseCase = GetCercanosUseCase(repository)
    private val localDataSource = AuthLocalDataSource(context)

    private val _pedidosState = MutableStateFlow<PedidosState>(PedidosState.Idle)
    val pedidosState: StateFlow<PedidosState> = _pedidosState.asStateFlow()

    private val _pedidosEnVivo = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidosEnVivo: StateFlow<List<Pedido>> = _pedidosEnVivo.asStateFlow()

    private val _lavadorId = MutableStateFlow<String?>(null)

    private val _conexionState = MutableStateFlow<Boolean>(false)
    val conexionState: StateFlow<Boolean> = _conexionState.asStateFlow()

    init {
        viewModelScope.launch {
            val userId = localDataSource.getUserId().first()
            _lavadorId.value = userId
        }

        viewModelScope.launch {
            SocketManager.connectionStateFlow.collect { connected ->
                _conexionState.value = connected
            }
        }

        viewModelScope.launch {
            SocketManager.nuevoPedidoFlow.collect { data ->
                try {
                    val pedidoJson = data.optJSONObject("pedido")
                    if (pedidoJson != null) {
                        val nuevoPedido = Pedido(
                            id = pedidoJson.getString("id"),
                            clienteId = pedidoJson.getString("cliente_id"),
                            lavadorId = pedidoJson.optString("lavador_id", null),
                            precioServicioId = pedidoJson.getString("precio_servicio_id"),
                            estado = pedidoJson.getString("estado"),
                            direccionTexto = pedidoJson.getString("direccion_texto"),
                            precioTotal = pedidoJson.getDouble("precio_total"),
                            comisionCalculada = pedidoJson.getDouble("comision_calculada"),
                            lavadoIniciadoAt = pedidoJson.optString("lavado_iniciado_at", null),
                            creadoAt = pedidoJson.getString("creado_at"),
                            actualizadoAt = pedidoJson.getString("actualizado_at")
                        )

                        val coordenadas = data.optJSONObject("coordenadas_cliente")
                        val lat = coordenadas?.optDouble("latitud") ?: 0.0
                        val lng = coordenadas?.optDouble("longitud") ?: 0.0

                        val pedidoConCoordenadas = nuevoPedido.copy(
                            direccionTexto = "${nuevoPedido.direccionTexto} (Lat: %.4f, Lng: %.4f)".format(lat, lng)
                        )

                        val currentList = _pedidosEnVivo.value.toMutableList()
                        if (currentList.none { it.id == pedidoConCoordenadas.id }) {
                            currentList.add(0, pedidoConCoordenadas)
                            _pedidosEnVivo.value = currentList
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        SocketManager.connect()
    }

    fun cargarPedidosCercanos(latitud: Double, longitud: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            _pedidosState.value = PedidosState.Loading
            try {
                val pedidos = getCercanosUseCase(
                    latitud.toString(),
                    longitud.toString(),
                    "5"
                )
                _pedidosState.value = PedidosState.Success(pedidos)
                _pedidosEnVivo.value = pedidos
            } catch (e: Exception) {
                _pedidosState.value = PedidosState.Error("No se pudieron cargar pedidos cercanos")
            }
        }
    }

    fun enviarUbicacion(latitud: Double, longitud: Double) {
        val lavadorId = _lavadorId.value ?: return
        SocketManager.enviarUbicacion(lavadorId, latitud, longitud)
    }

    fun desconectar() {
        SocketManager.disconnect()
    }
}

sealed interface PedidosState {
    data object Idle : PedidosState
    data object Loading : PedidosState
    data class Success(val pedidos: List<Pedido>) : PedidosState
    data class Error(val message: String) : PedidosState
}
