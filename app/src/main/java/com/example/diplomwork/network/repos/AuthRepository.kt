package com.example.diplomwork.network.repos

import com.example.diplomwork.model.LoginRequest
import com.example.diplomwork.model.LoginResponse
import com.example.diplomwork.model.TokenRefreshRequest
import com.example.diplomwork.model.TokenRefreshResponse
import com.example.diplomwork.model.RegisterRequest
import com.example.diplomwork.model.RegisterResponse
import com.example.diplomwork.network.api.ApiService
import dagger.hilt.android.scopes.ActivityScoped
import retrofit2.Response
import javax.inject.Inject

@ActivityScoped
class AuthRepository @Inject constructor(
    private val apiService: ApiService
) {

    // Логин
    suspend fun login(loginRequest: LoginRequest): LoginResponse {
        return apiService.login(loginRequest)
    }

    // Обновление токена
    suspend fun refreshToken(request: TokenRefreshRequest): TokenRefreshResponse {
        return apiService.refreshToken(request)
    }

    // Логаут
    suspend fun logout(): Response<Map<String, String>> {
        return apiService.logout()
    }

    // Регистрация пользователя
    suspend fun register(registerRequest: RegisterRequest): RegisterResponse {
        return apiService.register(registerRequest)
    }

    // Проверка существования пользователя
    suspend fun checkUserExists(login: String): Response<Boolean> {
        return apiService.checkUserExists(login)
    }
}