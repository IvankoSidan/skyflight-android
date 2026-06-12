package com.wheezy.skyflight.feature.review.presentation.states

import com.wheezy.skyflight.core.model.AirlineRating
import com.wheezy.skyflight.core.model.Review

sealed class ReviewsUiState {
    object Loading : ReviewsUiState()
    data class Success(val reviews: List<Review>) : ReviewsUiState()
    data class Error(val message: String) : ReviewsUiState()
}

sealed class AirlineRatingUiState {
    object Loading : AirlineRatingUiState()
    data class Success(val rating: AirlineRating) : AirlineRatingUiState()
    data class Error(val message: String) : AirlineRatingUiState()
}

sealed class CreateReviewUiState {
    object Idle : CreateReviewUiState()
    object Loading : CreateReviewUiState()
    data class Success(val review: Review) : CreateReviewUiState()
    data class Error(val message: String) : CreateReviewUiState()
}

sealed class DeleteReviewUiState {
    object Idle : DeleteReviewUiState()
    object Loading : DeleteReviewUiState()
    data class Success(val reviewId: Long) : DeleteReviewUiState()
    data class Error(val message: String) : DeleteReviewUiState()
}

sealed class UpdateReviewUiState {
    object Idle : UpdateReviewUiState()
    object Loading : UpdateReviewUiState()
    data class Success(val review: Review) : UpdateReviewUiState()
    data class Error(val message: String) : UpdateReviewUiState()
}

sealed class CanReviewUiState {
    object Loading : CanReviewUiState()
    data class Success(val canReview: Boolean) : CanReviewUiState()
    data class Error(val message: String) : CanReviewUiState()
}