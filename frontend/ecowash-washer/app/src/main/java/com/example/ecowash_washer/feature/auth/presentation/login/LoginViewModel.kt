package com.example.ecowash_washer.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecowash_washer.feature.auth.domain.usecase.LoginUseCase
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
        val email = correo.trim()
        if (email.isBlank() || contrasena.isBlank()) {
            _state.value = LoginState.Error("Los campos no pueden estar vacios")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _state.value = LoginState.Loading
            try {
                val usuario = loginUseCase(email, contrasena)
                _state.value = LoginState.Success(usuario)
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: ""
                val errorMsg = try {
                    val json = org.json.JSONObject(errorBody)
                    json.optString("message", "Error: ${e.code()}")
                } catch (_: Exception) {
                    "Error: ${e.code()}"
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
