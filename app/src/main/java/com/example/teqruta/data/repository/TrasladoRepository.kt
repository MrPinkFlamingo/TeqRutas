package com.example.teqruta.data.repository

import com.example.teqruta.data.database.TrasladoDao
import com.example.teqruta.data.model.EstadoSync
import com.example.teqruta.data.model.EstadoTraslado
import com.example.teqruta.data.model.Traslado
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de Traslados.
 * Capa intermedia entre el ViewModel y la base de datos Room.
 * Basado en el patrón de Plantilla_01_BaseDatos_Room_MVVM/ProductRepository.kt
 */
class TrasladoRepository(private val dao: TrasladoDao) {

    // Consultas reactivas Flow

    fun getTrasladosDeTecnico(tecnicoId: Int): Flow<List<Traslado>> =
        dao.getByTecnico(tecnicoId)

    fun getPendientesValidacion(): Flow<List<Traslado>> =
        dao.getPendientesValidacion()

    fun getPendientesConDesviacion(): Flow<List<Traslado>> =
        dao.getPendientesConDesviacion()

    fun getTodos(): Flow<List<Traslado>> =
        dao.getAll()

    // Consultas suspendidas
    suspend fun getTrasladoActivo(tecnicoId: Int): Traslado? =
        dao.getTrasladoActivo(tecnicoId)

    suspend fun getById(id: Int): Traslado? =
        dao.getById(id)

    // Escritura de datos

    /** Inicia un nuevo traslado — guarda estado EN_CURSO y sync PENDIENTE */
    suspend fun iniciarTraslado(traslado: Traslado): Long =
        dao.insertar(traslado.copy(
            estado     = EstadoTraslado.EN_CURSO,
            estadoSync = EstadoSync.PENDIENTE_SYNC
        ))

    /** Finaliza traslado: actualiza km llegada, peajes y pasa a estado PENDIENTE */
    suspend fun finalizarTraslado(traslado: Traslado) {
        dao.actualizar(traslado.copy(
            estado          = EstadoTraslado.PENDIENTE,
            estadoSync      = EstadoSync.PENDIENTE_SYNC,
            fechaHoraLlegada = System.currentTimeMillis()
        ))
    }

    /** Jefe aprueba el traslado tal como fue declarado */
    suspend fun aprobarTraslado(id: Int, comentario: String) {
        val traslado = dao.getById(id) ?: return
        dao.actualizar(traslado.copy(
            estado          = EstadoTraslado.APROBADO,
            comentarioJefe  = comentario,
            kmAprobados     = traslado.kmLlegada?.minus(traslado.kmSalida)
        ))
    }

    /** Jefe ajusta los km al oficial del tramo por desvío de ruta */
    suspend fun ajustarAKmOficial(id: Int, kmOficial: Int, comentario: String) {
        val traslado = dao.getById(id) ?: return
        dao.actualizar(traslado.copy(
            estado         = EstadoTraslado.AJUSTADO,
            comentarioJefe = comentario,
            kmAprobados    = kmOficial
        ))
    }

    suspend fun actualizarSync(id: Int, sync: EstadoSync) =
        dao.actualizarSync(id, sync)

    suspend fun eliminar(traslado: Traslado) =
        dao.eliminar(traslado)

    // Calculo de costos de traslado
    /**
     * Calcula el costo total de un traslado.
     * Aplica la fórmula corporativa de TEQMED con tarifas fijas de combustible y peajes.
     *
     * @param kmRecorridos Km declarados (o ajustados por el jefe)
     * @param peajesPagados Lista de peajes marcados por el técnico
     * @param tarifaCombustiblePorKm CLP por km (default ~$150 CLP/km)
     * @param costoPorPeaje CLP por peaje (default ~$2.200 CLP cada uno)
     */
    fun calcularCostos(
        kmRecorridos: Int,
        peajesPagados: List<String>,
        tarifaCombustiblePorKm: Double = 150.0,
        costoPorPeaje: Double = 2200.0
    ): Triple<Double, Double, Double> {
        val combustible = kmRecorridos * tarifaCombustiblePorKm
        val peajes      = peajesPagados.size * costoPorPeaje
        val total       = combustible + peajes
        return Triple(total, combustible, peajes)
    }
}
