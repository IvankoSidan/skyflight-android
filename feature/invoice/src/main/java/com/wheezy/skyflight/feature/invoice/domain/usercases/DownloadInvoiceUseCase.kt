package com.wheezy.skyflight.feature.invoice.domain.usecase

import com.wheezy.skyflight.feature.invoice.domain.repository.InvoiceRepository
import okhttp3.ResponseBody
import javax.inject.Inject

class DownloadInvoiceUseCase @Inject constructor(
    private val repository: InvoiceRepository
) {
    suspend operator fun invoke(invoiceId: Long): Result<ResponseBody> {
        return repository.downloadInvoice(invoiceId)
    }
}