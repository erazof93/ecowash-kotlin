package com.example.ecowash_client.feature.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowash_client.feature.auth.domain.usecase.LoginUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun login(correo: String, contrasena: String) {
        if (correo.isBlank() || contrasena.isBlank()) {
            _state.value = LoginState.Error("Los campos no pueden estar vacíos")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _state.value = LoginState.Loading
            try {
                val usuario = loginUseCase(correo, contrasena)
                _state.value = LoginState.Success(usuario)
            } catch (e: retrofit2.HttpException) {
                val errorMsg = when (e.code()) {
                    401 -> "Credenciales incorrectas"
                    404 -> "El usuario no existe"
                    else -> "Error en el servidor (${e.code()})"
                }
                _state.value = LoginState.Error(errorMsg)
            } catch (e: Exception) {
                _state.value = LoginState.Error("No se pudo conectar al servidor. Verifica tu red.")
            }
        }
    }

    fun resetState() {
        _state.value = LoginState.Idle
    }
}