package com.wheezy.skyflight.feature.review.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheezy.skyflight.core.common.state.CanReviewState
import com.wheezy.skyflight.core.ui.snackbar.AppSnackbar
import com.wheezy.skyflight.core.ui.snackbar.SnackbarManager
import com.wheezy.skyflight.core.ui.snackbar.SnackbarType
import com.wheezy.skyflight.core.ui.snackbar.SnackbarPriority
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
    private val getFlightReviewsPaginatedUseCase: GetFlightReviewsPaginatedUseCase,
    private val getAirlineReviewsPaginatedUseCase: GetAirlineReviewsPaginatedUseCase
) : ViewModel() {

    // Стейты для отзывов на рейс (без пагинации) - используется в FlightReviewsScreen
    private val _flightReviewsState = MutableStateFlow<ReviewsUiState>(ReviewsUiState.Loading)
    val flightReviewsState: StateFlow<ReviewsUiState> = _flightReviewsState.asStateFlow()

    // Стейт для пагинированных отзывов на рейс - используется в FlightReviewsScreen
    private val _reviewsPageState = MutableStateFlow<ReviewsPageState>(ReviewsPageState.Loading)
    val reviewsPageState: StateFlow<ReviewsPageState> = _reviewsPageState.asStateFlow()

    // Стейт для пагинированных отзывов на авиакомпанию - используется в AirlineReviewsScreen
    private val _airlineReviewsPageState = MutableStateFlow<ReviewsPageState>(ReviewsPageState.Loading)
    val airlineReviewsPageState: StateFlow<ReviewsPageState> = _airlineReviewsPageState.asStateFlow()

    // Стейт для моих отзывов - используется в MyReviewsScreen
    private val _myReviewsState = MutableStateFlow<ReviewsUiState>(ReviewsUiState.Loading)
    val myReviewsState: StateFlow<ReviewsUiState> = _myReviewsState.asStateFlow()

    // Стейт для рейтинга авиакомпании - используется в FlightReviewsScreen и AirlineReviewsScreen
    private val _airlineRatingState = MutableStateFlow<AirlineRatingUiState>(AirlineRatingUiState.Loading)
    val airlineRatingState: StateFlow<AirlineRatingUiState> = _airlineRatingState.asStateFlow()

    // Стейты для CRUD операций
    private val _createReviewState = MutableStateFlow<CreateReviewUiState>(CreateReviewUiState.Idle)
    val createReviewState: StateFlow<CreateReviewUiState> = _createReviewState.asStateFlow()

    private val _deleteReviewState = MutableStateFlow<DeleteReviewUiState>(DeleteReviewUiState.Idle)
    val deleteReviewState: StateFlow<DeleteReviewUiState> = _deleteReviewState.asStateFlow()

    private val _updateReviewState = MutableStateFlow<UpdateReviewUiState>(UpdateReviewUiState.Idle)
    val updateReviewState: StateFlow<UpdateReviewUiState> = _updateReviewState.asStateFlow()

    // Стейт для проверки возможности оставить отзыв
    private val _canReviewState = MutableStateFlow<CanReviewState>(CanReviewState.Idle)
    val canReviewState: StateFlow<CanReviewState> = _canReviewState.asStateFlow()

    // ============= МЕТОДЫ ДЛЯ РЕЙТИНГА АВИАКОМПАНИИ =============
    // Используется в FlightReviewsScreen и AirlineReviewsScreen

    fun loadAirlineRating(airlineName: String) {
        viewModelScope.launch {
            _airlineRatingState.value = AirlineRatingUiState.Loading
            getAirlineRatingUseCase(airlineName)
                .onSuccess { _airlineRatingState.value = AirlineRatingUiState.Success(it) }
                .onFailure {
                    _airlineRatingState.value = AirlineRatingUiState.Error(it.message ?: "Failed to load rating")
                    showError(it.message ?: "Failed to load rating")
                }
        }
    }

    // ============= МЕТОДЫ ДЛЯ ПАГИНИРОВАННЫХ ОТЗЫВОВ НА АВИАКОМПАНИЮ =============
    // Используется в AirlineReviewsScreen

    fun loadAirlineReviewsPaginated(airlineName: String, page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            if (page == 0) {
                _airlineReviewsPageState.value = ReviewsPageState.Loading
            }
            getAirlineReviewsPaginatedUseCase(airlineName, page, size)
                .onSuccess { pageData ->
                    val current = _airlineReviewsPageState.value
                    val existing = if (current is ReviewsPageState.Success && page > 0) current.reviews else emptyList()
                    _airlineReviewsPageState.value = ReviewsPageState.Success(
                        reviews = existing + pageData.reviews,
                        currentPage = pageData.currentPage,
                        totalPages = pageData.totalPages,
                        totalItems = pageData.totalItems,
                        airlineRating = pageData.airlineRating
                    )
                }
                .onFailure {
                    _airlineReviewsPageState.value = ReviewsPageState.Error(it.message ?: "Failed to load reviews")
                    showError(it.message ?: "Failed to load reviews")
                }
        }
    }

    fun resetAirlineReviewsPage() {
        _airlineReviewsPageState.value = ReviewsPageState.Loading
    }

    // ============= МЕТОДЫ ДЛЯ ПРОВЕРКИ ВОЗМОЖНОСТИ ОТЗЫВА =============
    // Используется в CreateReviewScreen

    fun checkCanReview(bookingId: Long) {
        viewModelScope.launch {
            _canReviewState.value = CanReviewState.Loading
            canReviewUseCase(bookingId)
                .onSuccess { _canReviewState.value = CanReviewState.Success(it, bookingId) }
                .onFailure {
                    _canReviewState.value = CanReviewState.Error(it.message ?: "Failed to check")
                    showError(it.message ?: "Failed to check")
                }
        }
    }

    // ============= CRUD МЕТОДЫ ДЛЯ ОТЗЫВОВ =============
    // Используются в CreateReviewScreen и MyReviewsScreen

    fun createReview(bookingId: Long, rating: Int, comment: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _createReviewState.value = CreateReviewUiState.Loading
            createReviewUseCase(bookingId, rating, comment)
                .onSuccess {
                    _createReviewState.value = CreateReviewUiState.Success(it)
                    SnackbarManager.show(
                        AppSnackbar(
                            message = "Thank you for your review!",
                            type = SnackbarType.SUCCESS,
                            priority = SnackbarPriority.NORMAL
                        )
                    )
                    onSuccess()
                }
                .onFailure {
                    _createReviewState.value = CreateReviewUiState.Error(it.message ?: "Failed to submit review")
                    showError(it.message ?: "Failed to submit review")
                }
        }
    }

    fun clearCreateReviewState() {
        if (_createReviewState.value !is CreateReviewUiState.Loading) {
            _createReviewState.value = CreateReviewUiState.Idle
        }
    }

    fun updateReview(reviewId: Long, rating: Int, comment: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _updateReviewState.value = UpdateReviewUiState.Loading
            updateReviewUseCase(reviewId, rating, comment)
                .onSuccess {
                    _updateReviewState.value = UpdateReviewUiState.Success(it)
                    SnackbarManager.show(
                        AppSnackbar(
                            message = "Review updated!",
                            type = SnackbarType.SUCCESS,
                            priority = SnackbarPriority.NORMAL
                        )
                    )
                    onSuccess()
                }
                .onFailure {
                    _updateReviewState.value = UpdateReviewUiState.Error(it.message ?: "Failed to update review")
                    showError(it.message ?: "Failed to update review")
                }
        }
    }

    fun clearUpdateReviewState() {
        if (_updateReviewState.value !is UpdateReviewUiState.Loading) {
            _updateReviewState.value = UpdateReviewUiState.Idle
        }
    }

    fun deleteReview(reviewId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _deleteReviewState.value = DeleteReviewUiState.Loading
            deleteReviewUseCase(reviewId)
                .onSuccess {
                    _deleteReviewState.value = DeleteReviewUiState.Success(reviewId)
                    SnackbarManager.show(
                        AppSnackbar(
                            message = "Review deleted",
                            type = SnackbarType.SUCCESS,
                            priority = SnackbarPriority.NORMAL
                        )
                    )
                    onSuccess()
                }
                .onFailure {
                    _deleteReviewState.value = DeleteReviewUiState.Error(it.message ?: "Failed to delete review")
                    showError(it.message ?: "Failed to delete review")
                }
        }
    }

    fun clearDeleteReviewState() {
        if (_deleteReviewState.value !is DeleteReviewUiState.Loading) {
            _deleteReviewState.value = DeleteReviewUiState.Idle
        }
    }

    // ============= МЕТОДЫ ДЛЯ ЗАГРУЗКИ ОТЗЫВОВ =============
    // Используются в FlightReviewsScreen

    fun loadFlightReviews(flightId: Long) {
        viewModelScope.launch {
            _flightReviewsState.value = ReviewsUiState.Loading
            getFlightReviewsUseCase(flightId)
                .onSuccess { _flightReviewsState.value = ReviewsUiState.Success(it) }
                .onFailure {
                    _flightReviewsState.value = ReviewsUiState.Error(it.message ?: "Failed to load reviews")
                    showError(it.message ?: "Failed to load reviews")
                }
        }
    }

    fun loadFlightReviewsPaginated(flightId: Long, page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            if (page == 0) {
                _reviewsPageState.value = ReviewsPageState.Loading
            }
            getFlightReviewsPaginatedUseCase(flightId, page, size)
                .onSuccess { pageData ->
                    val current = _reviewsPageState.value
                    val existing = if (current is ReviewsPageState.Success && page > 0) current.reviews else emptyList()
                    _reviewsPageState.value = ReviewsPageState.Success(
                        reviews = existing + pageData.reviews,
                        currentPage = pageData.currentPage,
                        totalPages = pageData.totalPages,
                        totalItems = pageData.totalItems,
                        airlineRating = pageData.airlineRating
                    )
                }
                .onFailure {
                    _reviewsPageState.value = ReviewsPageState.Error(it.message ?: "Failed to load reviews")
                    showError(it.message ?: "Failed to load reviews")
                }
        }
    }

    fun resetReviewsPage() {
        _reviewsPageState.value = ReviewsPageState.Loading
    }

    fun loadMyReviews() {
        viewModelScope.launch {
            _myReviewsState.value = ReviewsUiState.Loading
            getMyReviewsUseCase()
                .onSuccess { _myReviewsState.value = ReviewsUiState.Success(it) }
                .onFailure {
                    _myReviewsState.value = ReviewsUiState.Error(it.message ?: "Failed to load reviews")
                    showError(it.message ?: "Failed to load reviews")
                }
        }
    }

    // ============= ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =============

    private fun showError(message: String) {
        viewModelScope.launch {
            SnackbarManager.show(
                AppSnackbar(
                    message = message,
                    type = SnackbarType.ERROR,
                    priority = SnackbarPriority.NORMAL
                )
            )
        }
    }
}