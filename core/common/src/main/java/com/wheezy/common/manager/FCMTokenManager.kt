package com.wheezy.skyflight.core.common.manager

interface FCMTokenManager {
    suspend fun saveTokenLocally(token: String)
    suspend fun getLocalToken(): String?
    suspend fun sendTokenToServer(token: String): Boolean
    suspend fun unregisterToken(): Boolean
}