package com.example.teqruta.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "destinos")
data class Destino(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** Nombre de la clínica u hospital */
    val nombre: String,

    /** Dirección completa */
    val direccion: String,

    /** Ciudad o comuna */
    val ciudad: String,

    /**
     * Distancia oficial en km desde la base TEQMED.
     * Es el valor de referencia para detectar desviaciones de ruta.
     */
    val kmOficial: Double,

    /** Peajes del tramo (separados por coma, ej: "Autopista A1,Peaje B2") */
    val peajesDelTramo: String = "",

    val activo: Boolean = true
)
