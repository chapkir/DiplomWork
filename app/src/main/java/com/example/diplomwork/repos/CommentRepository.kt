package com.example.diplomwork.repos

import com.example.diplomwork.model.Comment
import com.example.diplomwork.model.CommentRequest
import com.example.diplomwork.model.CommentResponse
import com.example.diplomwork.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepository @Inject constructor(private val apiService: ApiService) {

    suspend fun getComments(pinId: Long): List<Comment> {
        return apiService.getComments(pinId)
    }

    suspend fun addComment(pinId: Long, comment: CommentRequest): CommentResponse {
        return apiService.addComment(pinId, comment)
    }
}