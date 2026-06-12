package com.wheezy.skyflight.feature.auth.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val initializeWebSocketUseCase: InitializeWebSocketUseCase
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

    init {
        viewModelScope.launch {
            val currentUser = getCurrentUserUseCase()
            if (currentUser != null) {
                _user.value = currentUser
                initializeWebSocketUseCase()
            }
        }
    }

    fun login(email: String, password: String) {
        if (_loginState.value is AuthState.Loading) return
        _loginState.value = AuthState.Loading

        viewModelScope.launch {
            val result = loginUseCase(email, password)
            result.onSuccess { user ->
                _user.value = user
                _loginState.value = AuthState.Success(user)
                SnackbarHelper.showSuccess("Login successful!")
                initializeWebSocketUseCase()
            }.onFailure { error ->
                _loginState.value = AuthState.Error(error.message ?: "Login failed")
                SnackbarHelper.showError(error.message ?: "Login failed")
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        if (_registerState.value is AuthState.Loading) return
        _registerState.value = AuthState.Loading

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
        }
    }

    fun googleAuth(token: String) {
        if (_googleAuthState.value is AuthState.Loading) return
        _googleAuthState.value = AuthState.Loading

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
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            logoutUseCase()
            _user.value = null
            _loginState.value = AuthState.Idle
            _registerState.value = AuthState.Idle
            _googleAuthState.value = AuthState.Idle
            _isLoading.value = false
            SnackbarHelper.showInfo("You have been logged out")
        }
    }

    fun clearErrors() {
        if (_loginState.value is AuthState.Error) {
            _loginState.value = AuthState.Idle
        }
        if (_registerState.value is AuthState.Error) {
            _registerState.value = AuthState.Idle
        }
        if (_googleAuthState.value is AuthState.Error) {
            _googleAuthState.value = AuthState.Idle
        }
    }
}