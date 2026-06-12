package com.wheezy.skyflight.feature.auth.data.repository

import com.wheezy.skyflight.core.datastore.preferences.AuthPreferences
import com.wheezy.skyflight.core.model.User
import com.wheezy.skyflight.core.network.api.ApiService
import com.wheezy.skyflight.core.network.manager.TokenManager
import com.wheezy.skyflight.core.network.model.GoogleAuthDto
import com.wheezy.skyflight.core.network.model.UserLoginDto
import com.wheezy.skyflight.core.network.model.UserRegisterDto
import com.wheezy.skyflight.core.network.model.toDomain
import com.wheezy.skyflight.feature.auth.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authPreferences: AuthPreferences,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(dto: UserLoginDto): Result<User> {
        return try {
            val response = apiService.login(dto)
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.updateToken(authResponse.token)
                authPreferences.saveAuthData(authResponse.token, authResponse.user.id)
                Result.success(authResponse.user.toDomain())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(dto: UserRegisterDto): Result<User> {
        return try {
            val response = apiService.register(dto)
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.updateToken(authResponse.token)
                authPreferences.saveAuthData(authResponse.token, authResponse.user.id)
                Result.success(authResponse.user.toDomain())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun googleAuth(token: String): Result<User> {
        return try {
            val response = apiService.googleAuth(GoogleAuthDto(token))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.updateToken(authResponse.token)
                authPreferences.saveAuthData(authResponse.token, authResponse.user.id)
                Result.success(authResponse.user.toDomain())
            } else {
                Result.failure(Exception(response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.user.toDomain()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun logout() {
        tokenManager.clearToken()
        authPreferences.clearAuthData()
    }

    override suspend fun refreshToken(): String? {
        return try {
            val response = apiService.refreshToken()
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.updateToken(authResponse.token)
                authPreferences.saveAuthData(authResponse.token, authResponse.user.id)
                authResponse.token
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}