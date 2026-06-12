package com.wheezy.skyflight.feature.booking.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheezy.skyflight.core.common.manager.NetworkMonitor
import com.wheezy.skyflight.core.database.dao.OfflineBookingDao
import com.wheezy.skyflight.core.database.entity.OfflineBookingEntity
import com.wheezy.skyflight.core.model.BookingStatus
import com.wheezy.skyflight.core.model.FlightModel
import com.wheezy.skyflight.core.model.Seat
import com.wheezy.skyflight.core.network.model.BookingDetailsDTO
import com.wheezy.skyflight.core.ui.snackbar.SnackbarHelper
import com.wheezy.skyflight.feature.booking.domain.usecase.*
import com.wheezy.skyflight.feature.booking.presentation.states.BookingListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val getMyBookingsUseCase: GetMyBookingsUseCase,
    private val getBookingByIdUseCase: GetBookingByIdUseCase,
    private val createBookingUseCase: CreateBookingUseCase,
    private val cancelOrDeleteBookingUseCase: CancelOrDeleteBookingUseCase,
    private val updateBookingStatusUseCase: UpdateBookingStatusUseCase,
    private val getFlightByIdUseCase: GetFlightByIdUseCase,
    private val checkCanReviewUseCase: CheckCanReviewUseCase,
    private val offlineBookingDao: OfflineBookingDao,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _bookingListState = MutableStateFlow<BookingListState>(BookingListState.Loading)
    val bookingListState: StateFlow<BookingListState> = _bookingListState.asStateFlow()

    private val _selectedFlight = MutableStateFlow<FlightModel?>(null)
    val selectedFlight: StateFlow<FlightModel?> = _selectedFlight.asStateFlow()

    private val _selectedSeats = MutableStateFlow<List<Seat>>(emptyList())
    val selectedSeats: StateFlow<List<Seat>> = _selectedSeats.asStateFlow()

    private val _bookingId = MutableStateFlow<Long?>(null)
    val bookingId: StateFlow<Long?> = _bookingId.asStateFlow()

    private val _loadingBookingIds = MutableStateFlow<Set<Long>>(emptySet())
    val loadingBookingIds: StateFlow<Set<Long>> = _loadingBookingIds.asStateFlow()

    private val _isCreatingBooking = MutableStateFlow(false)
    val isCreatingBooking: StateFlow<Boolean> = _isCreatingBooking.asStateFlow()

    private val _canReviewMap = MutableStateFlow<Map<Long, Boolean>>(emptyMap())
    val canReviewMap: StateFlow<Map<Long, Boolean>> = _canReviewMap.asStateFlow()

    suspend fun getBookingById(bookingId: Long): Result<BookingDetailsDTO> {
        return getBookingByIdUseCase(bookingId)
    }

    suspend fun getFlightById(flightId: Long): FlightModel? {
        return getFlightByIdUseCase(flightId)
    }

    fun checkCanReview(bookingId: Long) {
        viewModelScope.launch {
            val result = checkCanReviewUseCase(bookingId)
            _canReviewMap.value = _canReviewMap.value + (bookingId to result)
        }
    }

    fun loadMyBookings() {
        viewModelScope.launch {
            _bookingListState.value = BookingListState.Loading
            val result = getMyBookingsUseCase()
            result.onSuccess { bookings ->
                _bookingListState.value = BookingListState.Success(bookings)
                if (bookings.isEmpty()) {
                    SnackbarHelper.showInfo("No bookings found")
                }
            }.onFailure { error ->
                _bookingListState.value = BookingListState.Error(error.message ?: "Failed to load bookings")
                SnackbarHelper.showError(error.message ?: "Failed to load bookings")
            }
        }
    }

    fun createBooking(
        flight: FlightModel,
        selectedSeats: List<Seat>,
        onComplete: (Boolean, Long?) -> Unit = { _, _ -> }
    ) {
        if (_isCreatingBooking.value) {
            SnackbarHelper.showError("Booking already in progress")
            onComplete(false, null)
            return
        }

        viewModelScope.launch {
            _isCreatingBooking.value = true

            if (!networkMonitor.isConnected.value) {
                val offlineBooking = OfflineBookingEntity(
                    flightId = flight.flightId ?: 0,
                    seatNumbers = selectedSeats.joinToString(",") { it.name },
                    passengerName = null,
                    passengerEmail = null,
                    status = "PENDING_SYNC",
                    retryCount = 0
                )
                offlineBookingDao.insertBooking(offlineBooking)
                SnackbarHelper.showInfo("Booking saved offline. Will sync automatically when online.")
                _isCreatingBooking.value = false
                onComplete(false, null)
                return@launch
            }

            val result = createBookingUseCase(flight, selectedSeats)
            if (result.success) {
                SnackbarHelper.showSuccess("Booking created successfully!")
                _selectedFlight.value = flight
                _selectedSeats.value = selectedSeats
                _bookingId.value = result.bookingId
                onComplete(true, result.bookingId)
            } else {
                SnackbarHelper.showError(result.errorMessage ?: "Booking failed")
                onComplete(false, null)
            }
            _isCreatingBooking.value = false
        }
    }

    fun cancelOrDeleteBooking(bookingId: Long, status: BookingStatus) {
        viewModelScope.launch {
            _loadingBookingIds.value = _loadingBookingIds.value + bookingId

            val result = cancelOrDeleteBookingUseCase(bookingId, status)
            when (result) {
                is CancelOrDeleteBookingUseCase.Result.Success -> {
                    loadMyBookings()
                    SnackbarHelper.showSuccess("Operation completed successfully")
                }
                is CancelOrDeleteBookingUseCase.Result.Error -> {
                    SnackbarHelper.showError(result.message)
                }
            }

            _loadingBookingIds.value = _loadingBookingIds.value - bookingId
        }
    }

    fun updateBookingStatus(bookingId: Long, newStatus: BookingStatus) {
        viewModelScope.launch {
            _loadingBookingIds.value = _loadingBookingIds.value + bookingId

            val result = updateBookingStatusUseCase(bookingId, newStatus)
            result.onSuccess {
                loadMyBookings()
                SnackbarHelper.showSuccess("Booking status updated to ${newStatus.name}")
            }.onFailure { error ->
                SnackbarHelper.showError(error.message ?: "Failed to update status")
            }

            _loadingBookingIds.value = _loadingBookingIds.value - bookingId
        }
    }

    fun setFlight(flight: FlightModel) {
        _selectedFlight.value = flight
    }

    fun setSelectedSeats(seats: List<Seat>) {
        _selectedSeats.value = seats
    }

    fun setBookingId(id: Long?) {
        _bookingId.value = id
    }

    fun clearBooking() {
        _selectedFlight.value = null
        _selectedSeats.value = emptyList()
        _bookingId.value = null
        _isCreatingBooking.value = false
    }
}