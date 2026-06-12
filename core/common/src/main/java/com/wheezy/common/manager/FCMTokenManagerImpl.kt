package com.wheezy.skyflight.core.common.manager

import com.wheezy.skyflight.core.datastore.preferences.FCMTokenPreferences
import com.wheezy.skyflight.core.network.api.ApiService
import com.wheezy.skyflight.core.network.manager.TokenManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FCMTokenManagerImpl @Inject constructor(
    private val fcmPreferences: FCMTokenPreferences,
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : FCMTokenManager {

    override suspend fun saveTokenLocally(token: String) {
        fcmPreferences.saveToken(token)
    }

    override suspend fun getLocalToken(): String? {
        return fcmPreferences.getToken()
    }

    override suspend fun sendTokenToServer(token: String): Boolean {
        val authToken = tokenManager.getToken()
        if (authToken.isNullOrEmpty()) return false

        return try {
            val response = apiService.registerFCMToken(mapOf("token" to token))
            if (response.isSuccessful) {
                saveTokenLocally(token)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun unregisterToken(): Boolean {
        val authToken = tokenManager.getToken()
        if (authToken.isNullOrEmpty()) return false

        val localToken = getLocalToken() ?: return false

        return try {
            val response = apiService.unregisterFCMToken(mapOf("token" to localToken))
            if (response.isSuccessful) {
                fcmPreferences.clearToken()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}