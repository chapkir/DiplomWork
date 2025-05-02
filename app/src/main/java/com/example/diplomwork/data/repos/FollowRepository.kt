package com.example.diplomwork.data.repos

import com.example.diplomwork.data.api.ApiService
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun subscribe(followerId: Long, followingId: Long): Response<Unit> {
        return apiService.subscribe(followerId, followingId)
    }

    suspend fun unsubscribe(followerId: Long, followingId: Long): Response<Unit> {
        return apiService.unsubscribe(followerId, followingId)
    }
}