package com.example.diplomwork.data.repos

import com.example.diplomwork.data.api.ApiService
import com.example.diplomwork.data.model.ApiResponseWrapper
import com.example.diplomwork.data.model.SpotResponse
import com.example.diplomwork.data.model.PostResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun uploadSpot(
        files: List<MultipartBody.Part>,
        title: RequestBody,
        description: RequestBody,
        rating: RequestBody,
    ): Response<ApiResponseWrapper<SpotResponse>> {
        return apiService.uploadSpot(
            files = files,
            title = title,
            description = description,
            rating = rating
        )
    }

    suspend fun uploadPost(
        file: MultipartBody.Part,
        description: RequestBody
    ): Response<PostResponse> {
        return apiService.uploadPost(file, description)
    }
}