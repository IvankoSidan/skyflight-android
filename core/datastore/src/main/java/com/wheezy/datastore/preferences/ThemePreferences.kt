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
    private val themeKey = stringPreferencesKey("current_theme")

    val currentTheme: Flow<ThemeOption> = dataStore.data
        .map { prefs ->
            val themeName = prefs[themeKey] ?: ThemeOption.Auto.name
            try {
                ThemeOption.valueOf(themeName)
            } catch (_: IllegalArgumentException) {
                ThemeOption.Auto
            }
        }

    suspend fun setTheme(theme: ThemeOption) {
        dataStore.edit { prefs ->
            prefs[themeKey] = theme.name
        }
    }
}