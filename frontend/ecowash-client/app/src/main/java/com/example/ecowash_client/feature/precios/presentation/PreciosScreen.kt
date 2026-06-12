package com.example.ecowash_client.feature.precios.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreciosScreen(
    onSeleccionarPrecio: (precioId: String, precio: Double, nombreServicio: String, nombreCategoria: String) -> Unit,
    onVolver: () -> Unit
) {
    val viewModel = remember {
        PreciosViewModel(
            com.example.ecowash_client.feature.precios.domain.usecase.GetPreciosUseCase(
                com.example.ecowash_client.feature.precios.data.repository.PreciosRepositoryImpl(
                    com.example.ecowash_client.core.network.RetrofitClient.createPreciosApiService()
                )
            )
        )
    }
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Servicios", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onVolver) { Text("Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        when (val currentState = state) {
            is PreciosState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is PreciosState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(currentState.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.cargarPrecios() },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Reintentar") }
                    }
                }
            }
            is PreciosState.Success -> {
                val preciosAgrupados = currentState.precios.groupBy { it.categoriaNombre }
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    preciosAgrupados.forEach { (categoria, precios) ->
                        item {
                            Text(
                                text = categoria.uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }
                        items(precios) { precio ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    onSeleccionarPrecio(
                                        precio.id,
                                        precio.precio,
                                        precio.servicioNombre,
                                        precio.categoriaNombre
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = precio.servicioNombre,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (!precio.descripcion.isNullOrEmpty()) {
                                            Text(
                                                text = precio.descripcion,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "S/ %.2f".format(precio.precio),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}
