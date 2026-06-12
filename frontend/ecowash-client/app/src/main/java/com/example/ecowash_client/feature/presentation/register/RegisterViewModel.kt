package com.example.ecowash_client.feature.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowash_client.feature.auth.domain.usecase.RegisterUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun register(correo: String, contrasena: String, nombreCompleto: String, telefono: String) {
        if (correo.isBlank() || contrasena.isBlank() || nombreCompleto.isBlank() || telefono.isBlank()) {
            _state.value = RegisterState.Error("Todos los campos son obligatorios")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _state.value = RegisterState.Loading
            try {
                val usuario = registerUseCase(correo, contrasena, nombreCompleto, telefono)
                _state.value = RegisterState.Success(usuario)
            } catch (e: retrofit2.HttpException) {
                val body = e.response()?.errorBody()?.string() ?: ""
                _state.value = RegisterState.Error("Server ${e.code()}: $body")
            } catch (e: Exception) {
                _state.value = RegisterState.Error("${e.javaClass.simpleName}: ${e.message}")
            }
        }
    }

    fun resetState() {
        _state.value = RegisterState.Idle
    }
}