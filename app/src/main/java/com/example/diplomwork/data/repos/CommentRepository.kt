package com.example.diplomwork.data.repos

import com.example.diplomwork.data.model.CommentRequest
import com.example.diplomwork.data.model.CommentResponse
import com.example.diplomwork.data.api.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepository @Inject constructor(private val apiService: ApiService) {

    suspend fun getPictureComments(pictureId: Long): List<CommentResponse> {
        return try {
            val response = apiService.getPictureComments(pictureId)
            response.data ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addPictureComment(pictureId: Long, comment: CommentRequest): CommentResponse {
        return apiService.addPictureComment(pictureId, comment)
    }

    suspend fun getPostComments(postId: Long): List<CommentResponse> {
        return try {
            val response = apiService.getPostComments(postId)
            response ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addPostComment(postId: Long, comment: CommentRequest): CommentResponse {
        return apiService.addPostComment(postId, comment)
    }
}