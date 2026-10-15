package com.example.teqruta.ui.screens.tecnico

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teqruta.data.model.EstadoSync
import com.example.teqruta.data.model.EstadoTraslado
import com.example.teqruta.data.model.Traslado
import com.example.teqruta.data.model.Usuario
import com.example.teqruta.ui.screens.login.BadgeConexion
import com.example.teqruta.ui.theme.*
import com.example.teqruta.ui.viewmodel.TrasladoViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * W-02 / W-04: Home del Técnico.
 * Muestra el viaje activo (si hay uno) y el historial de traslados.
 * Botón FAB para iniciar un nuevo traslado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTecnicoScreen(
    usuario: Usuario,
    viewModel: TrasladoViewModel,
    estaOnline: Boolean,
    onNuevoTraslado: () -> Unit,
    onFinalizarTraslado: (Int) -> Unit,
    onLogout: () -> Unit
) {
    val trasladosFlow = remember(usuario.id) { viewModel.trasladosDeTecnico(usuario.id) }
    val traslados by trasladosFlow.collectAsState()
    val mensaje   by viewModel.mensaje.collectAsState()

    // Separar activo del historial
    val trasladoActivo = traslados.firstOrNull { it.estado == EstadoTraslado.EN_CURSO }
    val historial      = traslados.filter   { it.estado != EstadoTraslado.EN_CURSO }

    // Snackbar para mensajes
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("TeqRuta", fontWeight = FontWeight.Bold)
                        Text("Hola, ${usuario.nombre}", style = MaterialTheme.typography.bodyMedium)
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
                    containerColor    = AzulPrimario,
                    titleContentColor = Blanco,
                    actionIconContentColor = Blanco
                )
            )
        },
        floatingActionButton = {
            // Solo mostrar FAB si no hay traslado activo
            if (trasladoActivo == null) {
                FloatingActionButton(
                    onClick          = onNuevoTraslado,
                    containerColor   = AzulSecundario,
                    contentColor     = Blanco
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo traslado")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier            = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding      = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Tarjeta de traslado activo ────────────────────────────────────
            if (trasladoActivo != null) {
                item {
                    TarjetaTrasladoActivo(
                        traslado        = trasladoActivo,
                        onFinalizar     = { onFinalizarTraslado(trasladoActivo.id) }
                    )
                }
            }

            // ── Encabezado historial ──────────────────────────────────────────
            item {
                Text(
                    text       = "Mis Traslados",
                    style      = MaterialTheme.typography.titleLarge,
                    color      = GrisOscuro,
                    fontWeight = FontWeight.Bold
                )
            }

            if (historial.isEmpty()) {
                item {
                    Box(
                        modifier           = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment   = Alignment.Center
                    ) {
                        Text("No tienes traslados registrados", color = GrisMedio)
                    }
                }
            } else {
                items(historial, key = { it.id }) { traslado ->
                    TarjetaTrasladoHistorial(traslado = traslado)
                }
            }
        }
    }
}

/** Tarjeta destacada para el traslado actualmente EN CURSO */
@Composable
fun TarjetaTrasladoActivo(traslado: Traslado, onFinalizar: () -> Unit) {
    // Animación de pulso continuo para viaje activo (IE 2.2.2)
    val transicionInfinita = rememberInfiniteTransition(label = "pulsoEnCurso")
    val alfaPulsante by transicionInfinita.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alfaPulsante"
    )

    Card(
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = AmarilloClaro),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .graphicsLayer { alpha = alfaPulsante }
                        .background(AmarilloAlerta, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text       = "EN CURSO (En Terreno)",
                    color      = AmarilloAlerta,
                    fontWeight = FontWeight.Bold,
                    style      = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Destino ID: ${traslado.destinoId}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Km salida: ${traslado.kmSalida}", style = MaterialTheme.typography.bodyMedium, color = GrisMedio)
            Text(
                text  = "Inicio: ${formatearFecha(traslado.fechaHoraSalida)}",
                style = MaterialTheme.typography.bodyMedium,
                color = GrisMedio
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick  = onFinalizar,
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(containerColor = AzulPrimario),
                shape    = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Finalizar Traslado", fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** Tarjeta del historial con íconos de sincronización y estado de validación */
@Composable
fun TarjetaTrasladoHistorial(traslado: Traslado) {
    val (colorEstado, iconoEstado, textoEstado) = when (traslado.estado) {
        EstadoTraslado.APROBADO  -> Triple(VerdeSincronizado, Icons.Default.CheckCircle,  "Aprobado")
        EstadoTraslado.AJUSTADO  -> Triple(AmarilloAlerta,   Icons.Default.Warning,       "Ajustado")
        EstadoTraslado.RECHAZADO -> Triple(RojoDesviacion,   Icons.Default.Cancel,        "Rechazado")
        EstadoTraslado.PENDIENTE -> Triple(GrisMedio,        Icons.Default.HourglassEmpty,"Pendiente")
        else                     -> Triple(GrisMedio,        Icons.Default.Info,          traslado.estado.name)
    }

    val iconoSync = if (traslado.estadoSync == EstadoSync.SINCRONIZADO)
        Icons.Default.Cloud else Icons.Default.CloudOff
    val colorSync = if (traslado.estadoSync == EstadoSync.SINCRONIZADO)
        VerdeSincronizado else NaranjaOffline

    Card(
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = Blanco),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(iconoEstado, contentDescription = null, tint = colorEstado, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Destino ID: ${traslado.destinoId}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(formatearFecha(traslado.fechaHoraSalida), style = MaterialTheme.typography.bodyMedium, color = GrisMedio)
                if (traslado.kmLlegada != null) {
                    Text("${traslado.kmLlegada - traslado.kmSalida} km recorridos", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Icon(iconoSync, contentDescription = null, tint = colorSync, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text(textoEstado, style = MaterialTheme.typography.labelMedium, color = colorEstado)
            }
        }
    }
}

private fun formatearFecha(timestamp: Long): String {
    val formateadorFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formateadorFecha.format(Date(timestamp))
}
