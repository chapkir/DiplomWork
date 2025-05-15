package com.example.diplomwork.data.repos

import android.util.Log
import com.example.diplomwork.data.api.ApiService
import javax.inject.Inject

class FirebaseTokenRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun sendTokenToServer(token: String) {
        //apiService.sendFcmToken(token)
        Log.d("FCM", "Отправка токена на сервер: $token")
    }
}