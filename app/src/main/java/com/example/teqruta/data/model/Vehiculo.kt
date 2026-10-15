package com.example.teqruta.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehiculos")
data class Vehiculo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** Patente del vehículo, ej: "ABCD12" */
    val patente: String,

    val marca: String,
    val modelo: String,
    val anio: Int,

    /** Kilometraje actual registrado en el sistema */
    val kmActual: Int = 0,

    /** true = disponible para asignar traslados */
    val disponible: Boolean = true
)
