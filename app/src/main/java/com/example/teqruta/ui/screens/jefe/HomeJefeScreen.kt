package com.example.teqruta.ui.screens.jefe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teqruta.data.model.Traslado
import com.example.teqruta.data.model.Usuario
import com.example.teqruta.ui.screens.login.BadgeConexion
import com.example.teqruta.ui.theme.*
import com.example.teqruta.ui.viewmodel.TrasladoViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * W-05: Bandeja de Validación (Jefe de Servicio Técnico).
 * Incluye pestañas para filtrar:
 * 1. "Con Desviación (Alerta)"
 * 2. "Todos los Pendientes"
 * Compara visualmente Km Declarado vs Km Oficial.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeJefeScreen(
    usuario: Usuario,
    viewModel: TrasladoViewModel,
    estaOnline: Boolean,
    onSeleccionarTraslado: (Int) -> Unit,
    onLogout: () -> Unit
) {
    val pendientesTodos by viewModel.pendientesValidacion.collectAsState()
    val pendientesDesviacion by viewModel.pendientesConDesviacion.collectAsState()
    val destinos by viewModel.destinos.collectAsState()

    var pestanaSeleccionada by remember { mutableStateOf(0) }
    val titulosPestanas = listOf("Con Desviación", "Todos los Pendientes")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Bandeja de Validación", fontWeight = FontWeight.Bold)
                        Text("Jefe: ${usuario.nombre} ${usuario.apellido}", style = MaterialTheme.typography.bodyMedium)
                    }
                },
                actions = {
                    BadgeConexion(estaOnline = estaOnline)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
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

            val listaActual = if (pestanaSeleccionada == 0) pendientesDesviacion else pendientesTodos

            if (listaActual.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = VerdeSincronizado,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (pestanaSeleccionada == 0) "No hay traslados con desviación pendientes"
                            else "No hay traslados pendientes de revisión",
                            style = MaterialTheme.typography.bodyLarge,
                            color = GrisMedio
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listaActual, key = { it.id }) { traslado ->
                        val destino = destinos.firstOrNull { it.id == traslado.destinoId }
                        TarjetaValidacionJefe(
                            traslado = traslado,
                            destinoNombre = destino?.nombre ?: "Destino #${traslado.destinoId}",
                            kmOficial = destino?.kmOficial ?: 0.0,
                            onClick = { onSeleccionarTraslado(traslado.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaValidacionJefe(
    traslado: Traslado,
    destinoNombre: String,
    kmOficial: Double,
    onClick: () -> Unit
) {
    val kmDeclarados = (traslado.kmLlegada ?: traslado.kmSalida) - traslado.kmSalida
    val tieneDesviacion = traslado.justificacionDesvio.isNotBlank() || (kmOficial > 0 && kmDeclarados > kmOficial * 1.15)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Blanco),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = destinoNombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (tieneDesviacion) {
                    Surface(
                        color = RojoClaro,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = RojoDesviacion, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Desvío", color = RojoDesviacion, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Fecha: ${formatearFechaSimple(traslado.fechaHoraSalida)}",
                style = MaterialTheme.typography.bodyMedium,
                color = GrisMedio
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = GrisBorde)
            Spacer(modifier = Modifier.height(12.dp))

            // Comparativa de Kilómetros
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Km Declarados", style = MaterialTheme.typography.labelMedium, color = GrisMedio)
                    Text("$kmDeclarados km", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Km Oficial Tramo", style = MaterialTheme.typography.labelMedium, color = GrisMedio)
                    Text("${kmOficial.toInt()} km", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AzulSecundario)
                }
            }

            if (traslado.justificacionDesvio.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = AmarilloClaro,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Motivo: ${traslado.justificacionDesvio}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

private fun formatearFechaSimple(timestamp: Long): String {
    val formateadorFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formateadorFecha.format(Date(timestamp))
}
