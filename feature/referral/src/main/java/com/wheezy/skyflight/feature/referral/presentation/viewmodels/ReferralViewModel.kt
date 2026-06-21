package com.wheezy.skyflight.feature.referral.presentation.viewmodels

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheezy.skyflight.core.ui.snackbar.SnackbarHelper
import com.wheezy.skyflight.feature.referral.domain.usecase.ApplyReferralCodeUseCase
import com.wheezy.skyflight.feature.referral.domain.usecase.GetMyReferralsUseCase
import com.wheezy.skyflight.feature.referral.domain.usecase.GetReferralCodeUseCase
import com.wheezy.skyflight.feature.referral.presentation.states.ApplyReferralState
import com.wheezy.skyflight.feature.referral.presentation.states.ReferralCodeState
import com.wheezy.skyflight.feature.referral.presentation.states.ReferralInfoState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReferralViewModel @Inject constructor(
    private val getReferralCodeUseCase: GetReferralCodeUseCase,
    private val applyReferralCodeUseCase: ApplyReferralCodeUseCase,
    private val getMyReferralsUseCase: GetMyReferralsUseCase
) : ViewModel() {

    private val _referralCodeState = MutableStateFlow<ReferralCodeState>(ReferralCodeState.Loading)
    val referralCodeState: StateFlow<ReferralCodeState> = _referralCodeState.asStateFlow()

    private val _referralInfoState = MutableStateFlow<ReferralInfoState>(ReferralInfoState.Loading)
    val referralInfoState: StateFlow<ReferralInfoState> = _referralInfoState.asStateFlow()

    private val _applyReferralState = MutableStateFlow<ApplyReferralState>(ApplyReferralState.Idle)
    val applyReferralState: StateFlow<ApplyReferralState> = _applyReferralState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        loadReferralCode()
        loadReferrals()
    }

    fun loadReferralCode() {
        viewModelScope.launch {
            _referralCodeState.value = ReferralCodeState.Loading
            val result = getReferralCodeUseCase()
            result.onSuccess { code ->
                _referralCodeState.value = ReferralCodeState.Success(code)
            }.onFailure { error ->
                _referralCodeState.value = ReferralCodeState.Error(error.message ?: "Failed to load referral code")
            }
        }
    }

    fun loadReferrals() {
        viewModelScope.launch {
            _referralInfoState.value = ReferralInfoState.Loading
            val result = getMyReferralsUseCase()
            result.onSuccess { info ->
                _referralInfoState.value = ReferralInfoState.Success(info)
            }.onFailure { error ->
                _referralInfoState.value = ReferralInfoState.Error(error.message ?: "Failed to load referrals")
            }
        }
    }

    fun applyReferralCode(code: String) {
        viewModelScope.launch {
            _applyReferralState.value = ApplyReferralState.Loading
            val result = applyReferralCodeUseCase(code)
            result.onSuccess { response ->
                if (response.success) {
                    _applyReferralState.value = ApplyReferralState.Success(response.message, response.discountPercent)
                    SnackbarHelper.showSuccess(response.message)
                    loadReferrals()
                } else {
                    _applyReferralState.value = ApplyReferralState.Error(response.message)
                    SnackbarHelper.showError(response.message)
                }
            }.onFailure { error ->
                _applyReferralState.value = ApplyReferralState.Error(error.message ?: "Failed to apply code")
                SnackbarHelper.showError(error.message ?: "Failed to apply code")
            }
        }
    }

    fun shareCode(context: Context) {
        val state = _referralCodeState.value
        if (state is ReferralCodeState.Success) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, """
                    Join me on SkyFlight! ✈️
                    
                    Use my referral code: ${state.data.code}
                    
                    Download the app: https://skyflightbooking.ru/download
                    
                    You'll get 10% off your first booking!
                """.trimIndent())
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(shareIntent, "Invite Friends via"))
        }
    }
}