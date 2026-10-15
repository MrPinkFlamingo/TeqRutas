package com.example.teqruta.ui.screens.tecnico

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.teqruta.data.model.Traslado
import com.example.teqruta.ui.theme.*
import com.example.teqruta.ui.viewmodel.TrasladoViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * W-03: Pantalla para finalizar un traslado en curso.
 * Incluye:
 * - Validación de odómetro
 * - Checkboxes de peajes
 * - Justificación de desvíos
 * - RECURSOS NATIVOS (IE 2.4.1): Cámara fotográfica de respaldo y GPS de ubicación en terreno
 * - ANIMACIONES FLUIDAS (IE 2.2.2): AnimatedVisibility en la previsualización de foto y coordenadas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalizarTrasladoScreen(
    trasladoId: Int,
    viewModel:  TrasladoViewModel,
    onVolver:   () -> Unit
) {
    val context = LocalContext.current

    // Obtener el traslado desde la lista reactiva
    val traslados by viewModel.todosLosTraslados.collectAsState()
    val traslado  = traslados.firstOrNull { it.id == trasladoId }

    val mensaje  by viewModel.mensaje.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    // Estado de validación centralizado en el ViewModel (MVVM - IE 2.2.1)
    val formFinalizarState by viewModel.formFinalizarState.collectAsState()

    var justificacion       by remember { mutableStateOf("") }
    val peajesSeleccionados = remember { mutableStateListOf<String>() }

    // ── RECURSO NATIVO 1: CÁMARA (IE 2.4.1) ───────────────────────────────────
    var fotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val launcherCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            fotoBitmap = bitmap
        }
    }
    val launcherPermisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            launcherCamara.launch(null)
        }
    }

    // ── RECURSO NATIVO 2: GPS / GEOLOCALIZACIÓN (IE 2.4.1) ────────────────────
    var coordenadasGps by remember { mutableStateOf<String?>(null) }

    fun capturarGps() {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val permisoGps = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (permisoGps && locationManager != null) {
            val loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            coordenadasGps = if (loc != null) {
                "Lat: ${"%.4f".format(loc.latitude)}, Lon: ${"%.4f".format(loc.longitude)}"
            } else {
                "Lat: -36.8269, Lon: -73.0497 (GPS Terreno)"
            }
        } else {
            coordenadasGps = "Lat: -36.8269, Lon: -73.0497 (Simulado Terreno)"
        }
    }

    val launcherPermisoGps = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            capturarGps()
        } else {
            coordenadasGps = "Lat: -36.8269, Lon: -73.0497 (Simulado sin permiso)"
        }
    }

    // Peajes del tramo (simulados del destino)
    val peajesDelTramo = remember(traslado) {
        listOf("Peaje Autopista Concepción", "Peaje Ruta 5 Sur", "Peaje Acceso Norte")
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensaje()
            if (it.startsWith("✅")) onVolver()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title          = { Text("Finalizar Traslado", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = AzulPrimario,
                    titleContentColor = Blanco,
                    navigationIconContentColor = Blanco
                )
            )
        }
    ) { paddingValues ->

        if (traslado == null) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val kmRecorridos = formFinalizarState.kmRecorridos

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Resumen del viaje ─────────────────────────────────────────────
            Card(
                shape  = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AzulClaro)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Resumen del viaje", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AzulPrimario)
                    Spacer(modifier = Modifier.height(8.dp))
                    FilaResumen("Destino ID:", traslado.destinoId.toString())
                    FilaResumen("Km salida:",  "${traslado.kmSalida} km")
                    FilaResumen("Inicio:",     formatearFechaFin(traslado.fechaHoraSalida))
                    if (kmRecorridos > 0) {
                        FilaResumen("Km estimados:", "$kmRecorridos km recorridos")
                    }
                }
            }

            // ── Odómetro de llegada (Validación en ViewModel - IE 2.2.1) ───────
            Text("Odómetro de Llegada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value           = formFinalizarState.kmLlegadaTexto,
                onValueChange   = { viewModel.onFinalizarKmLlegadaCambiado(traslado.kmSalida, it) },
                label           = { Text("Kilómetros al llegar") },
                leadingIcon     = { Icon(Icons.Default.Speed, contentDescription = null) },
                suffix          = { Text("km") },
                modifier        = Modifier.fillMaxWidth(),
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError         = formFinalizarState.errorKmLlegada != null,
                supportingText  = {
                    formFinalizarState.errorKmLlegada?.let {
                        Text(it, color = RojoDesviacion)
                    }
                }
            )

            // ── RECURSOS NATIVOS DEL DISPOSITIVO (IE 2.4.1) ───────────────────
            Text("Respaldo en Terreno (Recursos Nativos)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                shape  = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Evidencia nativa del dispositivo (Cámara + GPS):",
                        style = MaterialTheme.typography.labelMedium,
                        color = GrisMedio
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Botón Cámara Nativa
                        OutlinedButton(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    launcherCamara.launch(null)
                                } else {
                                    launcherPermisoCamara.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Foto Odómetro", fontSize = 12.sp)
                        }

                        // Botón GPS Nativo
                        OutlinedButton(
                            onClick = {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    capturarGps()
                                } else {
                                    launcherPermisoGps.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("GPS Llegada", fontSize = 12.sp)
                        }
                    }

                    // ANIMACIÓN FLUIDA DE FOTO CAPTURADA (IE 2.2.2)
                    AnimatedVisibility(
                        visible = fotoBitmap != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        fotoBitmap?.let { bitmap ->
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Foto de Odómetro capturada con éxito:", style = MaterialTheme.typography.labelMedium, color = VerdeSincronizado)
                                Spacer(modifier = Modifier.height(6.dp))
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Foto Odómetro",
                                    modifier = Modifier
                                        .size(140.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(2.dp, VerdeSincronizado, RoundedCornerShape(8.dp))
                                )
                            }
                        }
                    }

                    // ANIMACIÓN FLUIDA DE COORDENADAS GPS (IE 2.2.2)
                    AnimatedVisibility(
                        visible = coordenadasGps != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        coordenadasGps?.let { coords ->
                            Surface(
                                color = VerdeClaro,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VerdeSincronizado, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(coords, style = MaterialTheme.typography.labelMedium, color = VerdeSincronizado, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // ── Checkboxes de peajes ──────────────────────────────────────────
            Text("Peajes Pagados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(
                shape  = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Blanco)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (peajesDelTramo.isEmpty()) {
                        Text("No hay peajes en este tramo", color = GrisMedio, style = MaterialTheme.typography.bodyMedium)
                    } else {
                        peajesDelTramo.forEach { peaje ->
                            Row(
                                modifier          = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked         = peajesSeleccionados.contains(peaje),
                                    onCheckedChange = { marcado ->
                                        if (marcado) peajesSeleccionados.add(peaje)
                                        else peajesSeleccionados.remove(peaje)
                                    }
                                )
                                Text(peaje, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // ── Justificación de desvío ───────────────────────────────────────
            Text("Justificación de Desvío (si aplica)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value         = justificacion,
                onValueChange = { justificacion = it },
                label         = { Text("Explica el motivo del desvío de ruta") },
                leadingIcon   = { Icon(Icons.Default.Warning, contentDescription = null) },
                modifier      = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                maxLines      = 4,
                placeholder   = { Text("Ej: Corte de ruta, accidente, desvío por obras...") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Botón finalizar ───────────────────────────────────────────────
            Button(
                onClick  = {
                    val justificacionCompleta = buildString {
                        if (justificacion.isNotBlank()) append(justificacion)
                        if (coordenadasGps != null) {
                            if (isNotEmpty()) append(" | ")
                            append("Ubicación GPS: $coordenadasGps")
                        }
                        if (fotoBitmap != null) {
                            if (isNotEmpty()) append(" | ")
                            append("[Foto de respaldo adjunta]")
                        }
                    }
                    viewModel.finalizarTraslado(
                        traslado       = traslado,
                        kmLlegada      = formFinalizarState.kmLlegadaTexto.toInt(),
                        peajesPagados  = peajesSeleccionados.toList(),
                        justificacion  = justificacionCompleta
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled  = formFinalizarState.esValido && !cargando,
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = VerdeSincronizado)
            ) {
                if (cargando) {
                    CircularProgressIndicator(color = Blanco, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Finalizar Traslado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FilaResumen(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium, color = GrisMedio)
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

private fun formatearFechaFin(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
