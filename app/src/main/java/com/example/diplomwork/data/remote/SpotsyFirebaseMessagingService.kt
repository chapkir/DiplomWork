package com.example.diplomwork.data.remote

import android.util.Log
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
class SpotsyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var repository: FirebaseTokenRepository

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
        CoroutineScope(Dispatchers.IO).launch {
            try {
                //repository.sendToken(token)
                Log.e("FCM", "Токен отправлен")
            } catch (e: Exception) {
                Log.e("FCM", "Ошибка при отправке токена", e)
            }
        }
    }
}