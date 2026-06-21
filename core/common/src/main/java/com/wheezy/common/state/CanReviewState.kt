package com.wheezy.skyflight.core.common.state

sealed class CanReviewState {
    object Idle : CanReviewState()
    object Loading : CanReviewState()
    data class Success(val canReview: Boolean, val bookingId: Long) : CanReviewState()
    data class Error(val message: String) : CanReviewState()
}