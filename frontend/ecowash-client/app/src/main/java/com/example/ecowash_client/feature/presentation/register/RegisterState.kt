package com.example.ecowash_client.feature.presentation.register

import com.example.ecowash_client.feature.auth.domain.model.Usuario

sealed interface RegisterState {
    data object Idle : RegisterState
    data object Loading : RegisterState
    data class Success(val usuario: Usuario) : RegisterState
    data class Error(val message: String) : RegisterState
}