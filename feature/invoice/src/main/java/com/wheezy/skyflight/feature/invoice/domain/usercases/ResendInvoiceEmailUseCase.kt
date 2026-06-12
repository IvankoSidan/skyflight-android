package com.wheezy.skyflight.feature.invoice.domain.usecase

import com.wheezy.skyflight.feature.invoice.domain.repository.InvoiceRepository
import javax.inject.Inject

class ResendInvoiceEmailUseCase @Inject constructor(
    private val repository: InvoiceRepository
) {
    suspend operator fun invoke(bookingId: Long): Result<Unit> {
        return repository.resendInvoiceEmail(bookingId)
    }
}