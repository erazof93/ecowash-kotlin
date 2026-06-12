package com.example.ecowash_washer.feature.auth.presentation.register

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var nombreCompleto by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is RegisterState.Success) {
            onRegisterSuccess()
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Crear Cuenta Lavador",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = nombreCompleto,
            onValueChange = { nombreCompleto = it },
            label = { Text("Nombre Completo") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is RegisterState.Loading
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo Electronico") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is RegisterState.Loading
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Telefono / Celular") },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is RegisterState.Loading
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contrasena (Min. 6 caracteres)") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is RegisterState.Loading
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState is RegisterState.Error) {
            Text(
                text = (uiState as RegisterState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = { viewModel.register(correo, contrasena, nombreCompleto, telefono) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = uiState !is RegisterState.Loading
        ) {
            if (uiState is RegisterState.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("Registrarme")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onBackToLogin, enabled = uiState !is RegisterState.Loading) {
            Text("Ya tienes cuenta? Inicia Sesion")
        }
    }
}
