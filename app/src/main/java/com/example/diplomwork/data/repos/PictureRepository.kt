package com.example.diplomwork.data.repos

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.diplomwork.data.model.PictureResponse
import com.example.diplomwork.data.api.ApiService
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.data.paging.PictureFactoryPaging
import kotlinx.coroutines.flow.Flow

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

    suspend fun deletePicture(id: Long): Result<Unit> {
        return try {
            val response = apiService.deletePicture(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Ошибка удаления: код ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUsername(): String {
        return sessionManager.username ?: ""
    }

    fun getPagingPictures(): Flow<PagingData<PictureResponse>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 15,
                initialLoadSize = 20
            ),
            pagingSourceFactory = { PictureFactoryPaging(apiService) }
        ).flow
    }
}