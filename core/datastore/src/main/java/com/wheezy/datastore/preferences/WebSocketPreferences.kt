package com.wheezy.skyflight.core.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WebSocketPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val WS_AUTO_CONNECT_KEY = booleanPreferencesKey("ws_auto_connect")
        private val WS_RECONNECT_ENABLED_KEY = booleanPreferencesKey("ws_reconnect_enabled")
        private val WS_SUBSCRIBED_KEY = booleanPreferencesKey("ws_subscribed")
    }

    val autoConnect: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[WS_AUTO_CONNECT_KEY] ?: true
        }

    val reconnectEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[WS_RECONNECT_ENABLED_KEY] ?: true
        }

    suspend fun setAutoConnect(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[WS_AUTO_CONNECT_KEY] = enabled
        }
    }

    suspend fun setReconnectEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[WS_RECONNECT_ENABLED_KEY] = enabled
        }
    }

    suspend fun setSubscribed(subscribed: Boolean) {
        dataStore.edit { preferences ->
            preferences[WS_SUBSCRIBED_KEY] = subscribed
        }
    }

    suspend fun isSubscribed(): Boolean {
        return dataStore.data.first()[WS_SUBSCRIBED_KEY] ?: false
    }
}