package com.example.teqruta.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teqruta.data.database.AppDatabase
import com.example.teqruta.data.datastore.SessionDataStore
import com.example.teqruta.data.model.Rol
import com.example.teqruta.data.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Estados posibles del proceso de login.
 * Basado en el patrón de Plantilla_03_DataStore_Animaciones.
 */
sealed class LoginUiState {
    object Idle    : LoginUiState()   // Estado inicial
    object Loading : LoginUiState()   // Verificando credenciales
    data class Success(val usuario: Usuario) : LoginUiState() // Login OK
    data class Error(val mensaje: String)    : LoginUiState() // Credenciales incorrectas
}

/**
 * ViewModel de autenticación.
 * Maneja el login, la sesión persistida en DataStore y el logout.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val baseDeDatos   = AppDatabase.getInstance(application)
    private val usuarioDao    = baseDeDatos.usuarioDao()
    private val almacenSesion = SessionDataStore(application)

    // ── Estado del UI ─────────────────────────────────────────────────────────
    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    // ── Al iniciar el ViewModel, verificar si ya hay sesión activa ─────────────
    init {
        viewModelScope.launch {
            val idGuardado = almacenSesion.usuarioId.first()
            if (idGuardado != -1) {
                val usuario = usuarioDao.getById(idGuardado)
                _usuarioActual.value = usuario
                if (usuario != null) {
                    _loginState.value = LoginUiState.Success(usuario)
                }
            }
        }
    }

    // ── Acciones del usuario ──────────────────────────────────────────────────

    fun login(credencial: String, contrasena: String) {
        if (credencial.isBlank() || contrasena.isBlank()) {
            _loginState.value = LoginUiState.Error("Completa todos los campos")
            return
        }
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            val usuario = usuarioDao.login(credencial.trim(), contrasena.trim())
            if (usuario != null) {
                almacenSesion.guardarSesion(
                    id      = usuario.id,
                    nombre  = "${usuario.nombre} ${usuario.apellido}",
                    correo  = usuario.correo,
                    rol     = usuario.rol
                )
                _usuarioActual.value = usuario
                _loginState.value = LoginUiState.Success(usuario)
            } else {
                _loginState.value = LoginUiState.Error("RUT/correo o contraseña incorrectos")
            }
        }
    }

    fun logout() {
        _usuarioActual.value = null
        _loginState.value    = LoginUiState.Idle
        viewModelScope.launch {
            almacenSesion.cerrarSesion()
        }
    }

    fun resetearEstado() {
        _loginState.value = LoginUiState.Idle
    }
}
