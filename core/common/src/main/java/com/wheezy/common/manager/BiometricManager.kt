package com.wheezy.skyflight.core.common.manager

import android.os.Build
import androidx.biometric.BiometricManager.Authenticators
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.wheezy.skyflight.core.common.security.SecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricManager @Inject constructor(
    private val securityManager: SecurityManager
) {

    private val _isAvailable = MutableStateFlow(false)

    val isAvailable: StateFlow<Boolean> =
        _isAvailable.asStateFlow()

    private val executor: Executor =
        Executors.newSingleThreadExecutor()

    fun checkBiometricAvailability(
        activity: FragmentActivity
    ): Boolean {

        if (!securityManager.isSecure()) {
            _isAvailable.value = false
            return false
        }

        val manager =
            androidx.biometric.BiometricManager.from(activity)

        val authenticators =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Authenticators.BIOMETRIC_STRONG or
                        Authenticators.DEVICE_CREDENTIAL
            } else {
                Authenticators.BIOMETRIC_STRONG
            }

        val available =
            manager.canAuthenticate(authenticators) ==
                    androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS

        _isAvailable.value = available

        return available
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        description: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit,
        onError: (String) -> Unit
    ) {

        if (!_isAvailable.value) {
            onFailure("Biometric authentication unavailable")
            return
        }

        val promptInfo =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription(description)
                .apply {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        setAllowedAuthenticators(
                            Authenticators.BIOMETRIC_STRONG or
                                    Authenticators.DEVICE_CREDENTIAL
                        )
                    } else {
                        setNegativeButtonText("Cancel")
                    }

                }
                .build()

        val prompt =
            BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {

                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        onSuccess()
                    }

                    override fun onAuthenticationFailed() {
                        onFailure("Authentication failed")
                    }

                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {
                        onError(errString.toString())
                    }
                }
            )

        prompt.authenticate(promptInfo)
    }
}