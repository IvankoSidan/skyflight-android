package com.wheezy.skyflight.feature.search.presentation.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SearchParamsViewModel : ViewModel() {

    private val _from = MutableStateFlow("")
    val from: StateFlow<String> = _from.asStateFlow()

    private val _to = MutableStateFlow("")
    val to: StateFlow<String> = _to.asStateFlow()

    private val _passengers = MutableStateFlow(1)
    val passengers: StateFlow<Int> = _passengers.asStateFlow()

    private val _selectedClass = MutableStateFlow("")
    val selectedClass: StateFlow<String> = _selectedClass.asStateFlow()

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
    }

    fun clear() {
        _from.value = ""
        _to.value = ""
        _passengers.value = 1
        _selectedClass.value = ""
    }
}