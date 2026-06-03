package com.example.ecowash_client.feature.presentation.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigationToLogin: () -> Unit,
    onNavigationToHome: () -> Unit
) {
    LaunchedEffect(key1 = true) {
        delay(2000) // Simulación de carga/lectura de token
        onNavigationToLogin() // Por defecto vamos al login
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "EcoWash Cliente Loading...")
    }
}