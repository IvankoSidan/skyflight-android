package com.wheezy.skyflight.feature.invoice.domain.usecase

import com.wheezy.skyflight.core.model.InvoiceListResponse
import com.wheezy.skyflight.feature.invoice.domain.repository.InvoiceRepository
import javax.inject.Inject

class GetMyInvoicesUseCase @Inject constructor(
    private val repository: InvoiceRepository
) {
    suspend operator fun invoke(page: Int = 0, size: Int = 20): Result<InvoiceListResponse> {
        return repository.getMyInvoices(page, size)
    }
}