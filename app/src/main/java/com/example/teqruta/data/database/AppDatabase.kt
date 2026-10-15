package com.example.teqruta.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.teqruta.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Conversores de tipo para enums
class Converters {
    @TypeConverter fun fromRol(rol: Rol): String = rol.name
    @TypeConverter fun toRol(value: String): Rol = Rol.valueOf(value)

    @TypeConverter fun fromEstadoTraslado(e: EstadoTraslado): String = e.name
    @TypeConverter fun toEstadoTraslado(value: String): EstadoTraslado = EstadoTraslado.valueOf(value)

    @TypeConverter fun fromEstadoSync(e: EstadoSync): String = e.name
    @TypeConverter fun toEstadoSync(value: String): EstadoSync = EstadoSync.valueOf(value)
}

// Base de datos principal Room
@Database(
    entities  = [Usuario::class, Vehiculo::class, Destino::class, Traslado::class],
    version   = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun vehiculoDao(): VehiculoDao
    abstract fun destinoDao(): DestinoDao
    abstract fun trasladoDao(): TrasladoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "teqruta_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // Carga de datos iniciales
    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    poblarDatosDePrueba(database)
                }
            }
        }
    }
}

/**
 * Inserta datos de prueba al crear la base de datos por primera vez.
 * Incluye usuarios de cada rol, vehículos y destinos de ejemplo.
 */
private suspend fun poblarDatosDePrueba(db: AppDatabase) {
    val usuarioDao  = db.usuarioDao()
    val vehiculoDao = db.vehiculoDao()
    val destinoDao  = db.destinoDao()

    // Usuarios de prueba
    usuarioDao.insertar(Usuario(
        rut = "12345678-9", nombre = "Juan", apellido = "Pérez",
        correo = "tecnico@teqmed.cl", contrasena = "1234", rol = Rol.TECNICO
    ))
    usuarioDao.insertar(Usuario(
        rut = "98765432-1", nombre = "María", apellido = "González",
        correo = "jefe@teqmed.cl", contrasena = "1234", rol = Rol.JEFE
    ))


    // Vehículos de prueba
    vehiculoDao.insertar(Vehiculo(
        patente = "ABCD12", marca = "Toyota", modelo = "Hilux",
        anio = 2022, kmActual = 45230
    ))
    vehiculoDao.insertar(Vehiculo(
        patente = "WXYZ34", marca = "Chevrolet", modelo = "D-Max",
        anio = 2021, kmActual = 78500
    ))

    // Destinos de prueba
    destinoDao.insertar(Destino(
        nombre = "Hospital Las Higueras", direccion = "Av. Las Higueras 544",
        ciudad = "Talcahuano", kmOficial = 12.5,
        peajesDelTramo = "Peaje Autopista Concepción"
    ))
    destinoDao.insertar(Destino(
        nombre = "Clínica Bíobío", direccion = "Av. Los Carrera 1405",
        ciudad = "Concepción", kmOficial = 8.0,
        peajesDelTramo = ""
    ))
    destinoDao.insertar(Destino(
        nombre = "Hospital Regional de Concepción", direccion = "San Martín 1436",
        ciudad = "Concepción", kmOficial = 10.3,
        peajesDelTramo = ""
    ))
}
