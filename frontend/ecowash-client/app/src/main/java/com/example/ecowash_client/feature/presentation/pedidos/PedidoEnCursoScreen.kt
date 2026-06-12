package com.example.ecowash_client.feature.presentation.pedidos

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecowash_client.core.network.RetrofitClient
import com.example.ecowash_client.feature.pedidos.data.model.PedidoDto
import com.example.ecowash_client.feature.pedidos.presentation.CrearPedidoState
import com.example.ecowash_client.feature.pedidos.presentation.PedidosViewModel
import com.example.ecowash_client.feature.pedidos.presentation.CancelarState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoEnCursoScreen(
    pedidoId: String,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiService = remember { RetrofitClient.createPedidosApiService(context) }
    val viewModel = remember { PedidosViewModel(context) }
    val cancelarState by viewModel.cancelarState.collectAsState()

    var pedido by remember { mutableStateOf<PedidoDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(cancelarState) {
        if (cancelarState is CancelarState.Success) {
            viewModel.resetCancelarState()
            onVolver()
        }
    }

    // Polling cada 5 segundos
    LaunchedEffect(pedidoId) {
        while (true) {
            try {
                val todos = apiService.getPedidos()
                pedido = todos.find { it.id == pedidoId }
                error = null
            } catch (e: Exception) {
                error = e.message
            }
            isLoading = false
            delay(5000)
        }
    }

    val estado = pedido?.estado ?: "CARGANDO"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pedido en curso", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onVolver) { Text("Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Indicador de estado animado
            EstadoCard(estado)

            // Detalles del pedido
            if (pedido != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Detalles del pedido", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        InfoRow("Direccion", pedido!!.direccion_texto ?: "")
                        InfoRow("Precio", "S/ %.2f".format(pedido!!.precio_total ?: 0.0))
                        InfoRow("Comision", "S/ %.2f".format(pedido!!.comision_calculada ?: 0.0))
                        InfoRow("Estado", pedido!!.estado ?: "")
                        InfoRow("Pedido", pedido!!.creado_at?.take(19)?.replace("T", " ") ?: "")
                    }
                }
            }

            // Lavador asignado
            if (pedido?.lavador_id != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Column {
                            Text(
                                "Lavador asignado",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                pedido!!.lavador_id ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (error != null) {
                Text("Error: $error", color = MaterialTheme.colorScheme.error)
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            if (estado == "PENDIENTE" && pedido != null) {
                Button(
                    onClick = { showCancelDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancelar pedido", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            title = { Text("Cancelar pedido", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estas seguro que deseas cancelar este pedido? Esta accion no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        viewModel.cancelarPedido(pedidoId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (cancelarState is CancelarState.Loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Si, cancelar")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCancelDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("No, volver")
                }
            }
        )
    }
}

@Composable
fun EstadoCard(estado: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val (icon, label, color) = when (estado) {
        "PENDIENTE" -> Triple(Icons.Filled.Info, "Esperando lavador...", MaterialTheme.colorScheme.tertiary)
        "ACEPTADO" -> Triple(Icons.Filled.Check, "Lavador acepto tu pedido", MaterialTheme.colorScheme.primary)
        "EN_CAMINO" -> Triple(Icons.Filled.ArrowForward, "El lavador va en camino", MaterialTheme.colorScheme.primary)
        "EN_SITIO" -> Triple(Icons.Filled.Place, "El lavador esta en el sitio", MaterialTheme.colorScheme.primary)
        "LAVANDO" -> Triple(Icons.Filled.Refresh, "Lavando tu vehiculo", MaterialTheme.colorScheme.secondary)
        "FINALIZADO" -> Triple(Icons.Filled.Done, "Lavado completado", MaterialTheme.colorScheme.primary)
        "CANCELADO" -> Triple(Icons.Filled.Close, "Pedido cancelado", MaterialTheme.colorScheme.error)
        else -> Triple(Icons.Filled.Info, "Cargando...", MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = if (estado == "PENDIENTE") alpha else 0.12f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(40.dp)
            )
            Column {
                Text(
                    label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    "Estado: $estado",
                    style = MaterialTheme.typography.bodySmall,
                    color = color.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}
