package com.wheezy.skyflight.core.network.api

import com.wheezy.skyflight.core.network.model.NotificationSettingsDTO
import retrofit2.Response
import retrofit2.http.*

interface NotificationSettingsApiService {

    @GET("/api/users/notification-settings")
    suspend fun getNotificationSettings(): Response<NotificationSettingsDTO>

    @PUT("/api/users/notification-settings")
    suspend fun updateNotificationSettings(
        @Body settings: NotificationSettingsDTO
    ): Response<NotificationSettingsDTO>
}