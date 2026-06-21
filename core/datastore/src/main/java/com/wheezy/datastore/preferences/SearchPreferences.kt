package com.wheezy.skyflight.core.datastore.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class SearchParams(
    val from: String = "",
    val to: String = "",
    val passengers: Int = 1,
    val selectedClass: String = ""
)

class SearchPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_FROM = stringPreferencesKey("search_from")
        val KEY_TO = stringPreferencesKey("search_to")
        val KEY_PASSENGERS = intPreferencesKey("search_passengers")
        val KEY_CLASS = stringPreferencesKey("search_class")
    }

    val searchParamsFlow: Flow<SearchParams> = dataStore.data.map { prefs ->
        SearchParams(
            from = prefs[KEY_FROM] ?: "",
            to = prefs[KEY_TO] ?: "",
            passengers = prefs[KEY_PASSENGERS] ?: 1,
            selectedClass = prefs[KEY_CLASS] ?: ""
        )
    }

    suspend fun saveSearchParams(params: SearchParams) {
        dataStore.edit { prefs ->
            prefs[KEY_FROM] = params.from
            prefs[KEY_TO] = params.to
            prefs[KEY_PASSENGERS] = params.passengers
            prefs[KEY_CLASS] = params.selectedClass
        }
    }

    suspend fun clearSearchParams() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_FROM)
            prefs.remove(KEY_TO)
            prefs.remove(KEY_PASSENGERS)
            prefs.remove(KEY_CLASS)
        }
    }
}