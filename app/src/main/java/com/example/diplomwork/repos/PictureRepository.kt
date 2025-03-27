package com.example.diplomwork.repos

import com.example.diplomwork.model.PictureResponse
import com.example.diplomwork.model.Comment
import com.example.diplomwork.model.CommentRequest
import com.example.diplomwork.model.CommentResponse
import com.example.diplomwork.network.ApiService
import dagger.hilt.android.scopes.ActivityScoped
import okhttp3.MultipartBody
import retrofit2.Response
import javax.inject.Inject

@ActivityScoped
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
}