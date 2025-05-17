package com.example.diplomwork.data.remote

import android.util.Log
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.data.repos.FirebaseTokenRepository
import com.example.diplomwork.presentation.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SpotsyFirebaseMessagingService() : FirebaseMessagingService() {

    @Inject
    lateinit var repository: FirebaseTokenRepository

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()
        // Hilt сам инжектит всё при старте сервиса
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Проверяем, пришли ли данные
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "Уведомление"
            val message = remoteMessage.data["body"] ?: ""
            NotificationHelper.showNotification(
                context = applicationContext,
                title = title,
                message = message
            )
        }

        // Обрабатываем уведомления с помощью NotificationCompat
        remoteMessage.notification?.let {
            NotificationHelper.showNotification(
                context = applicationContext,
                title = it.title ?: "",
                message = it.body ?: ""
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        sessionManager.savePendingFcmToken(token)

        if (sessionManager.isLoggedIn()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    repository.sendFcmToken(token)
                    sessionManager.clearPendingFcmToken()
                    Log.d("FCM", "FCM токен отправлен сразу, пользователь авторизован")
                } catch (e: Exception) {
                    Log.e("FCM", "Ошибка при отправке FCM токена", e)
                }
            }
        } else {
            Log.d("FCM", "Токен сохранён, но пользователь не авторизован")
        }
    }
}