package com.wheezy.common.state

import com.wheezy.skyflight.core.model.PointsBalance

sealed class PointsBalanceState {
    object Loading : PointsBalanceState()
    data class Success(val data: PointsBalance) : PointsBalanceState()
    data class Error(val message: String) : PointsBalanceState()
}