package com.example.teqruta.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Roles del sistema TeqRuta:
 * - TECNICO       → registra traslados
 * - JEFE          → valida y aprueba traslados
 * - GERENCIA      → ve reportes y dashboard de costos
 * - ADMINISTRADOR → gestiona vehículos, clientes y tramos
 */
enum class Rol { TECNICO, JEFE, GERENCIA, ADMINISTRADOR }

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** RUT o correo — usado para login */
    val rut: String,

    val nombre: String,
    val apellido: String,
    val correo: String,

    /** Contraseña en texto plano para MVP (en producción usar hash) */
    val contrasena: String,

    val rol: Rol,

    /** true = cuenta activa, false = deshabilitada por admin */
    val activo: Boolean = true
)
