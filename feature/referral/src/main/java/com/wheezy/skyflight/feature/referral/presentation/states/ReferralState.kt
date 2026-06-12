package com.wheezy.skyflight.feature.referral.presentation.states

import com.wheezy.skyflight.feature.referral.domain.model.ReferralCode
import com.wheezy.skyflight.feature.referral.domain.model.ReferralInfo

sealed class ReferralCodeState {
    object Loading : ReferralCodeState()
    data class Success(val data: ReferralCode) : ReferralCodeState()
    data class Error(val message: String) : ReferralCodeState()
}

sealed class ReferralInfoState {
    object Loading : ReferralInfoState()
    data class Success(val data: ReferralInfo) : ReferralInfoState()
    data class Error(val message: String) : ReferralInfoState()
}

sealed class ApplyReferralState {
    object Idle : ApplyReferralState()
    object Loading : ApplyReferralState()
    data class Success(val message: String, val discountPercent: Int?) : ApplyReferralState()
    data class Error(val message: String) : ApplyReferralState()
}