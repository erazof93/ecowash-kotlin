package com.example.ecowash_client.feature.presentation.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    // Recolectamos el flujo del estado de forma segura para la UI de Compose
    val uiState by viewModel.state.collectAsState()

    // Evaluamos efectos secundarios (Navegación en caso de éxito)
    LaunchedEffect(uiState) {
        if (uiState is LoginState.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "EcoWash Cliente",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is LoginState.Loading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is LoginState.Loading
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState is LoginState.Error) {
            Text(
                text = (uiState as LoginState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = { viewModel.login(correo, contrasena) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = uiState !is LoginState.Loading
        ) {
            if (uiState is LoginState.Loading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Iniciar Sesión")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 👈 2. AGREGAMOS EL BOTÓN EN LA PARTE INFERIOR
        TextButton(
            onClick = onNavigateToRegister,
            enabled = uiState !is LoginState.Loading
        ) {
            Text("¿No tienes cuenta? Regístrate aquí")
        }
    }
}