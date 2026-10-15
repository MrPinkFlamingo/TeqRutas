package com.example.teqruta.ui.screens.tecnico

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.teqruta.data.model.Usuario
import com.example.teqruta.ui.theme.*
import com.example.teqruta.ui.viewmodel.TrasladoViewModel

/**
 * W-02: Formulario para iniciar un nuevo traslado.
 * Implementado bajo arquitectura MVVM:
 * - El estado y la validación residen centralizados en TrasladoViewModel (IE 2.2.1)
 * - Diseñado para completarse en máximo 3-4 taps (RNF-03).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoTrasladoScreen(
    usuario:   Usuario,
    viewModel: TrasladoViewModel,
    onVolver:  () -> Unit
) {
    val destinos  by viewModel.destinos.collectAsState()
    val vehiculos by viewModel.vehiculos.collectAsState()
    val mensaje   by viewModel.mensaje.collectAsState()
    val cargando  by viewModel.cargando.collectAsState()

    // Estado centralizado y validado en el ViewModel (MVVM)
    val estadoFormulario by viewModel.formNuevoState.collectAsState()

    var expandirDestino  by remember { mutableStateOf(false) }
    var expandirVehiculo by remember { mutableStateOf(false) }

    val estadoSnackbar = remember { SnackbarHostState() }
    LaunchedEffect(mensaje) {
        mensaje?.let {
            estadoSnackbar.showSnackbar(it)
            viewModel.limpiarMensaje()
            if (it.contains("éxito")) onVolver()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(estadoSnackbar) },
        topBar = {
            TopAppBar(
                title          = { Text("Nuevo Traslado", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Seleccion de destino
            Text("1. Destino", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            ExposedDropdownMenuBox(
                expanded         = expandirDestino,
                onExpandedChange = { expandirDestino = it }
            ) {
                OutlinedTextField(
                    value            = estadoFormulario.destinoSeleccionado?.nombre ?: "Selecciona clínica / hospital",
                    onValueChange    = {},
                    readOnly         = true,
                    label            = { Text("Destino") },
                    leadingIcon      = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    trailingIcon     = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirDestino) },
                    modifier         = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded         = expandirDestino,
                    onDismissRequest = { expandirDestino = false }
                ) {
                    destinos.forEach { destino ->
                        DropdownMenuItem(
                            text    = {
                                Column {
                                    Text(destino.nombre, fontWeight = FontWeight.Medium)
                                    Text("${destino.kmOficial} km oficiales · ${destino.ciudad}",
                                        style = MaterialTheme.typography.bodyMedium, color = GrisMedio)
                                }
                            },
                            onClick = {
                                viewModel.onNuevoDestinoSeleccionado(destino)
                                expandirDestino = false
                            }
                        )
                    }
                }
            }

            // Seleccion de vehiculo
            Text("2. Vehículo Asignado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            ExposedDropdownMenuBox(
                expanded         = expandirVehiculo,
                onExpandedChange = { expandirVehiculo = it }
            ) {
                OutlinedTextField(
                    value            = estadoFormulario.vehiculoSeleccionado?.let { "${it.marca} ${it.modelo} · ${it.patente}" }
                                       ?: "Selecciona vehículo",
                    onValueChange    = {},
                    readOnly         = true,
                    label            = { Text("Vehículo") },
                    leadingIcon      = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                    trailingIcon     = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirVehiculo) },
                    modifier         = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded         = expandirVehiculo,
                    onDismissRequest = { expandirVehiculo = false }
                ) {
                    vehiculos.forEach { vehiculo ->
                        DropdownMenuItem(
                            text    = {
                                Column {
                                    Text("${vehiculo.marca} ${vehiculo.modelo}", fontWeight = FontWeight.Medium)
                                    Text("Patente: ${vehiculo.patente} · Km: ${vehiculo.kmActual}",
                                        style = MaterialTheme.typography.bodyMedium, color = GrisMedio)
                                }
                            },
                            onClick = {
                                viewModel.onNuevoVehiculoSeleccionado(vehiculo)
                                expandirVehiculo = false
                            }
                        )
                    }
                }
            }

            // Odometro inicial
            Text("3. Odómetro de Salida", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value           = estadoFormulario.kmSalidaTexto,
                onValueChange   = { viewModel.onNuevoKmSalidaCambiado(it) },
                label           = { Text("Kilómetros actuales del vehículo") },
                leadingIcon     = { Icon(Icons.Default.Speed, contentDescription = null) },
                suffix          = { Text("km") },
                modifier        = Modifier.fillMaxWidth(),
                singleLine      = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError         = estadoFormulario.errorKmSalida != null,
                supportingText  = {
                    estadoFormulario.errorKmSalida?.let {
                        Text(it, color = RojoDesviacion)
                    }
                },
                placeholder     = { Text(estadoFormulario.vehiculoSeleccionado?.kmActual?.toString() ?: "Ej: 45230") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Accion de inicio de traslado
            Button(
                onClick  = {
                    estadoFormulario.destinoSeleccionado?.let { destino ->
                        estadoFormulario.vehiculoSeleccionado?.let { vehiculo ->
                            viewModel.iniciarTraslado(
                                tecnicoId  = usuario.id,
                                vehiculoId = vehiculo.id,
                                destinoId  = destino.id,
                                kmSalida   = estadoFormulario.kmSalidaTexto.toInt()
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled  = estadoFormulario.esValido && !cargando,
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
            ) {
                if (cargando) {
                    CircularProgressIndicator(color = Blanco, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text("Iniciar Traslado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
