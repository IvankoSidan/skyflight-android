package com.wheezy.skyflight.core.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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
        val EMAIL_KEY = stringPreferencesKey("user_email")
        val NAME_KEY = stringPreferencesKey("user_name")
        val PROFILE_PICTURE_KEY = stringPreferencesKey("profile_picture")
        val SAVED_EMAIL_KEY = stringPreferencesKey("saved_email")
        val SAVED_PASSWORD_KEY = stringPreferencesKey("saved_password")
        val SAVE_CREDENTIALS_KEY = booleanPreferencesKey("save_credentials")
    }

    val tokenFlow: Flow<String?> = dataStore.data
        .map { it[TOKEN_KEY] }
        .distinctUntilChanged()

    suspend fun saveAuthData(
        token: String,
        userId: Long,
        email: String,
        name: String? = null,
        profilePicture: String? = null
    ) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
            preferences[EMAIL_KEY] = email
            name?.let { preferences[NAME_KEY] = it }
            profilePicture?.let { preferences[PROFILE_PICTURE_KEY] = it }
        }
    }

    suspend fun saveCredentials(email: String, password: String, save: Boolean) {
        dataStore.edit { preferences ->
            if (save) {
                preferences[SAVED_EMAIL_KEY] = email
                preferences[SAVED_PASSWORD_KEY] = password
                preferences[SAVE_CREDENTIALS_KEY] = true
            } else {
                preferences.remove(SAVED_EMAIL_KEY)
                preferences.remove(SAVED_PASSWORD_KEY)
                preferences[SAVE_CREDENTIALS_KEY] = false
            }
        }
    }

    suspend fun getSavedEmail(): String? {
        return dataStore.data.first()[SAVED_EMAIL_KEY]
    }

    suspend fun getSavedPassword(): String? {
        return dataStore.data.first()[SAVED_PASSWORD_KEY]
    }

    suspend fun shouldSaveCredentials(): Boolean {
        return dataStore.data.first()[SAVE_CREDENTIALS_KEY] ?: false
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun getToken(): String? {
        return dataStore.data.first()[TOKEN_KEY]
    }

    suspend fun getUserId(): Long? {
        return dataStore.data.first()[USER_ID_KEY]
    }

    suspend fun clearAuthData() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(EMAIL_KEY)
            preferences.remove(NAME_KEY)
            preferences.remove(PROFILE_PICTURE_KEY)
        }
    }

    suspend fun isAuthenticated(): Boolean {
        val preferences = dataStore.data.first()
        val token = preferences[TOKEN_KEY]
        return !token.isNullOrEmpty()
    }
}