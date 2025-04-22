package com.example.diplomwork.network.repos

import com.example.diplomwork.model.NotificationResponse
import com.example.diplomwork.network.api.ApiService
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