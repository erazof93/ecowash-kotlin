package com.example.ecowash_client.feature.precios.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowash_client.feature.precios.domain.model.Precio
import com.example.ecowash_client.feature.precios.domain.usecase.GetPreciosUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PreciosViewModel(
    private val getPreciosUseCase: GetPreciosUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<PreciosState>(PreciosState.Idle)
    val state: StateFlow<PreciosState> = _state.asStateFlow()

    init {
        cargarPrecios()
    }

    fun cargarPrecios() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = PreciosState.Loading
            try {
                val precios = getPreciosUseCase()
                _state.value = PreciosState.Success(precios)
            } catch (e: Exception) {
                _state.value = PreciosState.Error("No se pudieron cargar los precios")
            }
        }
    }
}

sealed interface PreciosState {
    data object Idle : PreciosState
    data object Loading : PreciosState
    data class Success(val precios: List<Precio>) : PreciosState
    data class Error(val message: String) : PreciosState
}
