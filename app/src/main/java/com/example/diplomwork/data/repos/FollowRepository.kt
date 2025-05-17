package com.example.diplomwork.data.repos

import com.example.diplomwork.data.api.FollowApi
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FollowRepository @Inject constructor(
    private val api: FollowApi
) {

    suspend fun subscribe(followerId: Long, followingId: Long): Response<Unit> {
        return api.subscribe(followerId, followingId)
    }

    suspend fun unsubscribe(followerId: Long, followingId: Long): Response<Unit> {
        return api.unsubscribe(followerId, followingId)
    }

    suspend fun checkFollowing(followerId: Long, followingId: Long): Response<Boolean> {
        return api.checkFollowing(followerId, followingId)
    }
}