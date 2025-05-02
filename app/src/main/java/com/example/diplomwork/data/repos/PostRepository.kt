package com.example.diplomwork.data.repos

import com.example.diplomwork.data.model.PostResponse
import com.example.diplomwork.data.api.ApiService
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val apiService: ApiService,
) {
    suspend fun getPosts(): List<PostResponse> {
        return apiService.getPosts()
    }

    suspend fun deletePost(id: Long): Boolean {
        return try {
            val response = apiService.deletePost(id)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun likePost(postId: Long): Response<Unit> {
        return apiService.likePost(postId)
    }

    suspend fun unlikePost(postId: Long): Response<Unit> {
        return apiService.unlikePost(postId)
    }
}