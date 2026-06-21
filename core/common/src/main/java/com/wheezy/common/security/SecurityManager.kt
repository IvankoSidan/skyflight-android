package com.wheezy.skyflight.core.common.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityManager @Inject constructor(
    private val rootDetector: RootDetector
) {

    sealed class SecurityStatus {
        object Secure : SecurityStatus()
        data class Compromised(val reason: String) : SecurityStatus()
        object Emulator : SecurityStatus()
    }

    private val _securityStatus: MutableStateFlow<SecurityStatus> = MutableStateFlow<SecurityStatus>(SecurityStatus.Secure)
    val securityStatus: StateFlow<SecurityStatus> = _securityStatus.asStateFlow()

    init {
        checkSecurity()
    }

    fun checkSecurity(): SecurityStatus {
        val status: SecurityStatus = when {
            rootDetector.isEmulator() -> SecurityStatus.Emulator
            rootDetector.isDeviceCompromised() -> SecurityStatus.Compromised(
                buildCompromisedReason()
            )
            else -> SecurityStatus.Secure
        }
        _securityStatus.value = status
        return status
    }

    private fun buildCompromisedReason(): String {
        val reasons = mutableListOf<String>()
        if (rootDetector.isRooted()) reasons.add("Root detected")
        if (rootDetector.isDevelopmentBuild()) reasons.add("Development build")
        if (rootDetector.hasDangerousApps()) reasons.add("Dangerous apps installed")
        return reasons.joinToString(", ")
    }

    fun isSecure(): Boolean {
        return _securityStatus.value is SecurityStatus.Secure
    }
}