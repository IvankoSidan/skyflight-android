package com.wheezy.skyflight.feature.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.fragment.app.FragmentActivity
import com.wheezy.skyflight.core.common.manager.BiometricManager
import com.wheezy.skyflight.core.datastore.preferences.AuthPreferences
import com.wheezy.skyflight.core.model.User
import com.wheezy.skyflight.core.ui.snackbar.SnackbarHelper
import com.wheezy.skyflight.feature.auth.domain.usecase.*
import com.wheezy.skyflight.feature.auth.presentation.states.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val googleAuthUseCase: GoogleAuthUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val initializeWebSocketUseCase: InitializeWebSocketUseCase,
    private val authPreferences: AuthPreferences,
    private val biometricManager: BiometricManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState: StateFlow<AuthState> = _registerState.asStateFlow()

    private val _googleAuthState = MutableStateFlow<AuthState>(AuthState.Idle)
    val googleAuthState: StateFlow<AuthState> = _googleAuthState.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _savedEmail = MutableStateFlow<String?>(null)
    val savedEmail: StateFlow<String?> = _savedEmail.asStateFlow()

    private val _savedPassword = MutableStateFlow<String?>(null)
    val savedPassword: StateFlow<String?> = _savedPassword.asStateFlow()

    private val _shouldSaveCredentials = MutableStateFlow(false)
    val shouldSaveCredentials: StateFlow<Boolean> = _shouldSaveCredentials.asStateFlow()

    private val _isBiometricAvailable = MutableStateFlow(false)
    val isBiometricAvailable: StateFlow<Boolean> = _isBiometricAvailable.asStateFlow()

    init {
        viewModelScope.launch {
            authPreferences.tokenFlow.collect { token ->
                if (token != null && authPreferences.isAuthenticated()) {
                    val userId = authPreferences.getUserId()
                    if (userId != null) {
                        val currentUser = getCurrentUserUseCase()
                        if (currentUser != null) {
                            _user.value = currentUser
                            initializeWebSocketUseCase()
                        }
                    }
                }
            }
        }
        viewModelScope.launch {
            val currentUser = getCurrentUserUseCase()
            if (currentUser != null) {
                _user.value = currentUser
                initializeWebSocketUseCase()
            }
            loadSavedCredentials()
        }
    }

    private suspend fun loadSavedCredentials() {
        val email = authPreferences.getSavedEmail()
        val password = authPreferences.getSavedPassword()
        val shouldSave = authPreferences.shouldSaveCredentials()
        _savedEmail.value = email
        _savedPassword.value = password
        _shouldSaveCredentials.value = shouldSave
    }

    fun checkBiometricAvailability(activity: FragmentActivity) {
        val available = biometricManager.checkBiometricAvailability(activity)
        _isBiometricAvailable.value = available
    }

    fun loginWithBiometric(activity: FragmentActivity) {
        biometricManager.authenticate(
            activity = activity,
            title = "SkyFlight Login",
            subtitle = "Use fingerprint to login",
            description = "Quick and secure access to your account",
            onSuccess = {
                viewModelScope.launch {
                    val emailValue = _savedEmail.value
                    val passwordValue = _savedPassword.value
                    if (emailValue != null && passwordValue != null) {
                        login(emailValue, passwordValue, _shouldSaveCredentials.value)
                    } else {
                        SnackbarHelper.showError("No saved credentials")
                    }
                }
            },
            onFailure = { message -> SnackbarHelper.showError(message) },
            onError = { message -> SnackbarHelper.showError(message) }
        )
    }

    fun login(email: String, password: String, saveCredentials: Boolean = false) {
        if (_loginState.value is AuthState.Loading) return
        _loginState.value = AuthState.Loading
        _isLoading.value = true
        viewModelScope.launch {
            val result = loginUseCase(email, password)
            result.onSuccess { user ->
                _user.value = user
                _loginState.value = AuthState.Success(user)
                SnackbarHelper.showSuccess("Login successful!")
                authPreferences.saveCredentials(email, password, saveCredentials)
                user.id?.let { userId ->
                    val newToken = "token_${System.currentTimeMillis()}"
                    authPreferences.saveAuthData(
                        token = newToken,
                        userId = userId,
                        email = email,
                        name = user.name
                    )
                    authPreferences.saveToken(newToken)
                }
                loadSavedCredentials()
                initializeWebSocketUseCase()
            }.onFailure { error ->
                _loginState.value = AuthState.Error(error.message ?: "Login failed")
                SnackbarHelper.showError(error.message ?: "Login failed")
            }
            _isLoading.value = false
        }
    }

    fun register(name: String, email: String, password: String) {
        if (_registerState.value is AuthState.Loading) return
        _registerState.value = AuthState.Loading
        _isLoading.value = true
        viewModelScope.launch {
            val result = registerUseCase(name, email, password)
            result.onSuccess { user ->
                _user.value = user
                _registerState.value = AuthState.Success(user)
                SnackbarHelper.showSuccess("Registration successful!")
                initializeWebSocketUseCase()
            }.onFailure { error ->
                _registerState.value = AuthState.Error(error.message ?: "Registration failed")
                SnackbarHelper.showError(error.message ?: "Registration failed")
            }
            _isLoading.value = false
        }
    }

    fun googleAuth(token: String) {
        if (_googleAuthState.value is AuthState.Loading) return
        _googleAuthState.value = AuthState.Loading
        _isLoading.value = true
        viewModelScope.launch {
            val result = googleAuthUseCase(token)
            result.onSuccess { user ->
                _user.value = user
                _googleAuthState.value = AuthState.Success(user)
                SnackbarHelper.showSuccess("Google sign-in successful!")
                initializeWebSocketUseCase()
            }.onFailure { error ->
                _googleAuthState.value = AuthState.Error(error.message ?: "Google authentication failed")
                SnackbarHelper.showError(error.message ?: "Google authentication failed")
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            logoutUseCase()
            authPreferences.clearAuthData()
            authPreferences.saveCredentials("", "", false)
            _user.value = null
            _loginState.value = AuthState.Idle
            _registerState.value = AuthState.Idle
            _googleAuthState.value = AuthState.Idle
            _isLoading.value = false
            loadSavedCredentials()
            SnackbarHelper.showInfo("You have been logged out")
        }
    }
}