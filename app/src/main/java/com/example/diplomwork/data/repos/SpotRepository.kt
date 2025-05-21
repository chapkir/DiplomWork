package com.example.diplomwork.data.repos

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.data.api.SpotApi
import com.example.diplomwork.data.model.ApiResponseWrapper
import com.example.diplomwork.data.model.CommentRequest
import com.example.diplomwork.data.model.CommentResponse
import com.example.diplomwork.data.model.SpotDetailResponse
import com.example.diplomwork.data.model.SpotPicturesResponse
import com.example.diplomwork.data.model.SpotResponse
import com.example.diplomwork.data.paging.SpotPagingSource
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpotRepository @Inject constructor(
    private val api: SpotApi,
    private val sessionManager: SessionManager
) {

    fun getSpotsPagingFlow(pageSize: Int = 5): Flow<PagingData<SpotResponse>> {
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { SpotPagingSource(api, pageSize) }
        ).flow
    }

    suspend fun getSpotPictures(spotId: Long): SpotPicturesResponse {
        return api.getSpotPictures(spotId)
    }

    suspend fun getSpotDetail(id: Long): SpotDetailResponse {
        return api.getSpotDetail(id)
    }

    suspend fun likePicture(pictureId: Long): Response<Unit> {
        return api.likeSpot(pictureId)
    }

    suspend fun unlikePicture(pictureId: Long): Response<Unit> {
        return api.unlikeSpot(pictureId)
    }

    suspend fun searchPictures(query: String, page: Int, size: Int): List<SpotResponse> {
        return api.searchSpots(query, page, size).data.content
    }

    suspend fun deletePicture(id: Long): Result<Unit> {
        return try {
            val response = api.deleteSpot(id)
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

//    fun getPagingPictures(): Flow<PagingData<SpotResponse>> {
//        return Pager(
//            config = PagingConfig(
//                pageSize = 20,
//                prefetchDistance = 15,
//                initialLoadSize = 20
//            ),
//            pagingSourceFactory = { PictureFactoryPaging(api) }
//        ).flow
//    }

    suspend fun uploadSpot(
        files: List<MultipartBody.Part>,
        title: RequestBody,
        description: RequestBody,
        rating: RequestBody,
    ): Response<ApiResponseWrapper<SpotResponse>> {
        return api.uploadSpot(
            files = files,
            title = title,
            description = description,
            rating = rating
        )
    }

    suspend fun getSpotComments(pictureId: Long): List<CommentResponse> {
        return try {
            val response = api.getSpotComments(pictureId)
            response.data.content
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addSpotComment(pictureId: Long, comment: CommentRequest): CommentResponse {
        return api.addSpotComment(pictureId, comment)
    }
}