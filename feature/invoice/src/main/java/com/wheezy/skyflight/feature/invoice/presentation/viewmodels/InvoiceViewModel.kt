package com.wheezy.skyflight.feature.invoice.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wheezy.skyflight.core.model.Invoice
import com.wheezy.skyflight.core.model.InvoiceListResponse
import com.wheezy.skyflight.core.ui.snackbar.SnackbarHelper
import com.wheezy.skyflight.feature.invoice.domain.usecase.*
import com.wheezy.skyflight.feature.invoice.presentation.states.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val getMyInvoicesUseCase: GetMyInvoicesUseCase,
    private val downloadInvoiceUseCase: DownloadInvoiceUseCase,
    private val resendInvoiceEmailUseCase: ResendInvoiceEmailUseCase,
    private val getInvoiceByBookingIdUseCase: GetInvoiceByBookingIdUseCase
) : ViewModel() {

    private val _invoicesState = MutableStateFlow<InvoicesState>(InvoicesState.Loading)
    val invoicesState: StateFlow<InvoicesState> = _invoicesState.asStateFlow()

    private val _invoiceDetailState = MutableStateFlow<InvoiceDetailState>(InvoiceDetailState.Loading)
    val invoiceDetailState: StateFlow<InvoiceDetailState> = _invoiceDetailState.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadInvoiceState>(DownloadInvoiceState.Idle)
    val downloadState: StateFlow<DownloadInvoiceState> = _downloadState.asStateFlow()

    private val _resendEmailState = MutableStateFlow<ResendEmailState>(ResendEmailState.Idle)
    val resendEmailState: StateFlow<ResendEmailState> = _resendEmailState.asStateFlow()

    fun loadInvoices(page: Int = 0, size: Int = 20) {
        viewModelScope.launch {
            if (page == 0) {
                _invoicesState.value = InvoicesState.Loading
            }

            val result = getMyInvoicesUseCase(page, size)
            result.onSuccess { response ->
                val currentState = _invoicesState.value
                val existingInvoices = if (currentState is InvoicesState.Success && page > 0) {
                    currentState.data.invoices
                } else {
                    emptyList()
                }

                _invoicesState.value = InvoicesState.Success(
                    InvoiceListResponse(
                        invoices = existingInvoices + response.invoices,
                        totalCount = response.totalCount,
                        totalPages = response.totalPages,
                        currentPage = page
                    )
                )
            }.onFailure { error ->
                if (page == 0) {
                    _invoicesState.value = InvoicesState.Error(error.message ?: "Failed to load invoices")
                } else {
                    SnackbarHelper.showError(error.message ?: "Failed to load more invoices")
                }
            }
        }
    }

    fun loadInvoiceByBookingId(bookingId: Long) {
        viewModelScope.launch {
            _invoiceDetailState.value = InvoiceDetailState.Loading
            val result = getInvoiceByBookingIdUseCase(bookingId)
            result.onSuccess { invoice ->
                _invoiceDetailState.value = InvoiceDetailState.Success(invoice)
            }.onFailure { error ->
                _invoiceDetailState.value = InvoiceDetailState.Error(error.message ?: "Failed to load invoice")
            }
        }
    }

    fun downloadInvoice(invoiceId: Long) {
        viewModelScope.launch {
            _downloadState.value = DownloadInvoiceState.Loading
            val result = downloadInvoiceUseCase(invoiceId)
            result.onSuccess { responseBody ->
                val bytes = responseBody.bytes()
                _downloadState.value = DownloadInvoiceState.Success(bytes)
                SnackbarHelper.showSuccess("PDF downloaded successfully")
            }.onFailure { error ->
                _downloadState.value = DownloadInvoiceState.Error(error.message ?: "Failed to download invoice")
                SnackbarHelper.showError(error.message ?: "Failed to download invoice")
            }
        }
    }

    fun resendInvoiceEmail(bookingId: Long) {
        viewModelScope.launch {
            _resendEmailState.value = ResendEmailState.Loading
            val result = resendInvoiceEmailUseCase(bookingId)
            result.onSuccess {
                _resendEmailState.value = ResendEmailState.Success("Invoice email resent successfully")
                SnackbarHelper.showSuccess("Invoice email resent")
            }.onFailure { error ->
                _resendEmailState.value = ResendEmailState.Error(error.message ?: "Failed to resend email")
                SnackbarHelper.showError(error.message ?: "Failed to resend email")
            }
        }
    }

    fun clearDownloadState() {
        if (_downloadState.value !is DownloadInvoiceState.Loading) {
            _downloadState.value = DownloadInvoiceState.Idle
        }
    }

    fun clearResendEmailState() {
        if (_resendEmailState.value !is ResendEmailState.Loading) {
            _resendEmailState.value = ResendEmailState.Idle
        }
    }

    fun resetInvoices() {
        _invoicesState.value = InvoicesState.Loading
        _invoiceDetailState.value = InvoiceDetailState.Loading
        _downloadState.value = DownloadInvoiceState.Idle
        _resendEmailState.value = ResendEmailState.Idle
    }
}