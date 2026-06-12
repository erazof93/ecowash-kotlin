package com.example.ecowash_washer.feature.pedidos.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecowash_washer.core.network.RetrofitClient
import com.example.ecowash_washer.feature.home.presentation.WasherPedidosViewModel
import com.example.ecowash_washer.feature.home.presentation.AccionState
import com.example.ecowash_washer.feature.pedidos.data.model.PedidoDto
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoActivoScreen(
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val apiService = remember { RetrofitClient.createPedidosApiService(context) }
    val viewModel = remember { WasherPedidosViewModel(context) }
    val accionState by viewModel.accionState.collectAsState()

    var pedido by remember { mutableStateOf<PedidoDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                val todos = apiService.getPedidos()
                val activo = todos.find {
                    it.estado in listOf("ACEPTADO", "EN_CAMINO", "EN_SITIO", "LAVANDO")
                }
                pedido = activo
            } catch (_: Exception) {}
            isLoading = false
            delay(5000)
        }
    }

    LaunchedEffect(accionState) {
        if (accionState is AccionState.Success) {
            viewModel.resetAccionState()
        }
    }

    val estado = pedido?.estado ?: "CARGANDO"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pedido activo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onVolver) { Text("Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (isLoading && pedido == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (pedido == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No tienes pedidos activos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Busca pedidos disponibles en el inicio",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EstadoPedidoCard(estado)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Direccion del cliente", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                pedido!!.direccion_texto,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (pedido!!.latitud != null && pedido!!.longitud != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val lat = pedido!!.latitud!!
                                val lng = pedido!!.longitud!!

                                OutlinedButton(
                                    onClick = {
                                        val uri = Uri.parse("waze://ul?ll=$lat,$lng&navigate=yes")
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        try {
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            val webUri = Uri.parse("https://www.waze.com/ul?ll=$lat,$lng&navigate=yes")
                                            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Waze", fontSize = 13.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val uri = Uri.parse("google.navigation:q=$lat,$lng&mode=d")
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        try {
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
                                            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Google Maps", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Precio total")
                            Text(
                                "S/ %.2f".format(pedido!!.precio_total),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tu comision")
                            Text("S/ %.2f".format(pedido!!.comision_calculada))
                        }
                    }
                }

                if (accionState is AccionState.Error) {
                    Text(
                        text = (accionState as AccionState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                when (estado) {
                    "ACEPTADO" -> {
                        Button(
                            onClick = { viewModel.marcarEnCamino(pedido!!.id) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = accionState !is AccionState.Loading,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (accionState is AccionState.Loading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Filled.ArrowForward, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ir en camino", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                    "EN_CAMINO" -> {
                        Button(
                            onClick = { viewModel.marcarEnSitio(pedido!!.id) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = accionState !is AccionState.Loading,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (accionState is AccionState.Loading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Filled.Place, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Llegue al sitio", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                    "EN_SITIO" -> {
                        Button(
                            onClick = { viewModel.iniciarLavado(pedido!!.id) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = accionState !is AccionState.Loading,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            if (accionState is AccionState.Loading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Filled.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Iniciar lavado", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                    "LAVANDO" -> {
                        Button(
                            onClick = { viewModel.finalizarPedido(pedido!!.id) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = accionState !is AccionState.Loading,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            if (accionState is AccionState.Loading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Filled.Done, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Finalizar lavado", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EstadoPedidoCard(estado: String) {
    val (icon, label, color) = when (estado) {
        "ACEPTADO" -> Triple(Icons.Filled.Check, "Pedido aceptado", MaterialTheme.colorScheme.primary)
        "EN_CAMINO" -> Triple(Icons.Filled.ArrowForward, "En camino al sitio", MaterialTheme.colorScheme.primary)
        "EN_SITIO" -> Triple(Icons.Filled.Place, "Llegaste al sitio", MaterialTheme.colorScheme.primary)
        "LAVANDO" -> Triple(Icons.Filled.Refresh, "Lavando vehiculo", MaterialTheme.colorScheme.secondary)
        else -> Triple(Icons.Filled.Info, "Estado: $estado", MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(36.dp))
            Column {
                Text(label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
                Text(
                    "Estado: $estado",
                    style = MaterialTheme.typography.bodySmall,
                    color = color.copy(alpha = 0.7f)
                )
            }
        }
    }
}
