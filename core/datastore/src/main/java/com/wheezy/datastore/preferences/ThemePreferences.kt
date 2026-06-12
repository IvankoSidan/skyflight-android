package com.wheezy.skyflight.core.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wheezy.skyflight.core.model.ThemeOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ThemePreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val THEME_KEY = stringPreferencesKey("current_theme")

    val currentTheme: Flow<ThemeOption> = dataStore.data
        .map { prefs ->
            val themeName = prefs[THEME_KEY] ?: ThemeOption.Auto.name
            try {
                ThemeOption.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                ThemeOption.Auto
            }
        }

    suspend fun setTheme(theme: ThemeOption) {
        dataStore.edit { prefs ->
            prefs[THEME_KEY] = theme.name
        }
    }
}