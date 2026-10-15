package com.example.teqruta.data.database

import androidx.room.*
import com.example.teqruta.data.model.Destino
import kotlinx.coroutines.flow.Flow

@Dao
interface DestinoDao {

    @Query("SELECT * FROM destinos WHERE activo = 1 ORDER BY nombre ASC")
    fun getAllActivos(): Flow<List<Destino>>

    @Query("SELECT * FROM destinos ORDER BY nombre ASC")
    fun getAll(): Flow<List<Destino>>

    @Query("SELECT * FROM destinos WHERE id = :id")
    suspend fun getById(id: Int): Destino?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(destino: Destino)

    @Update
    suspend fun actualizar(destino: Destino)

    @Delete
    suspend fun eliminar(destino: Destino)
}
