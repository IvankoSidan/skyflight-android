package com.wheezy.skyflight.feature.review.presentation.states

import com.wheezy.skyflight.core.model.AirlineRating
import com.wheezy.skyflight.core.model.Review

sealed class ReviewsPageState {
    object Loading : ReviewsPageState()
    data class Success(
        val reviews: List<Review>,
        val currentPage: Int,
        val totalPages: Int,
        val totalItems: Int,
        val airlineRating: AirlineRating? = null
    ) : ReviewsPageState()
    data class Error(val message: String) : ReviewsPageState()
}