package com.example.ecowash_washer.feature.auth.presentation.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ecowash_washer.core.network.RetrofitClient
import com.example.ecowash_washer.feature.auth.data.datasource.AuthLocalDataSource
import com.example.ecowash_washer.feature.auth.data.repository.AuthRepositoryImpl
import com.example.ecowash_washer.feature.auth.domain.usecase.LoginUseCase
import com.example.ecowash_washer.feature.auth.domain.usecase.RegisterUseCase
import com.example.ecowash_washer.feature.auth.presentation.register.RegisterViewModel

class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val apiService = RetrofitClient.createAuthApiService()
        val localDataSource = AuthLocalDataSource(context)
        val repository = AuthRepositoryImpl(apiService, localDataSource)

        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            val loginUseCase = LoginUseCase(repository)
            return LoginViewModel(loginUseCase) as T
        }

        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            val registerUseCase = RegisterUseCase(repository)
            return RegisterViewModel(registerUseCase) as T
        }

        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}
