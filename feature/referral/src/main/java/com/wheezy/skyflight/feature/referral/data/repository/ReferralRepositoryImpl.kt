package com.wheezy.skyflight.feature.referral.data.repository

import com.wheezy.skyflight.core.network.api.ApiService
import com.wheezy.skyflight.core.network.model.ReferralApplyRequest
import com.wheezy.skyflight.core.network.model.ReferralApplyResponse
import com.wheezy.skyflight.core.network.model.ReferralCodeResponse
import com.wheezy.skyflight.core.network.model.ReferralInfoResponse
import com.wheezy.skyflight.feature.referral.domain.model.ApplyReferralResult
import com.wheezy.skyflight.feature.referral.domain.model.ReferralCode
import com.wheezy.skyflight.feature.referral.domain.model.ReferralInfo
import com.wheezy.skyflight.feature.referral.domain.model.ReferredUser
import com.wheezy.skyflight.feature.referral.domain.repository.ReferralRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReferralRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ReferralRepository {

    override suspend fun getReferralCode(): Result<ReferralCode> {
        return try {
            val response = apiService.getReferralCode()
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                Result.success(
                    ReferralCode(
                        code = dto.code,
                        usageCount = dto.usageCount,
                        maxUses = dto.maxUses,
                        isValid = dto.isValid,
                        shareLink = dto.shareLink
                    )
                )
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun applyReferralCode(code: String): Result<ApplyReferralResult> {
        return try {
            val response = apiService.applyReferralCode(ReferralApplyRequest(code))
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                Result.success(
                    ApplyReferralResult(
                        success = dto.success,
                        message = dto.message,
                        discountPercent = dto.discountPercent,
                        discountAmount = dto.discountAmount
                    )
                )
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMyReferrals(): Result<ReferralInfo> {
        return try {
            val response = apiService.getMyReferrals()
            if (response.isSuccessful && response.body() != null) {
                val dto = response.body()!!
                Result.success(
                    ReferralInfo(
                        myCode = dto.myCode,
                        myReferrals = dto.myReferrals.map { referral ->
                            ReferredUser(
                                email = referral.email,
                                name = referral.name,
                                registeredAt = referral.registeredAt,
                                status = referral.status
                            )
                        },
                        totalReferrals = dto.totalReferrals,
                        totalDiscountEarned = dto.totalDiscountEarned
                    )
                )
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}