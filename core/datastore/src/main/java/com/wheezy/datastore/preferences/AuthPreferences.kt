package com.wheezy.skyflight.core.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AuthPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val TOKEN_KEY = stringPreferencesKey("auth_token")
        val USER_ID_KEY = longPreferencesKey("user_id")
    }

    val tokenFlow: Flow<String?> = dataStore.data
        .map { it[TOKEN_KEY] }
        .distinctUntilChanged()

    suspend fun saveAuthData(token: String, userId: Long) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
        }
    }

    suspend fun clearAuthData() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
        }
    }

    suspend fun isAuthenticated(): Boolean {
        val preferences = dataStore.data.first()
        val token = preferences[TOKEN_KEY]
        return !token.isNullOrEmpty()
    }
}