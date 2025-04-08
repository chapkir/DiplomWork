package com.example.diplomwork.network.repos

import com.example.diplomwork.model.PictureResponse
import com.example.diplomwork.model.CommentRequest
import com.example.diplomwork.model.CommentResponse
import com.example.diplomwork.network.api.ApiService
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton
import com.example.diplomwork.auth.SessionManager

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
    suspend fun likePicture(pictureId: Long): Response<Unit> {
        return apiService.likePicture(pictureId)
    }

    // Убрать лайк с картинки
    suspend fun unlikePicture(pictureId: Long): Response<Unit> {
        return apiService.unlikePicture(pictureId)
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