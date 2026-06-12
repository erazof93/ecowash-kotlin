package com.example.ecowash_washer.feature.home.presentation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecowash_washer.feature.location.LocationRepository
import com.example.ecowash_washer.feature.pedidos.domain.model.Pedido
import kotlinx.coroutines.launch

@Composable
fun AnimatedDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val dotCount = 3
    val dots = remember { mutableStateListOf(0f, 0f, 0f) }

    LaunchedEffect(Unit) {
        while (true) {
            for (i in 0 until dotCount) {
                dots[i] = 1f
                kotlinx.coroutines.delay(300L)
                dots[i] = 0.3f
            }
        }
    }

    Row {
        repeat(dotCount) { index ->
            Text(
                text = ".",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(dots.getOrElse(index) { 0.3f })
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WasherHomeScreen(
    onLogout: () -> Unit,
    onPedidoClick: (String) -> Unit,
    onPedidoActivoClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { WasherHomeViewModel(context) }
    val pedidosState by viewModel.pedidosState.collectAsState()
    val pedidosEnVivo by viewModel.pedidosEnVivo.collectAsState()
    val conexionState by viewModel.conexionState.collectAsState()
    val enLinea by viewModel.enLinea.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }

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
                    viewModel.toggleEnLinea(location.latitude, location.longitude)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.desconectar()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EcoWash Lavador", fontWeight = FontWeight.Bold) },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (enLinea) "En linea" else "Desconectado",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (enLinea) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = enLinea,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    if (locationRepo.hasLocationPermission()) {
                                        scope.launch {
                                            val location = locationRepo.getCurrentLocation()
                                            if (location != null) {
                                                viewModel.toggleEnLinea(location.latitude, location.longitude)
                                            } else {
                                                viewModel.toggleEnLinea()
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
                                } else {
                                    viewModel.toggleEnLinea()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                    TextButton(onClick = onLogout) {
                        Text("Salir")
                    }
                }
            )
        }
    ) { padding ->
        if (!enLinea) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Desconectado",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Activa el switch para recibir pedidos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Disponibles") },
                        icon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            onPedidoActivoClick()
                        },
                        text = { Text("Mi pedido activo") },
                        icon = { Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                if (selectedTab == 0) {
                    when (val state = pedidosState) {
                        is PedidosState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Buscando pedidos cercanos", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
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
                                        Icon(Icons.Filled.Refresh, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Reintentar")
                                    }
                                }
                            }
                        }
                        else -> {
                            val pedidos = if (pedidosEnVivo.isNotEmpty()) pedidosEnVivo else {
                                when (state) {
                                    is PedidosState.Success -> state.pedidos
                                    else -> emptyList()
                                }
                            }
                            if (pedidos.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Search,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(
                                                "Esperando pedidos cercanos",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            AnimatedDots()
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Mantente en linea para recibir nuevos pedidos",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                        Spacer(modifier = Modifier.height(20.dp))
                                        OutlinedButton(
                                            onClick = {
                                                scope.launch {
                                                    val location = locationRepo.getCurrentLocation()
                                                    if (location != null) {
                                                        viewModel.cargarPedidosCercanos(location.latitude, location.longitude)
                                                    }
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Actualizar")
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
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Pedidos disponibles (${pedidos.size})",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        val location = locationRepo.getCurrentLocation()
                                                        if (location != null) {
                                                            viewModel.cargarPedidosCercanos(location.latitude, location.longitude)
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Filled.Refresh,
                                                    contentDescription = "Actualizar",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                    items(pedidos) { pedido ->
                                        DisponiblePedidoCard(
                                            pedido = pedido,
                                            onClick = { onPedidoClick(pedido.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DisponiblePedidoCard(pedido: Pedido, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = pedido.estado,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = pedido.direccionTexto,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = pedido.creadoAt.take(10),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
