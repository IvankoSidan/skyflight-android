package com.wheezy.skyflight.feature.cards.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheezy.skyflight.core.ui.snackbar.SnackbarHelper
import com.wheezy.skyflight.feature.cards.domain.usecase.DeleteCardUseCase
import com.wheezy.skyflight.feature.cards.domain.usecase.GetSavedCardsUseCase
import com.wheezy.skyflight.feature.cards.domain.usecase.SetDefaultCardUseCase
import com.wheezy.skyflight.feature.cards.presentation.states.DeleteCardState
import com.wheezy.skyflight.feature.cards.presentation.states.SavedCardsState
import com.wheezy.skyflight.feature.cards.presentation.states.SetDefaultCardState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardsViewModel @Inject constructor(
    private val getSavedCardsUseCase: GetSavedCardsUseCase,
    private val deleteCardUseCase: DeleteCardUseCase,
    private val setDefaultCardUseCase: SetDefaultCardUseCase
) : ViewModel() {

    private val _savedCardsState = MutableStateFlow<SavedCardsState>(SavedCardsState.Loading)
    val savedCardsState: StateFlow<SavedCardsState> = _savedCardsState.asStateFlow()

    private val _deleteCardState = MutableStateFlow<DeleteCardState>(DeleteCardState.Idle)
    val deleteCardState: StateFlow<DeleteCardState> = _deleteCardState.asStateFlow()

    private val _setDefaultCardState = MutableStateFlow<SetDefaultCardState>(SetDefaultCardState.Idle)
    val setDefaultCardState: StateFlow<SetDefaultCardState> = _setDefaultCardState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadSavedCards() {
        viewModelScope.launch {
            _savedCardsState.value = SavedCardsState.Loading
            _isLoading.value = true
            val result = getSavedCardsUseCase()
            result.onSuccess { cards ->
                _savedCardsState.value = SavedCardsState.Success(cards)
            }.onFailure { error ->
                _savedCardsState.value = SavedCardsState.Error(error.message ?: "Failed to load cards")
                SnackbarHelper.showError(error.message ?: "Failed to load cards")
            }
            _isLoading.value = false
        }
    }

    fun deleteCard(paymentMethodId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _deleteCardState.value = DeleteCardState.Loading
            _isLoading.value = true
            val result = deleteCardUseCase(paymentMethodId)
            result.onSuccess {
                _deleteCardState.value = DeleteCardState.Success(paymentMethodId)
                SnackbarHelper.showSuccess("Card deleted")
                loadSavedCards()
                onSuccess()
            }.onFailure { error ->
                _deleteCardState.value = DeleteCardState.Error(error.message ?: "Failed to delete card")
                SnackbarHelper.showError(error.message ?: "Failed to delete card")
            }
            _isLoading.value = false
        }
    }

    fun setDefaultCard(paymentMethodId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _setDefaultCardState.value = SetDefaultCardState.Loading
            _isLoading.value = true
            val result = setDefaultCardUseCase(paymentMethodId)
            result.onSuccess {
                _setDefaultCardState.value = SetDefaultCardState.Success(paymentMethodId)
                SnackbarHelper.showSuccess("Default card updated")
                loadSavedCards()
                onSuccess()
            }.onFailure { error ->
                _setDefaultCardState.value = SetDefaultCardState.Error(error.message ?: "Failed to set default card")
                SnackbarHelper.showError(error.message ?: "Failed to set default card")
            }
            _isLoading.value = false
        }
    }

    fun clearDeleteState() {
        if (_deleteCardState.value !is DeleteCardState.Loading) {
            _deleteCardState.value = DeleteCardState.Idle
        }
    }

    fun clearSetDefaultState() {
        if (_setDefaultCardState.value !is SetDefaultCardState.Loading) {
            _setDefaultCardState.value = SetDefaultCardState.Idle
        }
    }
}