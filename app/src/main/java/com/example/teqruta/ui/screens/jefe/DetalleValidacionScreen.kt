package com.example.teqruta.ui.screens.jefe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teqruta.data.model.Traslado
import com.example.teqruta.ui.theme.*
import com.example.teqruta.ui.viewmodel.TrasladoViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * W-06: Detalle y Resolución de Viaje (Jefe de Servicio).
 * Permite al jefe auditar los kilómetros declarados, revisar la justificación
 * y resolver aprobando el monto completo o ajustando a la distancia oficial.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleValidacionScreen(
    trasladoId: Int,
    viewModel: TrasladoViewModel,
    onVolver: () -> Unit
) {
    val todos by viewModel.todosLosTraslados.collectAsState()
    val destinos by viewModel.destinos.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    val traslado = todos.firstOrNull { it.id == trasladoId }
    val destino = destinos.firstOrNull { it.id == traslado?.destinoId }

    var comentarioJefe by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensaje()
            if (it.contains("aprobado") || it.contains("Ajustado")) {
                onVolver()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Resolución de Viaje", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulPrimario,
                    titleContentColor = Blanco,
                    navigationIconContentColor = Blanco
                )
            )
        }
    ) { paddingValues ->
        if (traslado == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val kmDeclarados = (traslado.kmLlegada ?: traslado.kmSalida) - traslado.kmSalida
        val kmOficial = destino?.kmOficial?.toInt() ?: kmDeclarados

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Informacion general
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = destino?.nombre ?: "Destino #${traslado.destinoId}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Origen: ${traslado.origen}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GrisMedio
                    )
                    Text(
                        text = "Salida: ${formatearFechaHora(traslado.fechaHoraSalida)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GrisMedio
                    )
                    traslado.fechaHoraLlegada?.let {
                        Text(
                            text = "Llegada: ${formatearFechaHora(it)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GrisMedio
                        )
                    }
                }
            }

            // Comparativa de kilometraje
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AzulClaro),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Auditoría de Kilometraje",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AzulPrimario
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Km Salida", style = MaterialTheme.typography.labelMedium)
                            Text("${traslado.kmSalida} km", fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text("Km Llegada", style = MaterialTheme.typography.labelMedium)
                            Text("${traslado.kmLlegada ?: '-'} km", fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text("Declarado", style = MaterialTheme.typography.labelMedium)
                            Text("$kmDeclarados km", fontWeight = FontWeight.Bold, color = AzulPrimario)
                        }
                        Column {
                            Text("Oficial Tramo", style = MaterialTheme.typography.labelMedium)
                            Text("$kmOficial km", fontWeight = FontWeight.Bold, color = VerdeSincronizado)
                        }
                    }
                }
            }

            // Desglose de costos calculados
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Desglose de Costos Calculados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ItemCosto("Costo Combustible Estimado", traslado.costoCombustible ?: (kmDeclarados * 150.0))
                    ItemCosto("Costo Peajes (${if (traslado.peajesPagados.isBlank()) 0 else traslado.peajesPagados.split(",").size} pagados)", traslado.costoPeajes ?: 0.0)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = GrisBorde)
                    ItemCosto("Total Estimado", traslado.costoTotal ?: ((kmDeclarados * 150.0) + (traslado.costoPeajes ?: 0.0)), esTotal = true)
                }
            }

            // Justificacion del tecnico
            if (traslado.justificacionDesvio.isNotBlank()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AmarilloClaro),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Justificación del Técnico",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NaranjaOffline
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = traslado.justificacionDesvio,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Observacion del jefe
            OutlinedTextField(
                value = comentarioJefe,
                onValueChange = { comentarioJefe = it },
                label = { Text("Comentario / Observación de resolución") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ej: Aprobado según lo conversado / Ajustado por ruta estándar") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botones de resolucion
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val nota = if (comentarioJefe.isBlank()) "Ajustado a km oficial de referencia" else comentarioJefe
                        viewModel.ajustarAKmOficial(traslado.id, kmOficial, nota)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NaranjaOffline)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ajustar ($kmOficial km)")
                }

                Button(
                    onClick = {
                        val nota = if (comentarioJefe.isBlank()) "Aprobado sin observaciones" else comentarioJefe
                        viewModel.aprobarTraslado(traslado.id, nota)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeSincronizado)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Aprobar")
                }
            }
        }
    }
}

@Composable
private fun ItemCosto(etiqueta: String, monto: Double, esTotal: Boolean = false) {
    val formatoClp = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL")).apply {
        maximumFractionDigits = 0
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = etiqueta,
            style = if (esTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (esTotal) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = formatoClp.format(monto),
            style = if (esTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (esTotal) FontWeight.Bold else FontWeight.SemiBold,
            color = if (esTotal) AzulPrimario else GrisOscuro
        )
    }
}

private fun formatearFechaHora(timestamp: Long): String {
    val formateadorFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formateadorFecha.format(Date(timestamp))
}
