package com.example.ecowash_washer.feature.home.presentation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ecowash_washer.core.websocket.SocketManager
import com.example.ecowash_washer.feature.location.LocationRepository
import com.example.ecowash_washer.feature.pedidos.domain.model.Pedido
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WasherHomeScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { WasherHomeViewModel(context) }
    val pedidosState by viewModel.pedidosState.collectAsState()
    val pedidosEnVivo by viewModel.pedidosEnVivo.collectAsState()
    val conexionState by viewModel.conexionState.collectAsState()
    val scope = rememberCoroutineScope()

    val locationRepo = remember { LocationRepository(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            scope.launch {
                val location = locationRepo.getCurrentLocation()
                if (location != null) {
                    viewModel.cargarPedidosCercanos(location.latitude, location.longitude)
                    viewModel.enviarUbicacion(location.latitude, location.longitude)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        if (locationRepo.hasLocationPermission()) {
            scope.launch {
                val location = locationRepo.getCurrentLocation()
                if (location != null) {
                    viewModel.cargarPedidosCercanos(location.latitude, location.longitude)
                    viewModel.enviarUbicacion(location.latitude, location.longitude)
                }
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        onDispose {
            viewModel.desconectar()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EcoWash Lavador") },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (conexionState) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (conexionState) "En linea" else "Desconectado",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    TextButton(onClick = onLogout) {
                        Text("Salir")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (pedidosEnVivo.isEmpty()) {
                when (val state = pedidosState) {
                    is PedidosState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is PedidosState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(state.message, color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = {
                                    scope.launch {
                                        val location = locationRepo.getCurrentLocation()
                                        if (location != null) {
                                            viewModel.cargarPedidosCercanos(location.latitude, location.longitude)
                                        }
                                    }
                                }) {
                                    Text("Reintentar")
                                }
                            }
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Esperando pedidos cercanos...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Pedidos disponibles (${pedidosEnVivo.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(pedidosEnVivo) { pedido ->
                        PedidoCard(pedido)
                    }
                }
            }
        }
    }
}

@Composable
fun PedidoCard(pedido: Pedido) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pedido.estado,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (pedido.estado) {
                        "PENDIENTE" -> MaterialTheme.colorScheme.tertiary
                        "ACEPTADO" -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
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
                text = "Pedido: ${pedido.creadoAt.take(10)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
