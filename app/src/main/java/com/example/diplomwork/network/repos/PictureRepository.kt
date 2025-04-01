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

@Singleton
class PictureRepository @Inject constructor(
    private val apiService: ApiService
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
        return apiService.getComments(pinId)
    }

    // Добавление комментария
    suspend fun addComment(pinId: Long, comment: CommentRequest): CommentResponse {
        return apiService.addComment(pinId, comment)
    }

    // Загрузка картинки
    suspend fun uploadImage(file: MultipartBody.Part, description: String): PictureResponse {
        return apiService.uploadImage(file, description)
    }

    //Поиск картинки
    suspend fun searchPictures(query: String, page: Int, size: Int): List<PictureResponse> {
        return apiService.searchPictures(query, page, size).data.content
    }

    suspend fun deletePicture(id: Long): Boolean {
        return try {
            val response = apiService.deletePicture(id)
            response.isSuccessful // Вернет true, если удаление прошло успешно
        } catch (e: Exception) {
            false
        }
    }
}