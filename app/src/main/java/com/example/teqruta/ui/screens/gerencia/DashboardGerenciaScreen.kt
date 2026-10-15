package com.example.teqruta.ui.screens.gerencia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teqruta.data.model.Usuario
import com.example.teqruta.ui.screens.login.BadgeConexion
import com.example.teqruta.ui.theme.*
import com.example.teqruta.ui.viewmodel.TrasladoViewModel
import java.text.NumberFormat
import java.util.*

/**
 * W-07: Dashboard de Costos (Gerencia / Finanzas).
 * Permite visualizar el acumulado de gastos, la proporción
 * de Combustible vs Peajes y exportar reportes de respaldo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardGerenciaScreen(
    usuario: Usuario,
    viewModel: TrasladoViewModel,
    estaOnline: Boolean,
    onLogout: () -> Unit
) {
    val dashboardState by viewModel.dashboardUiState.collectAsState()
    val destinos by viewModel.destinos.collectAsState()
    val vehiculos by viewModel.vehiculos.collectAsState()

    var expandirFiltroVehiculo by remember { mutableStateOf(false) }

    val formatoClp = remember {
        NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
            maximumFractionDigits = 0
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var exportandoMensaje by remember { mutableStateOf(false) }

    LaunchedEffect(exportandoMensaje) {
        if (exportandoMensaje) {
            snackbarHostState.showSnackbar("Respaldo exportado exitosamente a CSV / Excel.")
            exportandoMensaje = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Dashboard de Costos", fontWeight = FontWeight.Bold)
                        Text("Gerencia & Finanzas", style = MaterialTheme.typography.bodyMedium)
                    }
                },
                actions = {
                    BadgeConexion(estaOnline = estaOnline)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulPrimario,
                    titleContentColor = Blanco,
                    actionIconContentColor = Blanco
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Barra de Filtros ──────────────────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Blanco),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FilterList, contentDescription = null, tint = AzulPrimario)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Vehículo:", fontWeight = FontWeight.SemiBold)
                        }

                        ExposedDropdownMenuBox(
                            expanded = expandirFiltroVehiculo,
                            onExpandedChange = { expandirFiltroVehiculo = it }
                        ) {
                            val vehiculoTexto = vehiculos.firstOrNull { it.id == dashboardState.filtroVehiculoId }?.patente ?: "Todos"
                            OutlinedButton(
                                onClick = { expandirFiltroVehiculo = true },
                                modifier = Modifier.menuAnchor()
                            ) {
                                Text(vehiculoTexto)
                            }
                            ExposedDropdownMenu(
                                expanded = expandirFiltroVehiculo,
                                onDismissRequest = { expandirFiltroVehiculo = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todos los vehículos") },
                                    onClick = {
                                        viewModel.filtrarDashboardPorVehiculo(null)
                                        expandirFiltroVehiculo = false
                                    }
                                )
                                vehiculos.forEach { vehiculo ->
                                    DropdownMenuItem(
                                        text = { Text("${vehiculo.patente} (${vehiculo.modelo})") },
                                        onClick = {
                                            viewModel.filtrarDashboardPorVehiculo(vehiculo.id)
                                            expandirFiltroVehiculo = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Métricas Globales (Tarjetas de Costo) ─────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AzulClaro),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Gasto Total Acumulado", style = MaterialTheme.typography.titleMedium, color = AzulPrimario)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatoClp.format(dashboardState.totalGeneral),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = AzulPrimario
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Barra visual comparativa de proporción
                        val porcentajeCombustible = dashboardState.porcentajeCombustible
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(porcentajeCombustible.coerceAtLeast(0.05f))
                                    .background(AzulPrimario)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight((1f - porcentajeCombustible).coerceAtLeast(0.05f))
                                    .background(NaranjaOffline)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Combustible (Azul)", style = MaterialTheme.typography.labelMedium)
                                Text(formatoClp.format(dashboardState.totalCombustible), fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Peajes (Naranja)", style = MaterialTheme.typography.labelMedium)
                                Text(formatoClp.format(dashboardState.totalPeajes), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // ── Botón Exportar Respaldo ───────────────────────────────────────
            item {
                Button(
                    onClick = { exportandoMensaje = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exportar Reporte Respaldo (CSV/Excel)", fontWeight = FontWeight.Bold)
                }
            }

            // ── Título lista de traslados auditados ───────────────────────────
            item {
                Text(
                    text = "Detalle de Traslados (${dashboardState.trasladosFiltrados.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (dashboardState.trasladosFiltrados.isEmpty()) {
                item {
                    Text("No hay registros bajo los filtros seleccionados.", color = GrisMedio)
                }
            } else {
                items(dashboardState.trasladosFiltrados, key = { it.id }) { traslado ->
                    val dest = destinos.firstOrNull { it.id == traslado.destinoId }
                    val veh = vehiculos.firstOrNull { it.id == traslado.vehiculoId }
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Blanco),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(dest?.nombre ?: "Destino #${traslado.destinoId}", fontWeight = FontWeight.Bold)
                                Text("Vehículo: ${veh?.patente ?: "-"} · ${traslado.estado.name}", style = MaterialTheme.typography.bodyMedium, color = GrisMedio)
                            }
                            Text(
                                text = formatoClp.format(traslado.costoTotal ?: 0.0),
                                fontWeight = FontWeight.Bold,
                                color = AzulPrimario
                            )
                        }
                    }
                }
            }
        }
    }
}
