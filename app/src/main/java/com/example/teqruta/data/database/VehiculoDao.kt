package com.example.teqruta.data.database

import androidx.room.*
import com.example.teqruta.data.model.Vehiculo
import kotlinx.coroutines.flow.Flow

@Dao
interface VehiculoDao {

    @Query("SELECT * FROM vehiculos WHERE disponible = 1 ORDER BY patente ASC")
    fun getDisponibles(): Flow<List<Vehiculo>>

    @Query("SELECT * FROM vehiculos ORDER BY patente ASC")
    fun getAll(): Flow<List<Vehiculo>>

    @Query("SELECT * FROM vehiculos WHERE id = :id")
    suspend fun getById(id: Int): Vehiculo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(vehiculo: Vehiculo)

    @Update
    suspend fun actualizar(vehiculo: Vehiculo)

    @Delete
    suspend fun eliminar(vehiculo: Vehiculo)
}
