package com.example.teqruta.ui.screens.admin

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teqruta.data.model.Usuario
import com.example.teqruta.ui.screens.login.BadgeConexion
import com.example.teqruta.ui.theme.*
import com.example.teqruta.ui.viewmodel.TrasladoViewModel

/**
 * W-08: Mantenedores y Configuración (Administrador del Sistema).
 * Muestra el precio actual del combustible (simulado de API pública ENAP/CNE),
 * y pestañas para gestionar Vehículos y Destinos/Tramos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MantenedoresAdminScreen(
    usuario: Usuario,
    viewModel: TrasladoViewModel,
    estaOnline: Boolean,
    onLogout: () -> Unit
) {
    val vehiculos by viewModel.vehiculos.collectAsState()
    val destinos by viewModel.destinos.collectAsState()
    val estadoAdmin by viewModel.adminState.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    var pestanaSeleccionada by remember { mutableStateOf(0) }
    val titulosPestanas = listOf("Vehículos", "Clínicas y Tramos")

    val estadoSnackbar = remember { SnackbarHostState() }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            estadoSnackbar.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(estadoSnackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Panel Administrador", fontWeight = FontWeight.Bold)
                        Text("Mantenedores del Sistema", style = MaterialTheme.typography.bodyMedium)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Tarjeta API Precio Combustible (Pública ENAP / CNE) ────────────
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AzulClaro),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = AzulPrimario)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("API Combustible (CNE/ENAP)", style = MaterialTheme.typography.labelMedium, color = AzulPrimario, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$${estadoAdmin.precioCombustible.toInt()} CLP / Litro (Diesel)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (estaOnline) "Sincronizado hoy con API pública" else "Última tarifa guardada en caché",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GrisMedio
                        )
                    }

                    IconButton(
                        onClick = {
                            if (estaOnline) {
                                viewModel.actualizarTarifaCombustibleDesdeApi()
                            }
                        },
                        enabled = estaOnline && !estadoAdmin.actualizandoTarifa
                    ) {
                        if (estadoAdmin.actualizandoTarifa) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = AzulPrimario
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Actualizar tarifa", tint = AzulPrimario)
                        }
                    }
                }
            }

            // ── Pestañas de Mantenedores ───────────────────────────────────────
            TabRow(
                selectedTabIndex = pestanaSeleccionada,
                containerColor = Blanco,
                contentColor = AzulPrimario
            ) {
                titulosPestanas.forEachIndexed { index, titulo ->
                    Tab(
                        selected = pestanaSeleccionada == index,
                        onClick = { pestanaSeleccionada = index },
                        text = {
                            Text(
                                text = titulo,
                                fontWeight = if (pestanaSeleccionada == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // ── Lista según pestaña ───────────────────────────────────────────
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (pestanaSeleccionada == 0) {
                    // Mantenedor Vehículos
                    items(vehiculos, key = { it.id }) { vehiculo ->
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
                                    Text("${vehiculo.marca} ${vehiculo.modelo} (${vehiculo.anio})", fontWeight = FontWeight.Bold)
                                    Text("Patente: ${vehiculo.patente} · ${vehiculo.kmActual} km", style = MaterialTheme.typography.bodyMedium, color = GrisMedio)
                                }
                                Surface(
                                    color = if (vehiculo.disponible) VerdeClaro else RojoClaro,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (vehiculo.disponible) "Activo" else "Taller",
                                        color = if (vehiculo.disponible) VerdeSincronizado else RojoDesviacion,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Mantenedor Clínicas y Tramos
                    items(destinos, key = { it.id }) { destino ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Blanco),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(destino.nombre, fontWeight = FontWeight.Bold)
                                    Text("${destino.kmOficial} km", fontWeight = FontWeight.Bold, color = AzulSecundario)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(destino.direccion + ", " + destino.ciudad, style = MaterialTheme.typography.bodyMedium, color = GrisMedio)
                                if (destino.peajesDelTramo.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Peajes: ${destino.peajesDelTramo}", style = MaterialTheme.typography.labelMedium, color = NaranjaOffline)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
