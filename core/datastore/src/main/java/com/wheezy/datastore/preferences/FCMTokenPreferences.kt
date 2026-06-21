package com.wheezy.skyflight.core.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FCMTokenPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val FCM_TOKEN_KEY = stringPreferencesKey("fcm_token")
        private val FCM_TOKEN_SENT_KEY = stringPreferencesKey("fcm_token_sent")
    }

    val tokenFlow: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[FCM_TOKEN_KEY]
        }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[FCM_TOKEN_KEY] = token
        }
    }

    suspend fun markTokenAsSent() {
        dataStore.edit { preferences ->
            preferences[FCM_TOKEN_SENT_KEY] = System.currentTimeMillis().toString()
        }
    }

    suspend fun isTokenSent(): Boolean {
        return dataStore.data.first()[FCM_TOKEN_SENT_KEY] != null
    }

    suspend fun getToken(): String? {
        return dataStore.data.first()[FCM_TOKEN_KEY]
    }

    suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(FCM_TOKEN_KEY)
            preferences.remove(FCM_TOKEN_SENT_KEY)
        }
    }
}