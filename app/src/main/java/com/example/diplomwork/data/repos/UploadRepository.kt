package com.example.diplomwork.data.repos

import com.example.diplomwork.data.model.PictureResponse
import com.example.diplomwork.data.model.PostResponse
import com.example.diplomwork.data.api.ApiService
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun uploadImage(
        file: MultipartBody.Part,
        description: RequestBody
    ): Response<PictureResponse> {
        return apiService.uploadImage(file, description)
    }

    suspend fun uploadPost(
        file: MultipartBody.Part,
        description: RequestBody
    ): Response<PostResponse> {
        return apiService.uploadPost(file, description)
    }
}