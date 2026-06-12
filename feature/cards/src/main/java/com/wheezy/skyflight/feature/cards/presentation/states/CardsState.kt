package com.wheezy.skyflight.feature.cards.presentation.states

import com.wheezy.skyflight.core.model.SavedCard

sealed class SavedCardsState {
    object Loading : SavedCardsState()
    data class Success(val cards: List<SavedCard>) : SavedCardsState()
    data class Error(val message: String) : SavedCardsState()
}

sealed class DeleteCardState {
    object Idle : DeleteCardState()
    object Loading : DeleteCardState()
    data class Success(val paymentMethodId: String) : DeleteCardState()
    data class Error(val message: String) : DeleteCardState()
}

sealed class SetDefaultCardState {
    object Idle : SetDefaultCardState()
    object Loading : SetDefaultCardState()
    data class Success(val paymentMethodId: String) : SetDefaultCardState()
    data class Error(val message: String) : SetDefaultCardState()
}