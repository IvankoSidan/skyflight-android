package com.wheezy.skyflight.feature.invoice.domain.usecase

import com.wheezy.skyflight.core.model.Invoice
import com.wheezy.skyflight.feature.invoice.domain.repository.InvoiceRepository
import javax.inject.Inject

class GetInvoiceByBookingIdUseCase @Inject constructor(
    private val repository: InvoiceRepository
) {
    suspend operator fun invoke(bookingId: Long): Result<Invoice> {
        return repository.getInvoiceByBookingId(bookingId)
    }
}