package com.wheezy.skyflight.feature.search.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheezy.skyflight.core.datastore.preferences.SearchParams
import com.wheezy.skyflight.core.datastore.preferences.SearchPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchParamsViewModel @Inject constructor(
    private val searchPreferences: SearchPreferences
) : ViewModel() {

    private val _from = MutableStateFlow("")
    val from: StateFlow<String> = _from.asStateFlow()

    private val _to = MutableStateFlow("")
    val to: StateFlow<String> = _to.asStateFlow()

    private val _passengers = MutableStateFlow(1)
    val passengers: StateFlow<Int> = _passengers.asStateFlow()

    private val _selectedClass = MutableStateFlow("")
    val selectedClass: StateFlow<String> = _selectedClass.asStateFlow()

    init {
        viewModelScope.launch {
            searchPreferences.searchParamsFlow.collect { params ->
                _from.value = params.from
                _to.value = params.to
                _passengers.value = params.passengers
                _selectedClass.value = params.selectedClass
            }
        }
    }

    fun setParams(
        from: String,
        to: String,
        passengers: Int,
        selectedClass: String
    ) {
        _from.value = from
        _to.value = to
        _passengers.value = passengers
        _selectedClass.value = selectedClass

        viewModelScope.launch {
            searchPreferences.saveSearchParams(
                SearchParams(
                    from = from,
                    to = to,
                    passengers = passengers,
                    selectedClass = selectedClass
                )
            )
        }
    }

    fun clear() {
        _from.value = ""
        _to.value = ""
        _passengers.value = 1
        _selectedClass.value = ""

        viewModelScope.launch {
            searchPreferences.clearSearchParams()
        }
    }
}