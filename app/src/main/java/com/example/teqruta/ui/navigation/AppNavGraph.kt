package com.example.teqruta.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.teqruta.data.model.Rol
import com.example.teqruta.ui.screens.admin.MantenedoresAdminScreen
import com.example.teqruta.ui.screens.gerencia.DashboardGerenciaScreen
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
    navController: NavHostController,
    authViewModel: AuthViewModel,
    trasladoViewModel: TrasladoViewModel,
    estaOnline: Boolean
) {
    val usuarioActual by authViewModel.usuarioActual.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN
    ) {
        // ── W-01: LOGIN ───────────────────────────────────────────────────────
        composable(Rutas.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                estaOnline = estaOnline,
                onLoginExitoso = { usuario ->
                    when (usuario.rol) {
                        Rol.TECNICO -> navController.navigate(Rutas.HOME_TECNICO) {
                            popUpTo(Rutas.LOGIN) { inclusive = true }
                        }
                        Rol.JEFE -> navController.navigate(Rutas.HOME_JEFE) {
                            popUpTo(Rutas.LOGIN) { inclusive = true }
                        }
                        Rol.GERENCIA -> navController.navigate(Rutas.DASHBOARD) {
                            popUpTo(Rutas.LOGIN) { inclusive = true }
                        }
                        Rol.ADMINISTRADOR -> navController.navigate(Rutas.HOME_ADMIN) {
                            popUpTo(Rutas.LOGIN) { inclusive = true }
                        }
                    }
                }
            )
        }

        // ── W-02 / W-04: HOME TÉCNICO & HISTORIAL ─────────────────────────────
        composable(Rutas.HOME_TECNICO) {
            usuarioActual?.let { user ->
                HomeTecnicoScreen(
                    usuario = user,
                    viewModel = trasladoViewModel,
                    estaOnline = estaOnline,
                    onNuevoTraslado = { navController.navigate(Rutas.NUEVO_TRASLADO) },
                    onFinalizarTraslado = { id -> navController.navigate(Rutas.finalizar(id)) },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        // ── W-02: NUEVO TRASLADO ──────────────────────────────────────────────
        composable(Rutas.NUEVO_TRASLADO) {
            usuarioActual?.let { user ->
                NuevoTrasladoScreen(
                    usuario = user,
                    viewModel = trasladoViewModel,
                    onVolver = { navController.popBackStack() }
                )
            }
        }

        // ── W-03: FINALIZAR TRASLADO ──────────────────────────────────────────
        composable(
            route = Rutas.FINALIZAR,
            arguments = listOf(navArgument("trasladoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val trasladoId = backStackEntry.arguments?.getInt("trasladoId") ?: 0
            FinalizarTrasladoScreen(
                trasladoId = trasladoId,
                viewModel = trasladoViewModel,
                onVolver = { navController.popBackStack() }
            )
        }

        // ── W-05: HOME JEFE (BANDEJA VALIDACIÓN) ──────────────────────────────
        composable(Rutas.HOME_JEFE) {
            usuarioActual?.let { user ->
                HomeJefeScreen(
                    usuario = user,
                    viewModel = trasladoViewModel,
                    estaOnline = estaOnline,
                    onSeleccionarTraslado = { id -> navController.navigate(Rutas.detalleValidacion(id)) },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        // ── W-06: DETALLE Y RESOLUCIÓN DE VIAJE ───────────────────────────────
        composable(
            route = Rutas.DETALLE_VALIDACION,
            arguments = listOf(navArgument("trasladoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val trasladoId = backStackEntry.arguments?.getInt("trasladoId") ?: 0
            DetalleValidacionScreen(
                trasladoId = trasladoId,
                viewModel = trasladoViewModel,
                onVolver = { navController.popBackStack() }
            )
        }

        // ── W-07: DASHBOARD GERENCIA ──────────────────────────────────────────
        composable(Rutas.DASHBOARD) {
            usuarioActual?.let { user ->
                DashboardGerenciaScreen(
                    usuario = user,
                    viewModel = trasladoViewModel,
                    estaOnline = estaOnline,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        // ── W-08: MANTENEDORES ADMIN ──────────────────────────────────────────
        composable(Rutas.HOME_ADMIN) {
            usuarioActual?.let { user ->
                MantenedoresAdminScreen(
                    usuario = user,
                    viewModel = trasladoViewModel,
                    estaOnline = estaOnline,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
