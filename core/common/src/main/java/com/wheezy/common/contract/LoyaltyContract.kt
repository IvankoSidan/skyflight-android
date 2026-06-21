package com.wheezy.skyflight.core.common.contract

import com.wheezy.common.state.PointsBalanceState
import com.wheezy.skyflight.core.model.CalculateDiscountResponse
import kotlinx.coroutines.flow.StateFlow

interface LoyaltyContract {

    val pointsBalanceState: StateFlow<PointsBalanceState>
    val calculatedDiscount: StateFlow<CalculateDiscountResponse?>
    val isLoading: StateFlow<Boolean>
    fun loadPointsBalance()
    fun calculateDiscount(
        amount: Long,
        points: Int
    )
    fun clearCalculatedDiscount()
    fun redeemPoints(
        points: Int,
        bookingId: Long,
        onSuccess: () -> Unit
    )
    fun clearRedeemState()
    fun resetAllStates()
}