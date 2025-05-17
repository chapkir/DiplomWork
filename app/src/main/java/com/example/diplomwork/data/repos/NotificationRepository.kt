package com.example.diplomwork.data.repos

import com.example.diplomwork.data.api.NotificationApi
import com.example.diplomwork.data.model.NotificationResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val api: NotificationApi
) {

    suspend fun getNotifications(): List<NotificationResponse> {
        return api.getNotifications()
    }
}