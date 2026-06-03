package com.example.ecowash_client.feature.presentation.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ecowash_client.core.network.RetrofitClient
import com.example.ecowash_client.feature.auth.data.datasource.AuthLocalDataSource
import com.example.ecowash_client.feature.auth.data.repository.AuthRepositoryImpl
import com.example.ecowash_client.feature.auth.domain.usecase.LoginUseCase

class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            // 1. Inicializamos las fuentes de datos (DataSources)
            val apiService = RetrofitClient.createAuthApiService()
            val localDataSource = AuthLocalDataSource(context)

            // 2. Acoplamos en el Repositorio
            val repository = AuthRepositoryImpl(apiService, localDataSource)

            // 3. Creamos el Caso de Uso de Dominio
            val loginUseCase = LoginUseCase(repository)

            // 4. Retornamos el ViewModel inyectado
            return LoginViewModel(loginUseCase) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
    }
}