package com.wheezy.skyflight.core.common.manager

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.annotation.SuppressLint
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        @SuppressLint("MissingPermission")
        override fun onAvailable(network: Network) {
            if (hasNetworkPermission()) {
                _isConnected.value = true
            }
        }

        @SuppressLint("MissingPermission")
        override fun onLost(network: Network) {
            if (hasNetworkPermission()) {
                _isConnected.value = false
            }
        }

        @SuppressLint("MissingPermission")
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            if (hasNetworkPermission()) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                _isConnected.value = hasInternet
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun register() {
        if (!hasNetworkPermission()) {
            _isConnected.value = false
            return
        }

        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
            _isConnected.value = isNetworkAvailable()
        } catch (e: SecurityException) {
            _isConnected.value = false
        }
    }

    fun unregister() {
        if (!hasNetworkPermission()) return

        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // Callback already unregistered
        }
    }

    @SuppressLint("MissingPermission")
    private fun isNetworkAvailable(): Boolean {
        if (!hasNetworkPermission()) return false

        return try {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
        } catch (e: SecurityException) {
            false
        }
    }

    private fun hasNetworkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_NETWORK_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }
}