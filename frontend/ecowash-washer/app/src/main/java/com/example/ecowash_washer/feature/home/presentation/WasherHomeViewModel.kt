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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
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

    private val _enLinea = MutableStateFlow(false)
    val enLinea: StateFlow<Boolean> = _enLinea.asStateFlow()

    private var _ultimaLat: Double? = null
    private var _ultimaLng: Double? = null

    private var pollingJob: Job? = null
    private var locationJob: Job? = null

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
                            _pedidosState.value = PedidosState.Success(currentList)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun toggleEnLinea(latitud: Double? = null, longitud: Double? = null) {
        val nuevoEstado = !_enLinea.value
        _enLinea.value = nuevoEstado

        if (nuevoEstado) {
            val lat = latitud ?: _ultimaLat
            val lng = longitud ?: _ultimaLng
            if (lat != null && lng != null) {
                _ultimaLat = lat
                _ultimaLng = lng
            }
            SocketManager.connect()
            if (lat != null && lng != null) {
                cargarPedidosCercanos(lat, lng)
                enviarUbicacion(lat, lng)
            }
            iniciarPolling()
            iniciarActualizacionUbicacion()
        } else {
            detenerPolling()
            detenerActualizacionUbicacion()
            _pedidosState.value = PedidosState.Idle
            _pedidosEnVivo.value = emptyList()
            SocketManager.disconnect()
        }
    }

    fun refresh() {
        val lat = _ultimaLat ?: return
        val lng = _ultimaLng ?: return
        cargarPedidosCercanos(lat, lng)
        enviarUbicacion(lat, lng)
    }

    private fun iniciarPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(30_000L)
                val lat = _ultimaLat ?: continue
                val lng = _ultimaLng ?: continue
                try {
                    var pedidos = getCercanosUseCase(
                        lat.toString(),
                        lng.toString(),
                        "10"
                    )

                    if (pedidos.isEmpty()) {
                        try {
                            val pendientes = repository.getPedidos()
                            pedidos = pendientes.filter { it.estado == "PENDIENTE" }
                        } catch (_: Exception) {}
                    }

                    _pedidosState.value = PedidosState.Success(pedidos)
                    _pedidosEnVivo.value = pedidos
                } catch (_: Exception) {}
            }
        }
    }

    private fun detenerPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun iniciarActualizacionUbicacion() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            while (isActive) {
                delay(60_000L)
                val lat = _ultimaLat ?: continue
                val lng = _ultimaLng ?: continue
                enviarUbicacion(lat, lng)
            }
        }
    }

    private fun detenerActualizacionUbicacion() {
        locationJob?.cancel()
        locationJob = null
    }

    fun cargarPedidosCercanos(latitud: Double, longitud: Double) {
        _ultimaLat = latitud
        _ultimaLng = longitud
        viewModelScope.launch(Dispatchers.IO) {
            if (_pedidosState.value !is PedidosState.Success) {
                _pedidosState.value = PedidosState.Loading
            }
            try {
                var pedidos = getCercanosUseCase(
                    latitud.toString(),
                    longitud.toString(),
                    "10"
                )

                if (pedidos.isEmpty()) {
                    try {
                        val pendientes = repository.getPedidos()
                        pedidos = pendientes.filter { it.estado == "PENDIENTE" }
                    } catch (_: Exception) {}
                }

                _pedidosState.value = PedidosState.Success(pedidos)
                _pedidosEnVivo.value = pedidos
            } catch (e: Exception) {
                try {
                    val pendientes = repository.getPedidos()
                    val pedidosFiltrados = pendientes.filter { it.estado == "PENDIENTE" }
                    _pedidosState.value = PedidosState.Success(pedidosFiltrados)
                    _pedidosEnVivo.value = pedidosFiltrados
                } catch (_: Exception) {
                    if (_pedidosState.value !is PedidosState.Success) {
                        _pedidosState.value = PedidosState.Error("No se pudieron cargar pedidos cercanos")
                    }
                }
            }
        }
    }

    fun enviarUbicacion(latitud: Double, longitud: Double) {
        val lavadorId = _lavadorId.value ?: return
        SocketManager.enviarUbicacion(lavadorId, latitud, longitud)
    }

    fun desconectar() {
        _enLinea.value = false
        detenerPolling()
        detenerActualizacionUbicacion()
        SocketManager.disconnect()
    }
}
