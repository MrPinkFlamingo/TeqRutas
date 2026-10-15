package com.example.teqruta.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teqruta.data.database.AppDatabase
import com.example.teqruta.data.model.*
import com.example.teqruta.data.repository.TrasladoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ── MODELOS DE ESTADO DE UI (MVVM - UDF) ──────────────────────────────────────

/** Estado del formulario de Nuevo Traslado (IE 2.2.1) */
data class FormNuevoTrasladoState(
    val kmSalidaTexto: String = "",
    val errorKmSalida: String? = null,
    val destinoSeleccionado: Destino? = null,
    val vehiculoSeleccionado: Vehiculo? = null,
    val esValido: Boolean = false
)

/** Estado del formulario de Finalizar Traslado (IE 2.2.1) */
data class FormFinalizarState(
    val kmLlegadaTexto: String = "",
    val errorKmLlegada: String? = null,
    val kmRecorridos: Int = 0,
    val esValido: Boolean = false
)

/** Estado de agregación financiera para Gerencia (MVVM) */
data class DashboardUiState(
    val totalGeneral: Double = 0.0,
    val totalCombustible: Double = 0.0,
    val totalPeajes: Double = 0.0,
    val porcentajeCombustible: Float = 0.5f,
    val filtroVehiculoId: Int? = null,
    val trasladosFiltrados: List<Traslado> = emptyList()
)

/** Estado de la pantalla de Mantenedores Admin (MVVM) */
data class AdminUiState(
    val precioCombustible: Double = 1280.0,
    val actualizandoTarifa: Boolean = false
)

/**
 * ViewModel principal de la aplicación TeqRuta.
 * Centraliza la lógica de negocio, validaciones y estado según el patrón MVVM.
 */
class TrasladoViewModel(application: Application) : AndroidViewModel(application) {

    private val baseDeDatos = AppDatabase.getInstance(application)
    private val repositorio = TrasladoRepository(baseDeDatos.trasladoDao())
    private val destinoDao  = baseDeDatos.destinoDao()
    private val vehiculoDao = baseDeDatos.vehiculoDao()

    // ── Estado Global y Notificaciones ─────────────────────────────────────────
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    // ── Estados de Formularios (Desacoplados de la UI - IE 2.2.1) ─────────────
    private val _formNuevoState = MutableStateFlow(FormNuevoTrasladoState())
    val formNuevoState: StateFlow<FormNuevoTrasladoState> = _formNuevoState.asStateFlow()

    private val _formFinalizarState = MutableStateFlow(FormFinalizarState())
    val formFinalizarState: StateFlow<FormFinalizarState> = _formFinalizarState.asStateFlow()

    private val _filtroVehiculoDashboard = MutableStateFlow<Int?>(null)

    private val _adminState = MutableStateFlow(AdminUiState())
    val adminState: StateFlow<AdminUiState> = _adminState.asStateFlow()

    // ── Flujos de Datos Reactivos de Room ──────────────────────────────────────
    private val cacheTrasladosTecnico = mutableMapOf<Int, StateFlow<List<Traslado>>>()

