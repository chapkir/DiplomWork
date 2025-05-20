package com.example.diplomwork.data.repos

import android.util.Log
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.data.api.AuthApi
import com.example.diplomwork.data.model.LoginRequest
import com.example.diplomwork.data.model.LoginResponse
import com.example.diplomwork.data.model.RegisterRequest
import com.example.diplomwork.data.model.RegisterResponse
import com.example.diplomwork.data.model.TokenRefreshRequest
import com.example.diplomwork.data.model.TokenRefreshResponse
import com.example.diplomwork.data.model.UserExistsResponse
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@ActivityScoped
class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val fcmRepository: FirebaseTokenRepository,
    private val sessionManager: SessionManager
) {

    suspend fun login(request: LoginRequest): LoginResponse {
        val response = api.login(request)
        if (response.isSuccessful) {
            val body = response.body() ?: throw IOException("Пустое тело от сервера")

            sessionManager.saveAuthData(body.token, body.refreshToken)

            val pendingToken = sessionManager.getPendingFcmToken()
            if (pendingToken != null) {
                try {
                    withContext(Dispatchers.IO) {
                        fcmRepository.sendFcmToken(pendingToken)
                        sessionManager.clearPendingFcmToken()
                        Log.d("FCM", "FCM токен отправлен после авторизации (из SessionManager)")
                    }
                } catch (e: Exception) {
                    Log.e("FCM", "Ошибка отправки pending FCM токена", e)
                }
            }

            try {
                val newFcmToken = FirebaseMessaging.getInstance().token.await()
                withContext(Dispatchers.IO) {
                    fcmRepository.sendFcmToken(newFcmToken)
                    Log.d("FCM", "FCM токен отправлен явно после логина")
                }
            } catch (e: Exception) {
                Log.e("FCM", "Не удалось получить и отправить FCM токен после логина", e)
            }

            return body
        } else {
            throw HttpException(response)
        }
    }

    // Обновление токена
    suspend fun refreshToken(request: TokenRefreshRequest): TokenRefreshResponse {
        return api.refreshToken(request)
    }

    // Логаут
    suspend fun logout(): Response<Map<String, String>> {
        val response = api.logout()
        //if (response.isSuccessful) {
            sessionManager.clearSession()
        //}
        return response
    }

    // Регистрация пользователя
    suspend fun register(registerRequest: RegisterRequest): RegisterResponse {
        return api.register(registerRequest)
    }

    // Проверка существования пользователя
    suspend fun checkUsernameExists(username: String): UserExistsResponse {
        return api.checkUsernameExists(username)
    }
}