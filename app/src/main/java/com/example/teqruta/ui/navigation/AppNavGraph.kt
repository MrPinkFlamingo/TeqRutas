package com.example.teqruta.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.teqruta.data.model.Rol
import com.example.teqruta.ui.screens.jefe.DetalleValidacionScreen
import com.example.teqruta.ui.screens.jefe.HomeJefeScreen
import com.example.teqruta.ui.screens.login.LoginScreen
import com.example.teqruta.ui.screens.tecnico.FinalizarTrasladoScreen
import com.example.teqruta.ui.screens.tecnico.HomeTecnicoScreen
import com.example.teqruta.ui.screens.tecnico.NuevoTrasladoScreen
import com.example.teqruta.ui.viewmodel.AuthViewModel
import com.example.teqruta.ui.viewmodel.TrasladoViewModel

@Composable
fun AppNavGraph(
    controladorNavegacion: NavHostController,
    authViewModel: AuthViewModel,
    trasladoViewModel: TrasladoViewModel,
    estaOnline: Boolean
) {
    val usuarioActual by authViewModel.usuarioActual.collectAsState()

    NavHost(
        navController = controladorNavegacion,
        startDestination = Rutas.LOGIN
    ) {
        // ── W-01: LOGIN ───────────────────────────────────────────────────────
        composable(Rutas.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                estaOnline = estaOnline,
                onLoginExitoso = { usuario ->
                    when (usuario.rol) {
                        Rol.TECNICO, Rol.GERENCIA, Rol.ADMINISTRADOR -> controladorNavegacion.navigate(Rutas.HOME_TECNICO) {
                            popUpTo(Rutas.LOGIN) { inclusive = true }
                        }
                        Rol.JEFE -> controladorNavegacion.navigate(Rutas.HOME_JEFE) {
                            popUpTo(Rutas.LOGIN) { inclusive = true }
                        }
                    }
                }
            )
        }

        // ── W-02 / W-04: HOME TÉCNICO & HISTORIAL ─────────────────────────────
        composable(Rutas.HOME_TECNICO) {
            val usuario = usuarioActual
            if (usuario != null) {
                HomeTecnicoScreen(
                    usuario = usuario,
                    viewModel = trasladoViewModel,
                    estaOnline = estaOnline,
                    onNuevoTraslado = { controladorNavegacion.navigate(Rutas.NUEVO_TRASLADO) },
                    onFinalizarTraslado = { idTraslado -> controladorNavegacion.navigate(Rutas.finalizar(idTraslado)) },
                    onLogout = {
                        authViewModel.logout()
                        controladorNavegacion.navigate(Rutas.LOGIN) {
                            popUpTo(0)
                            launchSingleTop = true
                        }
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    controladorNavegacion.navigate(Rutas.LOGIN) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            }
        }

        // ── W-02: NUEVO TRASLADO ──────────────────────────────────────────────
        composable(Rutas.NUEVO_TRASLADO) {
            val usuario = usuarioActual
            if (usuario != null) {
                NuevoTrasladoScreen(
                    usuario = usuario,
                    viewModel = trasladoViewModel,
                    onVolver = { controladorNavegacion.popBackStack() }
                )
            } else {
                LaunchedEffect(Unit) {
                    controladorNavegacion.navigate(Rutas.LOGIN) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            }
        }

        // ── W-03: FINALIZAR TRASLADO ──────────────────────────────────────────
        composable(
            route = Rutas.FINALIZAR,
            arguments = listOf(navArgument("trasladoId") { type = NavType.IntType })
        ) { entradaNavegacion ->
            val trasladoId = entradaNavegacion.arguments?.getInt("trasladoId") ?: 0
            FinalizarTrasladoScreen(
                trasladoId = trasladoId,
                viewModel = trasladoViewModel,
                onVolver = { controladorNavegacion.popBackStack() }
            )
        }

        // ── W-05: HOME JEFE (BANDEJA VALIDACIÓN) ──────────────────────────────
        composable(Rutas.HOME_JEFE) {
            val usuario = usuarioActual
            if (usuario != null) {
                HomeJefeScreen(
                    usuario = usuario,
                    viewModel = trasladoViewModel,
                    estaOnline = estaOnline,
                    onSeleccionarTraslado = { idTraslado -> controladorNavegacion.navigate(Rutas.detalleValidacion(idTraslado)) },
                    onLogout = {
                        authViewModel.logout()
                        controladorNavegacion.navigate(Rutas.LOGIN) {
                            popUpTo(0)
                            launchSingleTop = true
                        }
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    controladorNavegacion.navigate(Rutas.LOGIN) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            }
        }

        // ── W-06: DETALLE Y RESOLUCIÓN DE VIAJE ───────────────────────────────
        composable(
            route = Rutas.DETALLE_VALIDACION,
            arguments = listOf(navArgument("trasladoId") { type = NavType.IntType })
        ) { entradaNavegacion ->
            val trasladoId = entradaNavegacion.arguments?.getInt("trasladoId") ?: 0
            DetalleValidacionScreen(
                trasladoId = trasladoId,
                viewModel = trasladoViewModel,
                onVolver = { controladorNavegacion.popBackStack() }
            )
        }
    }
}