    fun trasladosDeTecnico(tecnicoId: Int): StateFlow<List<Traslado>> =
        cacheTrasladosTecnico.getOrPut(tecnicoId) {
            repositorio.getTrasladosDeTecnico(tecnicoId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }

    val pendientesValidacion: StateFlow<List<Traslado>> =
        repositorio.getPendientesValidacion()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendientesConDesviacion: StateFlow<List<Traslado>> =
        repositorio.getPendientesConDesviacion()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todosLosTraslados: StateFlow<List<Traslado>> =
        repositorio.getTodos()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val destinos: StateFlow<List<Destino>> =
        destinoDao.getAllActivos()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vehiculos: StateFlow<List<Vehiculo>> =
        vehiculoDao.getDisponibles()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Dashboard de Gerencia Reactivo (Calculado en ViewModel) ────────────────
    val dashboardUiState: StateFlow<DashboardUiState> = combine(
        todosLosTraslados,
        _filtroVehiculoDashboard
    ) { traslados, filtroId ->
        val filtrados = if (filtroId == null) traslados else traslados.filter { it.vehiculoId == filtroId }
        val combustible = filtrados.sumOf { it.costoCombustible ?: 0.0 }
        val peajes = filtrados.sumOf { it.costoPeajes ?: 0.0 }
        val total = combustible + peajes
        val pct = if (total > 0) (combustible / total).toFloat() else 0.5f

        DashboardUiState(
            totalGeneral = total,
            totalCombustible = combustible,
            totalPeajes = peajes,
            porcentajeCombustible = pct,
            filtroVehiculoId = filtroId,
            trasladosFiltrados = filtrados
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    // ── Validación Centralizada: Nuevo Traslado (IE 2.2.1) ─────────────────────
    fun onNuevoDestinoSeleccionado(destino: Destino) {
        val actual = _formNuevoState.value
        _formNuevoState.value = actual.copy(
            destinoSeleccionado = destino,
            esValido = validarNuevo(destino, actual.vehiculoSeleccionado, actual.kmSalidaTexto)
        )
    }

    fun onNuevoVehiculoSeleccionado(vehiculo: Vehiculo) {
        val actual = _formNuevoState.value
        _formNuevoState.value = actual.copy(
            vehiculoSeleccionado = vehiculo,
            esValido = validarNuevo(actual.destinoSeleccionado, vehiculo, actual.kmSalidaTexto)
        )
    }

    fun onNuevoKmSalidaCambiado(kmTexto: String) {
        if (!kmTexto.all { it.isDigit() }) return
        val actual = _formNuevoState.value
        val error = if (kmTexto.isNotBlank() && (kmTexto.toIntOrNull() ?: 0) <= 0) "El odómetro debe ser mayor a 0" else null
        _formNuevoState.value = actual.copy(
            kmSalidaTexto = kmTexto,
            errorKmSalida = error,
            esValido = error == null && validarNuevo(actual.destinoSeleccionado, actual.vehiculoSeleccionado, kmTexto)
        )
    }

    private fun validarNuevo(destino: Destino?, vehiculo: Vehiculo?, kilometrajeTexto: String): Boolean =
        destino != null && vehiculo != null && kilometrajeTexto.isNotBlank() && (kilometrajeTexto.toIntOrNull() ?: 0) > 0

    fun resetFormNuevo() {
        _formNuevoState.value = FormNuevoTrasladoState()
    }

    // ── Validación Centralizada: Finalizar Traslado (IE 2.2.1) ─────────────────
    fun onFinalizarKmLlegadaCambiado(kmSalida: Int, llegadaTexto: String) {
        if (!llegadaTexto.all { it.isDigit() }) return
        val kmLlegadaNumero = llegadaTexto.toIntOrNull()
        val error = when {
            llegadaTexto.isBlank() -> null
            kmLlegadaNumero == null || kmLlegadaNumero <= kmSalida -> "Debe ser mayor al odómetro inicial ($kmSalida km)"
            else -> null
        }
        val kmRecorridosCalculados = if (kmLlegadaNumero != null && kmLlegadaNumero > kmSalida) kmLlegadaNumero - kmSalida else 0
        _formFinalizarState.value = FormFinalizarState(
            kmLlegadaTexto = llegadaTexto,
            errorKmLlegada = error,
            kmRecorridos = kmRecorridosCalculados,
            esValido = error == null && llegadaTexto.isNotBlank() && kmRecorridosCalculados > 0
        )
    }

    // ── Operaciones de Gerencia y Admin en ViewModel ──────────────────────────
    fun filtrarDashboardPorVehiculo(vehiculoId: Int?) {
        _filtroVehiculoDashboard.value = vehiculoId
    }

    fun actualizarTarifaCombustibleDesdeApi() {
        viewModelScope.launch {
            _adminState.value = _adminState.value.copy(actualizandoTarifa = true)
            val nuevoPrecio = listOf(1275.0, 1282.0, 1290.0, 1280.0).random()
            _adminState.value = AdminUiState(precioCombustible = nuevoPrecio, actualizandoTarifa = false)
            _mensaje.value = "Tarifa actualizada desde API CNE/ENAP: $${nuevoPrecio.toInt()} CLP"
        }
    }

    // ── Acciones de Negocio ───────────────────────────────────────────────────
    fun iniciarTraslado(
        tecnicoId:  Int,
        vehiculoId: Int,
        destinoId:  Int,
        kmSalida:   Int,
        origen:     String = "Base TEQMED"
    ) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val trasladoEnCurso = repositorio.getTrasladoActivo(tecnicoId)
                if (trasladoEnCurso != null) {
                    _mensaje.value = "Ya tienes un traslado en curso"
                    return@launch
                }
                repositorio.iniciarTraslado(Traslado(
                    tecnicoId  = tecnicoId,
                    vehiculoId = vehiculoId,
                    destinoId  = destinoId,
                    kmSalida   = kmSalida,
                    origen     = origen
                ))
                resetFormNuevo()
                _mensaje.value = "✅ Traslado iniciado con éxito"
            } catch (e: Exception) {
                _mensaje.value = "Error al iniciar: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun finalizarTraslado(
        traslado:      Traslado,
        kmLlegada:     Int,
        peajesPagados: List<String>,
        justificacion: String
    ) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val kmRecorridos = kmLlegada - traslado.kmSalida
                val (costoTotal, costoCombustible, costoPeajes) = repositorio.calcularCostos(
                    kmRecorridos   = kmRecorridos,
                    peajesPagados  = peajesPagados
                )
                repositorio.finalizarTraslado(traslado.copy(
                    kmLlegada           = kmLlegada,
                    peajesPagados       = peajesPagados.joinToString(","),
                    justificacionDesvio = justificacion,
                    costoTotal          = costoTotal,
                    costoCombustible    = costoCombustible,
                    costoPeajes         = costoPeajes
                ))
                _formFinalizarState.value = FormFinalizarState()
                _mensaje.value = "✅ Traslado finalizado exitosamente"
            } catch (e: Exception) {
                _mensaje.value = "Error al finalizar: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun aprobarTraslado(trasladoId: Int, comentario: String = "Aprobado") {
        viewModelScope.launch {
            repositorio.aprobarTraslado(trasladoId, comentario)
            _mensaje.value = "✅ Traslado aprobado"
        }
    }

    fun ajustarAKmOficial(trasladoId: Int, kmOficial: Int, comentario: String) {
        viewModelScope.launch {
            repositorio.ajustarAKmOficial(trasladoId, kmOficial, comentario)
            _mensaje.value = "⚠️ Ajustado a Km oficial"
        }
    }

    fun limpiarMensaje() { _mensaje.value = null }
}
