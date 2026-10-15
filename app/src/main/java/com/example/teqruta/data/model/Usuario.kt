package com.example.teqruta.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Roles del sistema TeqRuta (Evaluación Parcial 2):
 * - TECNICO → registra traslados en terreno (odómetro, cámara, GPS)
 * - JEFE    → valida y audita los traslados
 */
enum class Rol { TECNICO, JEFE }

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

    /** true = cuenta activa, false = deshabilitada */
    val activo: Boolean = true
)
