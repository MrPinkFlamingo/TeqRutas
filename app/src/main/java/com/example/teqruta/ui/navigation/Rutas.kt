package com.example.teqruta.ui.navigation

/** Rutas de navegación de la app TeqRuta */
object Rutas {
    const val LOGIN            = "login"

    // Técnico
    const val HOME_TECNICO     = "home_tecnico"
    const val NUEVO_TRASLADO   = "nuevo_traslado"
    const val FINALIZAR        = "finalizar/{trasladoId}"
    const val HISTORIAL        = "historial"

    // Jefe
    const val HOME_JEFE        = "home_jefe"
    const val DETALLE_VALIDACION = "detalle_validacion/{trasladoId}"

    // Gerencia
    const val DASHBOARD        = "dashboard"

    // Admin
    const val HOME_ADMIN       = "home_admin"

    // Helpers para rutas con parámetros
    fun finalizar(trasladoId: Int)        = "finalizar/$trasladoId"
    fun detalleValidacion(trasladoId: Int) = "detalle_validacion/$trasladoId"
}
