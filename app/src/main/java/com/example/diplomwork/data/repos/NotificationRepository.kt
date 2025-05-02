package com.example.diplomwork.data.repos

import com.example.diplomwork.data.model.NotificationResponse
import com.example.diplomwork.data.api.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getNotifications(): List<NotificationResponse> {
        return apiService.getNotifications()
    }
}