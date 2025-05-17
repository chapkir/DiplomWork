package com.example.diplomwork.data.repos

import android.util.Log
import com.example.diplomwork.data.api.NotificationApi
import com.example.diplomwork.data.model.FcmTokenRequest
import javax.inject.Inject

class FirebaseTokenRepository @Inject constructor(
    private val api: NotificationApi
) {
    suspend fun sendFcmToken(token: String) {
        api.sendFcmToken(FcmTokenRequest(token))
    }
}