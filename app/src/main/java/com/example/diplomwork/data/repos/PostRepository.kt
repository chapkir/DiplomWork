package com.example.diplomwork.data.repos

import com.example.diplomwork.data.api.PostApi
import com.example.diplomwork.data.model.CommentRequest
import com.example.diplomwork.data.model.CommentResponse
import com.example.diplomwork.data.model.PostResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val api: PostApi,
) {
    suspend fun getPosts(): List<PostResponse> {
        return api.getPosts()
    }

    suspend fun deletePost(id: Long): Boolean {
        return try {
            val response = api.deletePost(id)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun likePost(postId: Long): Response<Unit> {
        return api.likePost(postId)
    }

    suspend fun unlikePost(postId: Long): Response<Unit> {
        return api.unlikePost(postId)
    }

    suspend fun getPostComments(postId: Long): List<CommentResponse> {
        return try {
            val response = api.getPostComments(postId)
            response ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addPostComment(postId: Long, comment: CommentRequest): CommentResponse {
        return api.addPostComment(postId, comment)
    }

    suspend fun uploadPost(
        file: MultipartBody.Part,
        description: RequestBody
    ): Response<PostResponse> {
        return api.uploadPost(file, description)
    }
}