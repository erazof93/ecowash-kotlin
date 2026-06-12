package com.example.ecowash_client.feature.presentation.pedidos

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecowash_client.core.network.RetrofitClient
import com.example.ecowash_client.feature.location.LocationRepository
import com.example.ecowash_client.feature.location.WebViewMap
import com.example.ecowash_client.feature.location.data.NominatimResult
import com.example.ecowash_client.feature.pedidos.presentation.CrearPedidoState
import com.example.ecowash_client.feature.pedidos.presentation.PedidosViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearPedidoScreen(
    precioServicioId: String,
    precio: Double,
    nombreServicio: String,
    nombreCategoria: String,
    onPedidoCreado: (pedidoId: String) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { PedidosViewModel(context) }
    val state by viewModel.crearState.collectAsState()
    val scope = rememberCoroutineScope()

    var direccionTexto by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf<Double?>(null) }
    var longitud by remember { mutableStateOf<Double?>(null) }
    var locationObtained by remember { mutableStateOf(false) }

    var suggestions by remember { mutableStateOf<List<NominatimResult>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    val nominatim = remember { RetrofitClient.createNominatimApiService() }

    var showConfirmDialog by remember { mutableStateOf(false) }

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
                    latitud = location.latitude
                    longitud = location.longitude
                    locationObtained = true
                }
            }
        }
    }

    LaunchedEffect(state) {
        val current = state
        if (current is CrearPedidoState.Success) {
            onPedidoCreado(current.pedido.id)
            viewModel.resetCrearState()
        }
    }

    // Dialogo de confirmacion
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Confirmar pedido", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Servicio: $nombreServicio")
                    Text("Categoria: $nombreCategoria")
                    Text(
                        "Direccion: ${direccionTexto.take(80)}${if (direccionTexto.length > 80) "..." else ""}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "S/ %.2f".format(kotlin.math.abs(precio)),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.crearPedido(precioServicioId, direccionTexto, latitud!!, longitud!!)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showConfirmDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirmar Pedido", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Resumen
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Resumen del servicio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Categoria: $nombreCategoria", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Servicio: $nombreServicio", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "S/ %.2f".format(kotlin.math.abs(precio)),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Direccion con autocomplete
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = direccionTexto,
                        onValueChange = { newValue ->
                            direccionTexto = newValue
                            latitud = null
                            longitud = null
                            locationObtained = false

                            searchJob?.cancel()
                            if (newValue.length >= 3) {
                                searchJob = scope.launch {
                                    delay(500)
                                    try {
                                        suggestions = nominatim.search(newValue)
                                        showSuggestions = suggestions.isNotEmpty()
                                    } catch (e: Exception) {
                                        suggestions = emptyList()
                                        showSuggestions = false
                                    }
                                }
                            } else {
                                suggestions = emptyList()
                                showSuggestions = false
                            }
                        },
                        label = { Text("Direccion del lavado") },
                        placeholder = { Text("Ej: Av. Larco 345, Miraflores") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        trailingIcon = {
                            if (direccionTexto.isNotEmpty()) {
                                IconButton(onClick = {
                                    direccionTexto = ""
                                    suggestions = emptyList()
                                    showSuggestions = false
                                    latitud = null
                                    longitud = null
                                    locationObtained = false
                                }) {
                                    Icon(Icons.Filled.Clear, "Limpiar")
                                }
                            }
                        }
                    )
                }

                if (showSuggestions && suggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 250.dp)
                        ) {
                            items(suggestions) { result ->
                                SuggestionItem(
                                    result = result,
                                    onClick = {
                                        direccionTexto = result.display_name
                                        latitud = result.lat.toDoubleOrNull()
                                        longitud = result.lon.toDoubleOrNull()
                                        locationObtained = latitud != null && longitud != null
                                        showSuggestions = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // GPS Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (locationObtained) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Ubicacion obtenida",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("O selecciona tu ubicacion GPS")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Activar GPS")
                        }
                    }
                }
            }

            // Mapa + Coordenadas
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column {
                    val mapLat = latitud ?: -12.0464
                    val mapLng = longitud ?: -77.0428
                    key(mapLat, mapLng) {
                        WebViewMap(
                            lat = mapLat,
                            lng = mapLng,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (locationObtained && latitud != null && longitud != null) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Ubicacion confirmada",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                "%.4f, %.4f".format(latitud, longitud),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Selecciona una ubicacion en el mapa o activa el GPS",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (state is CrearPedidoState.Error) {
                Text(
                    text = (state as CrearPedidoState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Boton confirmar
            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = state !is CrearPedidoState.Loading &&
                        latitud != null && longitud != null &&
                        direccionTexto.isNotBlank(),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (state is CrearPedidoState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Confirmar - S/ %.2f".format(kotlin.math.abs(precio)),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SuggestionItem(
    result: NominatimResult,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Filled.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = result.display_name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    )
}
