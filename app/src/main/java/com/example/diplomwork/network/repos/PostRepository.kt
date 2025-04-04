package com.example.diplomwork.network.repos

import com.example.diplomwork.model.PostResponse
import com.example.diplomwork.network.api.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun getPosts(): List<PostResponse> {
        return apiService.getPosts()
    }
}