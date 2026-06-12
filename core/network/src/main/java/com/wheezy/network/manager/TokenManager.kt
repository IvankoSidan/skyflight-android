package com.wheezy.skyflight.core.network.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor() {

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    fun getToken(): String? = _token.value

    suspend fun updateToken(newToken: String?) {
        _token.value = newToken
    }

    suspend fun clearToken() {
        _token.value = null
    }
}