package com.wheezy.skyflight.feature.loyalty.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheezy.skyflight.core.model.CalculateDiscountResponse
import com.wheezy.skyflight.core.model.RedeemPointsResponse
import com.wheezy.skyflight.core.ui.snackbar.SnackbarHelper
import com.wheezy.skyflight.feature.loyalty.domain.usecase.*
import com.wheezy.skyflight.feature.loyalty.presentation.states.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoyaltyViewModel @Inject constructor(
    private val getPointsBalanceUseCase: GetPointsBalanceUseCase,
    private val getPointsTransactionsUseCase: GetPointsTransactionsUseCase,
    private val getTiersUseCase: GetTiersUseCase,
    private val calculateDiscountUseCase: CalculateDiscountUseCase,
    private val redeemPointsUseCase: RedeemPointsUseCase
) : ViewModel() {

    private val _pointsBalanceState = MutableStateFlow<PointsBalanceState>(PointsBalanceState.Loading)
    val pointsBalanceState: StateFlow<PointsBalanceState> = _pointsBalanceState.asStateFlow()

    private val _transactionsState = MutableStateFlow<TransactionsState>(TransactionsState.Loading)
    val transactionsState: StateFlow<TransactionsState> = _transactionsState.asStateFlow()

    private val _tiersState = MutableStateFlow<TiersState>(TiersState.Loading)
    val tiersState: StateFlow<TiersState> = _tiersState.asStateFlow()

    private val _redeemPointsState = MutableStateFlow<RedeemPointsState>(RedeemPointsState.Idle)
    val redeemPointsState: StateFlow<RedeemPointsState> = _redeemPointsState.asStateFlow()

    private val _calculatedDiscount = MutableStateFlow<CalculateDiscountResponse?>(null)
    val calculatedDiscount: StateFlow<CalculateDiscountResponse?> = _calculatedDiscount.asStateFlow()

    fun loadPointsBalance() {
        viewModelScope.launch {
            _pointsBalanceState.value = PointsBalanceState.Loading
            val result = getPointsBalanceUseCase()
            result.onSuccess { balance ->
                _pointsBalanceState.value = PointsBalanceState.Success(balance)
            }.onFailure { error ->
                _pointsBalanceState.value = PointsBalanceState.Error(error.message ?: "Failed to load balance")
            }
        }
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _transactionsState.value = TransactionsState.Loading
            val result = getPointsTransactionsUseCase()
            result.onSuccess { transactions ->
                _transactionsState.value = TransactionsState.Success(transactions)
            }.onFailure { error ->
                _transactionsState.value = TransactionsState.Error(error.message ?: "Failed to load transactions")
            }
        }
    }

    fun loadTiers() {
        viewModelScope.launch {
            _tiersState.value = TiersState.Loading
            val result = getTiersUseCase()
            result.onSuccess { tiers ->
                _tiersState.value = TiersState.Success(tiers)
            }.onFailure { error ->
                _tiersState.value = TiersState.Error(error.message ?: "Failed to load tiers")
            }
        }
    }

    fun calculateDiscount(amount: Long, points: Int) {
        viewModelScope.launch {
            val result = calculateDiscountUseCase(amount, points)
            result.onSuccess { discount ->
                _calculatedDiscount.value = discount
            }.onFailure { error ->
                _calculatedDiscount.value = null
                SnackbarHelper.showError(error.message ?: "Failed to calculate discount")
            }
        }
    }

    fun clearCalculatedDiscount() {
        _calculatedDiscount.value = null
    }

    fun redeemPoints(points: Int, bookingId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _redeemPointsState.value = RedeemPointsState.Loading
            val result = redeemPointsUseCase(points, bookingId)
            result.onSuccess { response ->
                if (response.success) {
                    _redeemPointsState.value = RedeemPointsState.Success(response)
                    SnackbarHelper.showSuccess(response.message)
                    loadPointsBalance()
                    onSuccess()
                } else {
                    _redeemPointsState.value = RedeemPointsState.Error(response.message)
                    SnackbarHelper.showError(response.message)
                }
            }.onFailure { error ->
                _redeemPointsState.value = RedeemPointsState.Error(error.message ?: "Failed to redeem points")
                SnackbarHelper.showError(error.message ?: "Failed to redeem points")
            }
        }
    }

    fun clearRedeemState() {
        _redeemPointsState.value = RedeemPointsState.Idle
    }

    fun resetAllStates() {
        _pointsBalanceState.value = PointsBalanceState.Loading
        _transactionsState.value = TransactionsState.Loading
        _tiersState.value = TiersState.Loading
        _redeemPointsState.value = RedeemPointsState.Idle
        _calculatedDiscount.value = null
    }
}