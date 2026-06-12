package com.example.ecowash_washer.feature.home.presentation

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowash_washer.core.network.RetrofitClient
import com.example.ecowash_washer.core.websocket.SocketManager
import com.example.ecowash_washer.feature.auth.data.datasource.AuthLocalDataSource
import com.example.ecowash_washer.feature.pedidos.data.repository.PedidosRepositoryImpl
import com.example.ecowash_washer.feature.pedidos.domain.model.Pedido
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WasherPedidosViewModel(context: Context) : ViewModel() {

    private val repository = PedidosRepositoryImpl(RetrofitClient.createPedidosApiService(context))
    private val localDataSource = AuthLocalDataSource(context)

    private val _pedidosState = MutableStateFlow<PedidosState>(PedidosState.Idle)
    val pedidosState: StateFlow<PedidosState> = _pedidosState.asStateFlow()

    private val _pedidoActivoState = MutableStateFlow<PedidoActivoState>(PedidoActivoState.Idle)
    val pedidoActivoState: StateFlow<PedidoActivoState> = _pedidoActivoState.asStateFlow()

    private val _accionState = MutableStateFlow<AccionState>(AccionState.Idle)
    val accionState: StateFlow<AccionState> = _accionState.asStateFlow()

    private val _lavadorId = MutableStateFlow<String?>(null)

    val pedidosEnVivo = MutableStateFlow<List<Pedido>>(emptyList())

    init {
        viewModelScope.launch {
            val userId = localDataSource.getUserId().first()
            _lavadorId.value = userId
        }
    }

    fun cargarPedidos() {
        viewModelScope.launch(Dispatchers.IO) {
            _pedidosState.value = PedidosState.Loading
            try {
                val pedidos = repository.getPedidos()
                val activos = pedidos.filter {
                    it.estado in listOf("ACEPTADO", "EN_CAMINO", "EN_SITIO", "LAVANDO")
                }
                _pedidosState.value = PedidosState.Success(activos)
                if (activos.isNotEmpty()) {
                    _pedidoActivoState.value = PedidoActivoState.ConPedido(activos.first())
                } else {
                    _pedidoActivoState.value = PedidoActivoState.SinPedido
                }
            } catch (e: Exception) {
                Log.e("WasherPedidosVM", "Error cargando pedidos", e)
                _pedidosState.value = PedidosState.Error("No se pudieron cargar los pedidos")
            }
        }
    }

    fun aceptarPedido(pedidoId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _accionState.value = AccionState.Loading
            try {
                repository.aceptarPedido(pedidoId)
                _accionState.value = AccionState.Success("Pedido aceptado")
                cargarPedidos()
            } catch (e: Exception) {
                Log.e("WasherPedidosVM", "Error aceptando pedido", e)
                _accionState.value = AccionState.Error("No se pudo aceptar el pedido")
            }
        }
    }

    fun cancelarPedido(pedidoId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _accionState.value = AccionState.Loading
            try {
                repository.cancelarPedido(pedidoId)
                _accionState.value = AccionState.Success("Pedido cancelado")
                cargarPedidos()
            } catch (e: Exception) {
                Log.e("WasherPedidosVM", "Error cancelando pedido", e)
                _accionState.value = AccionState.Error("No se pudo cancelar el pedido")
            }
        }
    }

    fun marcarEnCamino(pedidoId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _accionState.value = AccionState.Loading
            try {
                repository.marcarEnCamino(pedidoId)
                _accionState.value = AccionState.Success("En camino")
                cargarPedidos()
            } catch (e: Exception) {
                _accionState.value = AccionState.Error("No se pudo actualizar")
            }
        }
    }

    fun marcarEnSitio(pedidoId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _accionState.value = AccionState.Loading
            try {
                repository.marcarEnSitio(pedidoId)
                _accionState.value = AccionState.Success("Llegaste al sitio")
                cargarPedidos()
            } catch (e: Exception) {
                _accionState.value = AccionState.Error("No se pudo actualizar")
            }
        }
    }

    fun iniciarLavado(pedidoId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _accionState.value = AccionState.Loading
            try {
                repository.iniciarLavado(pedidoId)
                _accionState.value = AccionState.Success("Lavado iniciado")
                cargarPedidos()
            } catch (e: Exception) {
                _accionState.value = AccionState.Error("No se pudo iniciar el lavado")
            }
        }
    }

    fun finalizarPedido(pedidoId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _accionState.value = AccionState.Loading
            try {
                repository.finalizarPedido(pedidoId)
                _accionState.value = AccionState.Success("Pedido finalizado")
                cargarPedidos()
            } catch (e: Exception) {
                _accionState.value = AccionState.Error("No se pudo finalizar el pedido")
            }
        }
    }

    fun resetAccionState() {
        _accionState.value = AccionState.Idle
    }
}

sealed interface PedidosState {
    data object Idle : PedidosState
    data object Loading : PedidosState
    data class Success(val pedidos: List<Pedido>) : PedidosState
    data class Error(val message: String) : PedidosState
}

sealed interface PedidoActivoState {
    data object Idle : PedidoActivoState
    data object SinPedido : PedidoActivoState
    data class ConPedido(val pedido: Pedido) : PedidoActivoState
}

sealed interface AccionState {
    data object Idle : AccionState
    data object Loading : AccionState
    data class Success(val message: String) : AccionState
    data class Error(val message: String) : AccionState
}
