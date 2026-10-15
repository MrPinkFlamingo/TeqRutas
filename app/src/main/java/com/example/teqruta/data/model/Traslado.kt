package com.example.teqruta.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Estados del ciclo de vida de un traslado:
 * EN_CURSO     → técnico inició viaje, no ha llegado
 * FINALIZADO   → técnico llegó, llenó km final y peajes
 * PENDIENTE    → esperando validación del Jefe
 * APROBADO     → Jefe aprobó los kilómetros declarados
 * AJUSTADO     → Jefe corrigió a km oficial por desvío
 * RECHAZADO    → Jefe rechazó el traslado
 */
enum class EstadoTraslado {
    EN_CURSO, FINALIZADO, PENDIENTE, APROBADO, AJUSTADO, RECHAZADO
}

/**
 * Estado de sincronización con el servidor:
 * SINCRONIZADO    → guardado en servidor correctamente
 * PENDIENTE_SYNC  → guardado solo localmente (modo offline)
 * ERROR_SYNC      → falló la sincronización, requiere reintento
 */
enum class EstadoSync {
    SINCRONIZADO, PENDIENTE_SYNC, ERROR_SYNC
}

@Entity(
    tableName = "traslados",
    foreignKeys = [
        ForeignKey(
            entity        = Usuario::class,
            parentColumns = ["id"],
            childColumns  = ["tecnicoId"],
            onDelete      = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity        = Vehiculo::class,
            parentColumns = ["id"],
            childColumns  = ["vehiculoId"],
            onDelete      = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity        = Destino::class,
            parentColumns = ["id"],
            childColumns  = ["destinoId"],
            onDelete      = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("tecnicoId"),
        Index("vehiculoId"),
        Index("destinoId")
    ]
)
data class Traslado(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** FK → usuarios.id */
    val tecnicoId: Int,

    /** FK → vehiculos.id */
    val vehiculoId: Int,

    /** FK → destinos.id */
    val destinoId: Int,

    // Datos de salida
    /** Odómetro al salir de la base, en km */
    val kmSalida: Int,

    /** Timestamp Unix de inicio del viaje */
    val fechaHoraSalida: Long = System.currentTimeMillis(),

    /** Origen del viaje (ej: "Base TEQMED Santiago") */
    val origen: String = "Base TEQMED",

    // Datos de llegada
    /** Odómetro al llegar al destino, en km */
    val kmLlegada: Int? = null,

    /** Timestamp Unix de fin del viaje */
    val fechaHoraLlegada: Long? = null,

    /** Lista de peajes marcados como pagados (separados por coma) */
    val peajesPagados: String = "",

    /** Justificación si los km declarados superan el km oficial del tramo */
    val justificacionDesvio: String = "",

    // Validacion del jefe
    /** Comentario del Jefe al aprobar o ajustar */
    val comentarioJefe: String = "",

    /** Km finales aprobados (pueden diferir de kmLlegada - kmSalida si hay ajuste) */
    val kmAprobados: Int? = null,

    // Estados
    val estado: EstadoTraslado = EstadoTraslado.EN_CURSO,
    val estadoSync: EstadoSync = EstadoSync.PENDIENTE_SYNC,

    // Costos calculados
    /** Costo total calculado en CLP (combustible + peajes) */
    val costoTotal: Double? = null,

    /** Costo solo combustible en CLP */
    val costoCombustible: Double? = null,

    /** Costo total de peajes en CLP */
    val costoPeajes: Double? = null
)
