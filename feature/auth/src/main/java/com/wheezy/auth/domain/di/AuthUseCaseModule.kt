package com.wheezy.skyflight.feature.auth.domain.di

import com.wheezy.skyflight.core.common.manager.FCMTokenManager
import com.wheezy.skyflight.core.common.manager.WebSocketManager
import com.wheezy.skyflight.core.network.manager.TokenManager
import com.wheezy.skyflight.feature.auth.domain.repository.AuthRepository
import com.wheezy.skyflight.feature.auth.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthUseCaseModule {

    @Provides
    @Singleton
    fun provideLoginUseCase(
        authRepository: AuthRepository
    ): LoginUseCase = LoginUseCase(authRepository)

    @Provides
    @Singleton
    fun provideRegisterUseCase(
        authRepository: AuthRepository
    ): RegisterUseCase = RegisterUseCase(authRepository)

    @Provides
    @Singleton
    fun provideGoogleAuthUseCase(
        authRepository: AuthRepository
    ): GoogleAuthUseCase = GoogleAuthUseCase(authRepository)

    @Provides
    @Singleton
    fun provideLogoutUseCase(
        authRepository: AuthRepository,
        fcmTokenManager: FCMTokenManager,
        webSocketManager: WebSocketManager
    ): LogoutUseCase = LogoutUseCase(
        authRepository = authRepository,
        fcmTokenManager = fcmTokenManager,
        webSocketManager = webSocketManager
    )

    @Provides
    @Singleton
    fun provideGetCurrentUserUseCase(
        authRepository: AuthRepository,
        tokenManager: TokenManager
    ): GetCurrentUserUseCase = GetCurrentUserUseCase(
        authRepository = authRepository,
        tokenManager = tokenManager
    )

    @Provides
    @Singleton
    fun provideInitializeWebSocketUseCase(
        webSocketManager: WebSocketManager,
        fcmTokenManager: FCMTokenManager
    ): InitializeWebSocketUseCase = InitializeWebSocketUseCase(
        webSocketManager = webSocketManager,
        fcmTokenManager = fcmTokenManager
    )
}