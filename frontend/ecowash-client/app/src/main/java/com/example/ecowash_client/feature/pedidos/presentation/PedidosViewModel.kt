package com.example.ecowash_client.feature.pedidos.presentation

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowash_client.core.network.RetrofitClient
import com.example.ecowash_client.feature.pedidos.data.repository.PedidosRepositoryImpl
import com.example.ecowash_client.feature.pedidos.domain.model.Pedido
import com.example.ecowash_client.feature.pedidos.domain.usecase.CreatePedidoUseCase
import com.example.ecowash_client.feature.pedidos.domain.usecase.GetPedidosUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PedidosViewModel(context: Context) : ViewModel() {

    private val repository = PedidosRepositoryImpl(RetrofitClient.createPedidosApiService(context))
    private val createPedidoUseCase = CreatePedidoUseCase(repository)
    private val getPedidosUseCase = GetPedidosUseCase(repository)

    private val _crearState = MutableStateFlow<CrearPedidoState>(CrearPedidoState.Idle)
    val crearState: StateFlow<CrearPedidoState> = _crearState.asStateFlow()

    private val _pedidosState = MutableStateFlow<PedidosListState>(PedidosListState.Idle)
    val pedidosState: StateFlow<PedidosListState> = _pedidosState.asStateFlow()

    fun crearPedido(
        precioServicioId: String,
        direccionTexto: String,
        latitud: Double,
        longitud: Double
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _crearState.value = CrearPedidoState.Loading
            try {
                val pedido = createPedidoUseCase(precioServicioId, direccionTexto, latitud, longitud)
                _crearState.value = CrearPedidoState.Success(pedido)
            } catch (e: retrofit2.HttpException) {
                val body = e.response()?.errorBody()?.string() ?: ""
                Log.e("PedidosVM", "HTTP ${e.code()}: $body")
                _crearState.value = CrearPedidoState.Error("Server ${e.code()}: $body")
            } catch (e: Exception) {
                Log.e("PedidosVM", "Error", e)
                _crearState.value = CrearPedidoState.Error("${e.javaClass.simpleName}: ${e.message}")
            }
        }
    }

    fun cargarPedidos() {
        viewModelScope.launch(Dispatchers.IO) {
            _pedidosState.value = PedidosListState.Loading
            try {
                val pedidos = getPedidosUseCase()
                _pedidosState.value = PedidosListState.Success(pedidos)
            } catch (e: Exception) {
                _pedidosState.value = PedidosListState.Error("No se pudieron cargar los pedidos")
            }
        }
    }

    fun resetCrearState() {
        _crearState.value = CrearPedidoState.Idle
    }
}

sealed interface CrearPedidoState {
    data object Idle : CrearPedidoState
    data object Loading : CrearPedidoState
    data class Success(val pedido: Pedido) : CrearPedidoState
    data class Error(val message: String) : CrearPedidoState
}

sealed interface PedidosListState {
    data object Idle : PedidosListState
    data object Loading : PedidosListState
    data class Success(val pedidos: List<Pedido>) : PedidosListState
    data class Error(val message: String) : PedidosListState
}
