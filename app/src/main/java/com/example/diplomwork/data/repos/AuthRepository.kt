package com.example.diplomwork.data.repos

import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.data.api.ApiService
import com.example.diplomwork.data.model.LoginRequest
import com.example.diplomwork.data.model.LoginResponse
import com.example.diplomwork.data.model.RegisterRequest
import com.example.diplomwork.data.model.RegisterResponse
import com.example.diplomwork.data.model.TokenRefreshRequest
import com.example.diplomwork.data.model.TokenRefreshResponse
import com.example.diplomwork.data.model.UserExistsResponse
import dagger.hilt.android.scopes.ActivityScoped
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@ActivityScoped
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

    // Логин
    suspend fun login(request: LoginRequest): LoginResponse {
        val response = apiService.login(request)
        if (response.isSuccessful) {
            return response.body() ?: throw IOException("Пустое тело от сервера")
        } else {
            throw HttpException(response)
        }
    }

    // Обновление токена
    suspend fun refreshToken(request: TokenRefreshRequest): TokenRefreshResponse {
        return apiService.refreshToken(request)
    }

    // Логаут
    suspend fun logout(): Response<Map<String, String>> {
        val response = apiService.logout()
        if (response.isSuccessful) {
            sessionManager.clearSession()
        }
        return response
    }

    suspend fun deleteAccount(): Response<Unit>{
        return apiService.deleteAccount()
    }

    // Регистрация пользователя
    suspend fun register(registerRequest: RegisterRequest): RegisterResponse {
        return apiService.register(registerRequest)
    }

    // Проверка существования пользователя
    suspend fun checkUsernameExists(username: String): UserExistsResponse {
        return apiService.checkUsernameExists(username)
    }
}