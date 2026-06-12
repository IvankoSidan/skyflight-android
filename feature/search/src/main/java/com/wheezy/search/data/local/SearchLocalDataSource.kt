package com.wheezy.skyflight.feature.search.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wheezy.skyflight.feature.search.domain.model.SearchParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchLocalDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val KEY_FROM = stringPreferencesKey("search_from")
        private val KEY_TO = stringPreferencesKey("search_to")
        private val KEY_PASSENGERS = stringPreferencesKey("search_passengers")
        private val KEY_CLASS = stringPreferencesKey("search_class")
    }

    val searchParamsFlow: Flow<SearchParams> = dataStore.data.map { prefs ->
        SearchParams(
            from = prefs[KEY_FROM] ?: "",
            to = prefs[KEY_TO] ?: "",
            passengers = prefs[KEY_PASSENGERS]?.toIntOrNull() ?: 1,
            selectedClass = prefs[KEY_CLASS] ?: ""
        )
    }

    suspend fun saveSearchParams(params: SearchParams) {
        dataStore.edit { prefs ->
            prefs[KEY_FROM] = params.from
            prefs[KEY_TO] = params.to
            prefs[KEY_PASSENGERS] = params.passengers.toString()
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