package com.example.teqruta.data.database

import androidx.room.*
import com.example.teqruta.data.model.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuarios WHERE activo = 1")
    fun getAllActivos(): Flow<List<Usuario>>

    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun getById(id: Int): Usuario?

    /** Login por RUT/correo + contraseña */
    @Query("SELECT * FROM usuarios WHERE (rut = :credencial OR correo = :credencial) AND contrasena = :contrasena AND activo = 1 LIMIT 1")
    suspend fun login(credencial: String, contrasena: String): Usuario?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(usuario: Usuario)

    @Update
    suspend fun actualizar(usuario: Usuario)

    @Delete
    suspend fun eliminar(usuario: Usuario)
}
