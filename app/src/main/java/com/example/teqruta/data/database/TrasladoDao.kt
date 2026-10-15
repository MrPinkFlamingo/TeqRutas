package com.example.teqruta.data.database

import androidx.room.*
import com.example.teqruta.data.model.EstadoSync
import com.example.teqruta.data.model.EstadoTraslado
import com.example.teqruta.data.model.Traslado
import kotlinx.coroutines.flow.Flow

@Dao
interface TrasladoDao {

    /** Todos los traslados de un técnico, del más reciente al más antiguo */
    @Query("SELECT * FROM traslados WHERE tecnicoId = :tecnicoId ORDER BY fechaHoraSalida DESC")
    fun getByTecnico(tecnicoId: Int): Flow<List<Traslado>>

    /** Traslados en curso de un técnico (solo debería haber 1 a la vez) */
    @Query("SELECT * FROM traslados WHERE tecnicoId = :tecnicoId AND estado = 'EN_CURSO' LIMIT 1")
    suspend fun getTrasladoActivo(tecnicoId: Int): Traslado?

    /** Todos los traslados pendientes de validación — para la bandeja del Jefe */
    @Query("SELECT * FROM traslados WHERE estado = 'PENDIENTE' ORDER BY fechaHoraLlegada ASC")
    fun getPendientesValidacion(): Flow<List<Traslado>>

    /** Traslados con desviación de ruta pendientes — para alertas del Jefe */
    @Query("""
        SELECT t.* FROM traslados t
        INNER JOIN destinos d ON t.destinoId = d.id
        WHERE t.estado = 'PENDIENTE'
        AND t.justificacionDesvio != ''
        ORDER BY t.fechaHoraLlegada ASC
    """)
    fun getPendientesConDesviacion(): Flow<List<Traslado>>

    /** Todos los traslados — para reportes de Gerencia */
    @Query("SELECT * FROM traslados ORDER BY fechaHoraSalida DESC")
    fun getAll(): Flow<List<Traslado>>

    @Query("SELECT * FROM traslados WHERE id = :id")
    suspend fun getById(id: Int): Traslado?

    /** Traslados pendientes de sincronizar (guardados solo offline) */
    @Query("SELECT * FROM traslados WHERE estadoSync = 'PENDIENTE_SYNC'")
    suspend fun getPendientesSync(): List<Traslado>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(traslado: Traslado): Long

    @Update
    suspend fun actualizar(traslado: Traslado)

    @Query("UPDATE traslados SET estadoSync = :sync WHERE id = :id")
    suspend fun actualizarSync(id: Int, sync: EstadoSync)

    @Query("UPDATE traslados SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Int, estado: EstadoTraslado)

    @Delete
    suspend fun eliminar(traslado: Traslado)
}
