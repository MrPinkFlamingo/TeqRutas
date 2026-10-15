package com.example.teqruta.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Observador reactivo de conectividad a Internet.
 * Permite que la app opere en modo Offline-First (RNF-01)
 * detectando automáticamente cuando vuelve la señal para sincronizar.
 */
class NetworkObserver(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val estaConectado: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                trySend(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                trySend(false)
            }
        }

        // Estado inicial
        val redActual = connectivityManager.activeNetwork
        val capacidades = connectivityManager.getNetworkCapabilities(redActual)
        val conectadoInicial = capacidades?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        trySend(conectadoInicial)

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
}
