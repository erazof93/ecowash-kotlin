package com.example.ecowash_client.feature.presentation.splash

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ecowash_client.feature.auth.data.datasource.AuthLocalDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

@Composable
fun SplashScreen(
    onNavigationToLogin: () -> Unit,
    onNavigationToHome: () -> Unit,
    context: Context
) {
    LaunchedEffect(key1 = true) {
        delay(1500)
        val localDataSource = AuthLocalDataSource(context)
        val token = runBlocking { localDataSource.getToken().first() }
        if (token.isNullOrEmpty()) {
            onNavigationToLogin()
        } else {
            onNavigationToHome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "ECOWASH",
            fontSize = 42.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 8.sp
        )
    }
}
