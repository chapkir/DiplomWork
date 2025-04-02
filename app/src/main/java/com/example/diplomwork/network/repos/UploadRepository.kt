package com.example.diplomwork.network.repos

import com.example.diplomwork.model.PictureResponse
import com.example.diplomwork.network.ApiService
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun uploadImage(file: MultipartBody.Part, description: String): PictureResponse {
        return apiService.uploadImage(file, description)
    }
}