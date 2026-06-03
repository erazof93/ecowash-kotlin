package com.example.ecowash_client.feature.presentation.login

import com.example.ecowash_client.feature.auth.domain.model.Usuario

sealed interface LoginState {
    data object Idle : LoginState
    data object Loading : LoginState
    data class Success(val usuario: Usuario) : LoginState
    data class Error(val message: String) : LoginState
}