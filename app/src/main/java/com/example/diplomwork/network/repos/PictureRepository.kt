package com.example.diplomwork.network.repos

import android.util.Log
import com.example.diplomwork.model.PictureResponse
import com.example.diplomwork.model.Comment
import com.example.diplomwork.model.CommentRequest
import com.example.diplomwork.model.CommentResponse
import com.example.diplomwork.model.PageResponse
import com.example.diplomwork.network.ApiService
import dagger.hilt.android.scopes.ActivityScoped
import okhttp3.MultipartBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton
import com.example.diplomwork.auth.SessionManager
import okhttp3.RequestBody

@Singleton
class PictureRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

    // Получение всех картинок
    suspend fun getPictures(): List<PictureResponse> {
        return apiService.getPictures()
    }

    // Получение одной картинки по ID
    suspend fun getPicture(id: Long): PictureResponse {
        return apiService.getPicture(id)
    }

    // Лайк картинки
    suspend fun likePicture(pinId: Long): Response<Unit> {
        return apiService.likePicture(pinId)
    }

    // Убрать лайк с картинки
    suspend fun unlikePicture(pinId: Long): Response<Unit> {
        return apiService.unlikePicture(pinId)
    }

    // Получение комментариев для картинки
    suspend fun getComments(pinId: Long): List<Comment> {
        return try {
            val response = apiService.getComments(pinId)
            response.data ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Добавление комментария
    suspend fun addComment(pinId: Long, comment: CommentRequest): CommentResponse {
        return apiService.addComment(pinId, comment)
    }

    //Поиск картинки
    suspend fun searchPictures(query: String, page: Int, size: Int): List<PictureResponse> {
        return apiService.searchPictures(query, page, size).data.content
    }

    suspend fun deletePicture(id: Long): Boolean {
        return try {
            val response = apiService.deletePicture(id)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    // Получение имени текущего пользователя
    fun getCurrentUsername(): String {
        return sessionManager.username ?: ""
    }
}