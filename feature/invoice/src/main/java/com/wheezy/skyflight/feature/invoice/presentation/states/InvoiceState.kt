package com.wheezy.skyflight.feature.invoice.presentation.states

import com.wheezy.skyflight.core.model.Invoice
import com.wheezy.skyflight.core.model.InvoiceListResponse

sealed class InvoicesState {
    object Loading : InvoicesState()
    data class Success(val data: InvoiceListResponse) : InvoicesState()
    data class Error(val message: String) : InvoicesState()
}

sealed class InvoiceDetailState {
    object Loading : InvoiceDetailState()
    data class Success(val invoice: Invoice) : InvoiceDetailState()
    data class Error(val message: String) : InvoiceDetailState()
}

sealed class DownloadInvoiceState {
    object Idle : DownloadInvoiceState()
    object Loading : DownloadInvoiceState()
    data class Success(val bytes: ByteArray) : DownloadInvoiceState() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Success

            return bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int {
            return bytes.contentHashCode()
        }
    }
    data class Error(val message: String) : DownloadInvoiceState()
}

sealed class ResendEmailState {
    object Idle : ResendEmailState()
    object Loading : ResendEmailState()
    data class Success(val message: String) : ResendEmailState()
    data class Error(val message: String) : ResendEmailState()
}