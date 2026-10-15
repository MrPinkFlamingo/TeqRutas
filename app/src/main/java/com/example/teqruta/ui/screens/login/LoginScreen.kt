package com.example.teqruta.ui.screens.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teqruta.data.model.Rol
import com.example.teqruta.data.model.Usuario
import com.example.teqruta.ui.theme.*
import com.example.teqruta.ui.viewmodel.AuthViewModel
import com.example.teqruta.ui.viewmodel.LoginUiState

/**
 * W-01: Pantalla de Login con indicador de conexión.
 * El técnico puede iniciar sesión con RUT o correo.
 */
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    estaOnline: Boolean,
    onLoginExitoso: (Usuario) -> Unit
) {
    val loginState by authViewModel.loginState.collectAsState()
    var credencial  by remember { mutableStateOf("") }
    var contrasena  by remember { mutableStateOf("") }

    // Navegar cuando el login es exitoso
    LaunchedEffect(loginState) {
        if (loginState is LoginUiState.Success) {
            onLoginExitoso((loginState as LoginUiState.Success).usuario)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrisClaro)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // ── Badge de estado de conexión ───────────────────────────────────────
        BadgeConexion(estaOnline = estaOnline)

        Spacer(modifier = Modifier.height(40.dp))

        // ── Logo / Nombre de la app ───────────────────────────────────────────
        Text(
            text       = "TeqRuta",
            fontSize   = 36.sp,
            fontWeight = FontWeight.Bold,
            color      = AzulPrimario
        )
        Text(
            text  = "TEQMED SpA",
            style = MaterialTheme.typography.bodyMedium,
            color = GrisMedio
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text      = "Traslados en Terreno",
            style     = MaterialTheme.typography.labelLarge,
            color     = AzulSecundario,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // ── Tarjeta de formulario ─────────────────────────────────────────────
        Card(
            shape    = RoundedCornerShape(16.dp),
            colors   = CardDefaults.cardColors(containerColor = Blanco),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier  = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text       = "Iniciar Sesión",
                    style      = MaterialTheme.typography.titleLarge,
                    color      = GrisOscuro,
                    fontWeight = FontWeight.Bold
                )

                // Campo RUT o correo
                OutlinedTextField(
                    value            = credencial,
                    onValueChange    = { credencial = it },
                    label            = { Text("RUT o Correo") },
                    leadingIcon      = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier         = Modifier.fillMaxWidth(),
                    singleLine       = true,
                    keyboardOptions  = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError          = loginState is LoginUiState.Error
                )

                // Campo contraseña
                OutlinedTextField(
                    value               = contrasena,
                    onValueChange       = { contrasena = it },
                    label               = { Text("Contraseña") },
                    leadingIcon         = { Icon(Icons.Default.Lock, contentDescription = null) },
                    modifier            = Modifier.fillMaxWidth(),
                    singleLine          = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions     = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError             = loginState is LoginUiState.Error
                )

                // Mensaje de error
                AnimatedVisibility(visible = loginState is LoginUiState.Error) {
                    Text(
                        text  = (loginState as? LoginUiState.Error)?.mensaje ?: "",
                        color = RojoDesviacion,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Botón de ingreso
                Button(
                    onClick  = { authViewModel.login(credencial, contrasena) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled  = loginState !is LoginUiState.Loading,
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
                ) {
                    if (loginState is LoginUiState.Loading) {
                        CircularProgressIndicator(
                            color    = Blanco,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Ingresar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Usuarios de prueba (solo para MVP/desarrollo)
        Card(
            shape    = RoundedCornerShape(12.dp),
            colors   = CardDefaults.cardColors(containerColor = AzulClaro),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("👤 Usuarios de prueba:", style = MaterialTheme.typography.labelLarge, color = AzulPrimario)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Técnico:  tecnico@teqmed.cl / 1234", style = MaterialTheme.typography.bodyMedium)
                Text("Jefe:     jefe@teqmed.cl / 1234",    style = MaterialTheme.typography.bodyMedium)
                Text("Gerencia: gerencia@teqmed.cl / 1234", style = MaterialTheme.typography.bodyMedium)
                Text("Admin:    admin@teqmed.cl / 1234",    style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** Indicador persistente de estado de conexión con animación fluida (IE 2.2.2) */
@Composable
fun BadgeConexion(estaOnline: Boolean, modifier: Modifier = Modifier) {
    val backgroundColorAnim by androidx.compose.animation.animateColorAsState(
        targetValue = if (estaOnline) VerdeClaro else NaranjaClaro,
        animationSpec = androidx.compose.animation.core.tween(500),
        label = "BadgeBgColor"
    )
    val contentColorAnim by androidx.compose.animation.animateColorAsState(
        targetValue = if (estaOnline) VerdeSincronizado else NaranjaOffline,
        animationSpec = androidx.compose.animation.core.tween(500),
        label = "BadgeContentColor"
    )

    Row(
        modifier          = modifier
            .background(
                color = backgroundColorAnim,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (estaOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
            contentDescription = null,
            tint     = contentColorAnim,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text  = if (estaOnline) "En línea" else "Sin conexión",
            color = contentColorAnim,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
