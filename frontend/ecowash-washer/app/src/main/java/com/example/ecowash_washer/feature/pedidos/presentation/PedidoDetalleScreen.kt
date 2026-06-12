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
fun PedidoDetalleScreen(
    pedidoId: String,
    onVolver: () -> Unit,
    onPedidoAceptado: () -> Unit
) {
    val context = LocalContext.current
    val apiService = remember { RetrofitClient.createPedidosApiService(context) }
    val viewModel = remember { WasherPedidosViewModel(context) }
    val accionState by viewModel.accionState.collectAsState()

    var pedido by remember { mutableStateOf<PedidoDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(pedidoId) {
        try {
            val todos = apiService.getPedidos()
            pedido = todos.find { it.id == pedidoId }
        } catch (_: Exception) {}
        isLoading = false
    }

    LaunchedEffect(accionState) {
        if (accionState is AccionState.Success) {
            viewModel.resetAccionState()
            onPedidoAceptado()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del pedido", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onVolver) { Text("Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (isLoading) {
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
                Text("Pedido no encontrado", color = MaterialTheme.colorScheme.error)
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Direccion del cliente",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                pedido!!.direccion_texto,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (pedido!!.latitud != null && pedido!!.longitud != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Abrir en navegacion",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
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
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Waze")
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
                                    Icon(
                                        Icons.Filled.Place,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Google Maps")
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
                            Text("Precio", fontWeight = FontWeight.Bold)
                            Text(
                                "S/ %.2f".format(pedido!!.precio_total),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tu comision", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "S/ %.2f".format(pedido!!.comision_calculada),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Pedido", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                pedido!!.creado_at.take(19).replace("T", " "),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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

                Button(
                    onClick = { viewModel.aceptarPedido(pedidoId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = accionState !is AccionState.Loading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (accionState is AccionState.Loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Aceptar pedido", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
