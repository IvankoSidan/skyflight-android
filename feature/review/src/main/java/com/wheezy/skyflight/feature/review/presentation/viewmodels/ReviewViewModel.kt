package com.wheezy.skyflight.feature.review.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheezy.skyflight.core.model.AirlineRating
import com.wheezy.skyflight.core.model.Review
import com.wheezy.skyflight.core.ui.snackbar.SnackbarHelper
import com.wheezy.skyflight.feature.review.domain.usecase.*
import com.wheezy.skyflight.feature.review.presentation.states.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val createReviewUseCase: CreateReviewUseCase,
    private val updateReviewUseCase: UpdateReviewUseCase,
    private val deleteReviewUseCase: DeleteReviewUseCase,
    private val getFlightReviewsUseCase: GetFlightReviewsUseCase,
    private val getAirlineRatingUseCase: GetAirlineRatingUseCase,
    private val getMyReviewsUseCase: GetMyReviewsUseCase,
    private val canReviewUseCase: CanReviewUseCase,
    private val getFlightReviewsPaginatedUseCase: GetFlightReviewsPaginatedUseCase
) : ViewModel() {

    private val _flightReviewsState = MutableStateFlow<ReviewsUiState>(ReviewsUiState.Loading)
    val flightReviewsState: StateFlow<ReviewsUiState> = _flightReviewsState.asStateFlow()

    private val _reviewsPageState = MutableStateFlow<ReviewsPageState>(ReviewsPageState.Loading)
    val reviewsPageState: StateFlow<ReviewsPageState> = _reviewsPageState.asStateFlow()

    private val _myReviewsState = MutableStateFlow<ReviewsUiState>(ReviewsUiState.Loading)
    val myReviewsState: StateFlow<ReviewsUiState> = _myReviewsState.asStateFlow()

    private val _airlineRatingState = MutableStateFlow<AirlineRatingUiState>(AirlineRatingUiState.Loading)
    val airlineRatingState: StateFlow<AirlineRatingUiState> = _airlineRatingState.asStateFlow()

    private val _createReviewState = MutableStateFlow<CreateReviewUiState>(CreateReviewUiState.Idle)
    val createReviewState: StateFlow<CreateReviewUiState> = _createReviewState.asStateFlow()

    private val _deleteReviewState = MutableStateFlow<DeleteReviewUiState>(DeleteReviewUiState.Idle)
    val deleteReviewState: StateFlow<DeleteReviewUiState> = _deleteReviewState.asStateFlow()

    private val _updateReviewState = MutableStateFlow<UpdateReviewUiState>(UpdateReviewUiState.Idle)
    val updateReviewState: StateFlow<UpdateReviewUiState> = _updateReviewState.asStateFlow()

    private val _canReviewState = MutableStateFlow<CanReviewUiState>(CanReviewUiState.Loading)
    val canReviewState: StateFlow<CanReviewUiState> = _canReviewState.asStateFlow()

    fun loadAirlineRating(airlineName: String) {
        viewModelScope.launch {
            _airlineRatingState.value = AirlineRatingUiState.Loading
            val result = getAirlineRatingUseCase(airlineName)
            result.onSuccess { rating ->
                _airlineRatingState.value = AirlineRatingUiState.Success(rating)
            }.onFailure { error ->
                _airlineRatingState.value = AirlineRatingUiState.Error(error.message ?: "Failed to load rating")
            }
        }
    }

    fun checkCanReview(bookingId: Long) {
        viewModelScope.launch {
            _canReviewState.value = CanReviewUiState.Loading
            val result = canReviewUseCase(bookingId)
            result.onSuccess { canReview ->
                _canReviewState.value = CanReviewUiState.Success(canReview)
            }.onFailure { error ->
                _canReviewState.value = CanReviewUiState.Error(error.message ?: "Failed to check review eligibility")
            }
        }
    }

    fun createReview(bookingId: Long, rating: Int, comment: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _createReviewState.value = CreateReviewUiState.Loading
            val result = createReviewUseCase(bookingId, rating, comment)
            result.onSuccess { review ->
                _createReviewState.value = CreateReviewUiState.Success(review)
                SnackbarHelper.showSuccess("Thank you for your review! You've earned bonus points!")
                onSuccess()
            }.onFailure { error ->
                _createReviewState.value = CreateReviewUiState.Error(error.message ?: "Failed to submit review")
                SnackbarHelper.showError(error.message ?: "Failed to submit review")
            }
        }
    }

    fun clearCreateReviewState() {
        if (_createReviewState.value !is CreateReviewUiState.Loading) {
            _createReviewState.value = CreateReviewUiState.Idle
        }
    }

    fun loadMyReviews() {
        viewModelScope.launch {
            _myReviewsState.value = ReviewsUiState.Loading
            val result = getMyReviewsUseCase()
            result.onSuccess { reviews ->
                _myReviewsState.value = ReviewsUiState.Success(reviews)
            }.onFailure { error ->
                _myReviewsState.value = ReviewsUiState.Error(error.message ?: "Failed to load your reviews")
            }
        }
    }

    fun updateReview(reviewId: Long, rating: Int, comment: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _updateReviewState.value = UpdateReviewUiState.Loading
            val result = updateReviewUseCase(reviewId, rating, comment)
            result.onSuccess { review ->
                _updateReviewState.value = UpdateReviewUiState.Success(review)
                SnackbarHelper.showSuccess("Review updated!")
                onSuccess()
            }.onFailure { error ->
                _updateReviewState.value = UpdateReviewUiState.Error(error.message ?: "Failed to update review")
                SnackbarHelper.showError(error.message ?: "Failed to update review")
            }
        }
    }

    fun deleteReview(reviewId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _deleteReviewState.value = DeleteReviewUiState.Loading
            val result = deleteReviewUseCase(reviewId)
            result.onSuccess {
                _deleteReviewState.value = DeleteReviewUiState.Success(reviewId)
                SnackbarHelper.showSuccess("Review deleted")
                onSuccess()
            }.onFailure { error ->
                _deleteReviewState.value = DeleteReviewUiState.Error(error.message ?: "Failed to delete review")
                SnackbarHelper.showError(error.message ?: "Failed to delete review")
            }
        }
    }

    fun clearDeleteReviewState() {
        if (_deleteReviewState.value !is DeleteReviewUiState.Loading) {
            _deleteReviewState.value = DeleteReviewUiState.Idle
        }
    }

    fun clearUpdateReviewState() {
        if (_updateReviewState.value !is UpdateReviewUiState.Loading) {
            _updateReviewState.value = UpdateReviewUiState.Idle
        }
    }

    fun loadFlightReviews(flightId: Long) {
        viewModelScope.launch {
            _flightReviewsState.value = ReviewsUiState.Loading
            val result = getFlightReviewsUseCase(flightId)
            result.onSuccess { reviews ->
                _flightReviewsState.value = ReviewsUiState.Success(reviews)
            }.onFailure { error ->
                _flightReviewsState.value = ReviewsUiState.Error(error.message ?: "Failed to load reviews")
            }
        }
    }

    fun loadFlightReviewsPaginated(flightId: Long, page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            if (page == 0) {
                _reviewsPageState.value = ReviewsPageState.Loading
            }

            val result = getFlightReviewsPaginatedUseCase(flightId, page, size)
            result.onSuccess { pageData ->
                val currentState = _reviewsPageState.value
                val existingReviews = if (currentState is ReviewsPageState.Success && page > 0) {
                    currentState.reviews
                } else {
                    emptyList()
                }

                _reviewsPageState.value = ReviewsPageState.Success(
                    reviews = existingReviews + pageData.reviews,
                    currentPage = pageData.currentPage,
                    totalPages = pageData.totalPages,
                    totalItems = pageData.totalItems,
                    airlineRating = pageData.airlineRating
                )
            }.onFailure { error ->
                _reviewsPageState.value = ReviewsPageState.Error(error.message ?: "Failed to load reviews")
            }
        }
    }

    fun resetReviewsPage() {
        _reviewsPageState.value = ReviewsPageState.Loading
    }
}