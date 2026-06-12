package com.wheezy.skyflight.core.network.interceptor

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkQualityMonitor @Inject constructor(
    private val context: Context
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    enum class NetworkQuality {
        EXCELLENT, GOOD, FAIR, POOR, UNKNOWN
    }

    private val _networkQuality = MutableStateFlow(NetworkQuality.UNKNOWN)
    val networkQuality: StateFlow<NetworkQuality> = _networkQuality.asStateFlow()

    fun getNetworkQuality(): NetworkQuality {
        val network = connectivityManager.activeNetwork ?: return NetworkQuality.UNKNOWN
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkQuality.UNKNOWN

        return when {
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) &&
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_TEMPORARILY_NOT_METERED) -> NetworkQuality.EXCELLENT

            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                    !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) -> NetworkQuality.GOOD

            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) -> NetworkQuality.FAIR

            else -> NetworkQuality.POOR
        }
    }

    fun getTimeoutByQuality(): Long {
        return when (getNetworkQuality()) {
            NetworkQuality.EXCELLENT -> 5000L
            NetworkQuality.GOOD -> 10000L
            NetworkQuality.FAIR -> 15000L
            else -> 20000L
        }
    }
}