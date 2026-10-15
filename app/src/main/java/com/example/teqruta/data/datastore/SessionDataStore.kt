package com.example.teqruta.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.teqruta.data.model.Rol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión para crear el DataStore una sola vez en el Context
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sesion_teqruta")

/**
 * Gestiona la sesión activa del usuario usando DataStore.
 * Basado en el patrón de Plantilla_03_DataStore_Animaciones/EstadoDataStore.kt
 *
 * Persiste: id, nombre, correo y rol del usuario logueado.
 */
class SessionDataStore(private val context: Context) {

    companion object {
        private val KEY_USUARIO_ID     = intPreferencesKey("usuario_id")
        private val KEY_USUARIO_NOMBRE = stringPreferencesKey("usuario_nombre")
        private val KEY_USUARIO_CORREO = stringPreferencesKey("usuario_correo")
        private val KEY_USUARIO_ROL    = stringPreferencesKey("usuario_rol")
    }

    // ── Lectura reactiva ──────────────────────────────────────────────────────

    val usuarioId: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_USUARIO_ID] ?: -1
    }

    val usuarioNombre: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_USUARIO_NOMBRE] ?: ""
    }

    val usuarioRol: Flow<Rol?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USUARIO_ROL]?.let { Rol.valueOf(it) }
    }

    val estaLogueado: Flow<Boolean> = context.dataStore.data.map { prefs ->
        (prefs[KEY_USUARIO_ID] ?: -1) != -1
    }

    // ── Escritura ─────────────────────────────────────────────────────────────

    /** Guarda los datos del usuario al hacer login exitoso */
    suspend fun guardarSesion(id: Int, nombre: String, correo: String, rol: Rol) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USUARIO_ID]     = id
            prefs[KEY_USUARIO_NOMBRE] = nombre
            prefs[KEY_USUARIO_CORREO] = correo
            prefs[KEY_USUARIO_ROL]    = rol.name
        }
    }

    /** Borra la sesión al hacer logout */
    suspend fun cerrarSesion() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
