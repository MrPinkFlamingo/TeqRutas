package com.example.teqruta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.teqruta.ui.navigation.AppNavGraph
import com.example.teqruta.ui.theme.GrisClaro
import com.example.teqruta.ui.theme.TeqRutaTheme
import com.example.teqruta.ui.viewmodel.AuthViewModel
import com.example.teqruta.ui.viewmodel.TrasladoViewModel
import com.example.teqruta.util.NetworkObserver

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TeqRutaTheme {
                val observadorRed = remember { NetworkObserver(applicationContext) }
                val estaOnline by observadorRed.estaConectado.collectAsState(initial = true)

                val controladorNavegacion = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val trasladoViewModel: TrasladoViewModel = viewModel()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = GrisClaro
                ) {
                    AppNavGraph(
                        controladorNavegacion = controladorNavegacion,
                        authViewModel = authViewModel,
                        trasladoViewModel = trasladoViewModel,
                        estaOnline = estaOnline
                    )
                }
            }
        }
    }
}