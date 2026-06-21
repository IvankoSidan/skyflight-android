package com.wheezy.skyflight.feature.notifications.data.repository

import com.wheezy.skyflight.core.common.manager.NetworkMonitor
import com.wheezy.skyflight.core.network.api.NotificationSettingsApiService
import com.wheezy.skyflight.core.network.model.NotificationSettingsDTO
import com.wheezy.skyflight.feature.notifications.data.local.NotificationSettingsLocalDataSource
import com.wheezy.skyflight.feature.notifications.domain.model.NotificationSettings
import com.wheezy.skyflight.feature.notifications.domain.repository.NotificationSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationSettingsRepositoryImpl @Inject constructor(
    private val localDataSource: NotificationSettingsLocalDataSource,
    private val networkMonitor: NetworkMonitor,
    private val notificationSettingsApiService: NotificationSettingsApiService
) : NotificationSettingsRepository {

    override fun getSettings(): Flow<NotificationSettings> = localDataSource.settingsFlow

    override suspend fun updateSettings(settings: NotificationSettings) {
        // 1. Сохраняем локально
        localDataSource.updateSettings(settings)

        // 2. Синхронизируем с сервером (используем updateNotificationSettings)
        syncToServer(settings)

        // 3. После обновления на сервере - получаем актуальные настройки обратно
        // Используем getNotificationSettings для проверки
        if (networkMonitor.isConnected.value) {
            try {
                val response = notificationSettingsApiService.getNotificationSettings()
                if (response.isSuccessful && response.body() != null) {
                    val serverSettings = response.body()!!
                    // Если на сервере другие настройки - обновляем локально
                    val local = localDataSource.settingsFlow.first()
                    if (local != settings) {
                        localDataSource.updateSettings(
                            NotificationSettings(
                                bookingCreated = serverSettings.bookingCreated,
                                bookingConfirmed = serverSettings.bookingConfirmed,
                                bookingCancelled = serverSettings.bookingCancelled,
                                paymentSuccess = serverSettings.paymentSuccess,
                                paymentFailed = serverSettings.paymentFailed,
                                flightReminder = serverSettings.flightReminder,
                                flightStatusUpdate = serverSettings.flightStatusUpdate,
                                massPromotion = serverSettings.massPromotion,
                                thankYouAfterFlight = serverSettings.thankYouAfterFlight,
                                quietHoursEnabled = serverSettings.quietHoursEnabled,
                                quietHoursStart = serverSettings.quietHoursStart,
                                quietHoursEnd = serverSettings.quietHoursEnd,
                                pushEnabled = serverSettings.pushEnabled,
                                emailEnabled = serverSettings.emailEnabled
                            )
                        )
                    }
                }
            } catch (_: Exception) {
                // игнорируем ошибку получения настроек
            }
        }
    }

    private suspend fun syncToServer(settings: NotificationSettings) {
        if (!networkMonitor.isConnected.value) return

        try {
            val response = notificationSettingsApiService.updateNotificationSettings(
                NotificationSettingsDTO(
                    bookingCreated = settings.bookingCreated,
                    bookingConfirmed = settings.bookingConfirmed,
                    bookingCancelled = settings.bookingCancelled,
                    paymentSuccess = settings.paymentSuccess,
                    paymentFailed = settings.paymentFailed,
                    flightReminder = settings.flightReminder,
                    flightStatusUpdate = settings.flightStatusUpdate,
                    massPromotion = settings.massPromotion,
                    thankYouAfterFlight = settings.thankYouAfterFlight,
                    quietHoursEnabled = settings.quietHoursEnabled,
                    quietHoursStart = settings.quietHoursStart,
                    quietHoursEnd = settings.quietHoursEnd,
                    pushEnabled = settings.pushEnabled,
                    emailEnabled = settings.emailEnabled
                )
            )

            // Если обновление не удалось - пробуем получить текущие настройки с сервера
            if (!response.isSuccessful) {
                val getResponse = notificationSettingsApiService.getNotificationSettings()
                if (getResponse.isSuccessful && getResponse.body() != null) {
                    val serverDto = getResponse.body()!!
                    localDataSource.updateSettings(
                        NotificationSettings(
                            bookingCreated = serverDto.bookingCreated,
                            bookingConfirmed = serverDto.bookingConfirmed,
                            bookingCancelled = serverDto.bookingCancelled,
                            paymentSuccess = serverDto.paymentSuccess,
                            paymentFailed = serverDto.paymentFailed,
                            flightReminder = serverDto.flightReminder,
                            flightStatusUpdate = serverDto.flightStatusUpdate,
                            massPromotion = serverDto.massPromotion,
                            thankYouAfterFlight = serverDto.thankYouAfterFlight,
                            quietHoursEnabled = serverDto.quietHoursEnabled,
                            quietHoursStart = serverDto.quietHoursStart,
                            quietHoursEnd = serverDto.quietHoursEnd,
                            pushEnabled = serverDto.pushEnabled,
                            emailEnabled = serverDto.emailEnabled
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // игнорируем ошибку синхронизации
        }
    }

    override suspend fun shouldSendNotification(notificationType: String, userId: Long): Boolean {
        // Получаем настройки и проверяем их актуальность с сервера
        val settings = localDataSource.settingsFlow.first()

        // Если интернет есть - проверяем актуальность настроек
        if (networkMonitor.isConnected.value) {
            try {
                val response = notificationSettingsApiService.getNotificationSettings()
                if (response.isSuccessful && response.body() != null) {
                    val serverSettings = response.body()!!
                    val local = localDataSource.settingsFlow.first()

                    // Если на сервере другие настройки - обновляем локальные
                    if (local != settings) {
                        localDataSource.updateSettings(
                            NotificationSettings(
                                bookingCreated = serverSettings.bookingCreated,
                                bookingConfirmed = serverSettings.bookingConfirmed,
                                bookingCancelled = serverSettings.bookingCancelled,
                                paymentSuccess = serverSettings.paymentSuccess,
                                paymentFailed = serverSettings.paymentFailed,
                                flightReminder = serverSettings.flightReminder,
                                flightStatusUpdate = serverSettings.flightStatusUpdate,
                                massPromotion = serverSettings.massPromotion,
                                thankYouAfterFlight = serverSettings.thankYouAfterFlight,
                                quietHoursEnabled = serverSettings.quietHoursEnabled,
                                quietHoursStart = serverSettings.quietHoursStart,
                                quietHoursEnd = serverSettings.quietHoursEnd,
                                pushEnabled = serverSettings.pushEnabled,
                                emailEnabled = serverSettings.emailEnabled
                            )
                        )
                        // Используем обновленные настройки
                        val updatedSettings = localDataSource.settingsFlow.first()
                        return checkNotificationType(updatedSettings, notificationType)
                    }
                }
            } catch (_: Exception) {
                // игнорируем ошибку
            }
        }

        return checkNotificationType(settings, notificationType)
    }

    private fun checkNotificationType(settings: NotificationSettings, notificationType: String): Boolean {
        if (!settings.pushEnabled) return false
        if (settings.isQuietHour()) return false

        return when (notificationType) {
            "booking_created" -> settings.bookingCreated
            "booking_confirmed" -> settings.bookingConfirmed
            "booking_cancelled" -> settings.bookingCancelled
            "payment_success" -> settings.paymentSuccess
            "payment_failed" -> settings.paymentFailed
            "reminder" -> settings.flightReminder
            "flight_status_update" -> settings.flightStatusUpdate
            "mass_promotion" -> settings.massPromotion
            "thank_you" -> settings.thankYouAfterFlight
            else -> true
        }
    }
}