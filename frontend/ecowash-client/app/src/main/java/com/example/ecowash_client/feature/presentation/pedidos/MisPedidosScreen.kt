package com.example.ecowash_client.feature.presentation.pedidos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ecowash_client.feature.pedidos.domain.model.Pedido
import com.example.ecowash_client.feature.pedidos.presentation.PedidosListState
import com.example.ecowash_client.feature.pedidos.presentation.PedidosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisPedidosScreen(
    onVolver: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = remember { PedidosViewModel(context) }
    val state by viewModel.pedidosState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarPedidos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Mis Pedidos", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    TextButton(onClick = onVolver) { Text("Volver") }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.Filled.ExitToApp,
                            contentDescription = "Cerrar sesion",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        when (val currentState = state) {
            is PedidosListState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is PedidosListState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(currentState.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.cargarPedidos() },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Reintentar") }
                    }
                }
            }
            is PedidosListState.Success -> {
                val pedidosFiltrados = currentState.pedidos.filter {
                    it.estado == "FINALIZADO" || it.estado == "CANCELADO"
                }
                if (pedidosFiltrados.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No tienes pedidos finalizados o cancelados",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pedidosFiltrados) { pedido ->
                            PedidoCard(pedido)
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun PedidoCard(pedido: Pedido) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (pedido.estado) {
                        "PENDIENTE" -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        "FINALIZADO" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        "CANCELADO" -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = pedido.estado,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (pedido.estado) {
                            "PENDIENTE" -> MaterialTheme.colorScheme.tertiary
                            "CANCELADO" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }
                Text(
                    text = "S/ %.2f".format(pedido.precioTotal),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = pedido.direccionTexto,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = pedido.creadoAt.take(10),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
