package com.wheezy.skyflight.feature.loyalty.presentation.states

import com.wheezy.skyflight.core.model.PointsBalance
import com.wheezy.skyflight.core.model.PointsTransaction
import com.wheezy.skyflight.core.model.RedeemPointsResponse
import com.wheezy.skyflight.core.model.TierBenefit

sealed class PointsBalanceState {
    object Loading : PointsBalanceState()
    data class Success(val data: PointsBalance) : PointsBalanceState()
    data class Error(val message: String) : PointsBalanceState()
}

sealed class TransactionsState {
    object Loading : TransactionsState()
    data class Success(val transactions: List<PointsTransaction>) : TransactionsState()
    data class Error(val message: String) : TransactionsState()
}

sealed class TiersState {
    object Loading : TiersState()
    data class Success(val tiers: List<TierBenefit>) : TiersState()
    data class Error(val message: String) : TiersState()
}

sealed class RedeemPointsState {
    object Idle : RedeemPointsState()
    object Loading : RedeemPointsState()
    data class Success(val response: RedeemPointsResponse) : RedeemPointsState()
    data class Error(val message: String) : RedeemPointsState()
}