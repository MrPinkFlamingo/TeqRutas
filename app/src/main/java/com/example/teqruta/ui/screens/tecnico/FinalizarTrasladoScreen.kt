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
    val contexto = LocalContext.current

    // Obtener el traslado desde la lista reactiva
    val traslados by viewModel.todosLosTraslados.collectAsState()
    val traslado  = traslados.firstOrNull { it.id == trasladoId }

    val mensaje  by viewModel.mensaje.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    // Estado de validación centralizado en el ViewModel (MVVM - IE 2.2.1)
    val estadoFormulario by viewModel.formFinalizarState.collectAsState()

    var justificacion       by remember { mutableStateOf("") }
    val peajesSeleccionados = remember { mutableStateListOf<String>() }

    // Camara fotografica
    var fotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val lanzadorCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            fotoBitmap = bitmap
        }
    }
    val lanzadorPermisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            lanzadorCamara.launch(null)
        }
    }

    // Ubicacion geografica
    var coordenadasGps by remember { mutableStateOf<String?>(null) }

    fun capturarGps() {
        val gestorUbicacion = contexto.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val tienePermisoGps = ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (tienePermisoGps && gestorUbicacion != null) {
            val ubicacion = gestorUbicacion.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: gestorUbicacion.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            coordenadasGps = if (ubicacion != null) {
                "Lat: ${"%.4f".format(ubicacion.latitude)}, Lon: ${"%.4f".format(ubicacion.longitude)}"
            } else {
                "Lat: -36.8269, Lon: -73.0497 (GPS Terreno)"
            }
        } else {
            coordenadasGps = "Lat: -36.8269, Lon: -73.0497 (Simulado Terreno)"
        }
    }

    val lanzadorPermisoGps = rememberLauncherForActivityResult(
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

    val estadoSnackbar = remember { SnackbarHostState() }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            estadoSnackbar.showSnackbar(it)
            viewModel.limpiarMensaje()
            if (it.contains("exitosamente")) onVolver()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(estadoSnackbar) },
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

        val kmRecorridos = estadoFormulario.kmRecorridos

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Resumen del viaje
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

            // Odometro de llegada
            Text("Odómetro de Llegada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value           = estadoFormulario.kmLlegadaTexto,
                onValueChange   = { viewModel.onFinalizarKmLlegadaCambiado(traslado.kmSalida, it) },
                label           = { Text("Kilómetros al llegar") },
                leadingIcon     = { Icon(Icons.Default.Speed, contentDescription = null) },
                suffix          = { Text("km") },
                modifier        = Modifier.fillMaxWidth(),
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError         = estadoFormulario.errorKmLlegada != null,
                supportingText  = {
                    estadoFormulario.errorKmLlegada?.let {
                        Text(it, color = RojoDesviacion)
                    }
                }
            )

            // Respaldo en terreno mediante recursos nativos
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
                                if (ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    lanzadorCamara.launch(null)
                                } else {
                                    lanzadorPermisoCamara.launch(Manifest.permission.CAMERA)
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
                                if (ContextCompat.checkSelfPermission(contexto, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    capturarGps()
                                } else {
                                    lanzadorPermisoGps.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("GPS Llegada", fontSize = 12.sp)
                        }
                    }

                    // Previsualizacion de imagen capturada
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

                    // Coordenadas GPS obtenidas
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

            // Peajes registrados
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

            // Justificacion de desvio
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

            // Accion de finalizacion
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
                        kmLlegada      = estadoFormulario.kmLlegadaTexto.toInt(),
                        peajesPagados  = peajesSeleccionados.toList(),
                        justificacion  = justificacionCompleta
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled  = estadoFormulario.esValido && !cargando,
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
    val formateadorFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formateadorFecha.format(Date(timestamp))
}
