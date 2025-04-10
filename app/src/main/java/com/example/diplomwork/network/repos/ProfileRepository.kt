package com.example.diplomwork.network.repos

import com.example.diplomwork.model.PictureResponse
import com.example.diplomwork.model.ProfileResponse
import com.example.diplomwork.network.api.ApiService
import dagger.hilt.android.scopes.ActivityScoped
import okhttp3.MultipartBody
import retrofit2.Response
import javax.inject.Inject

@ActivityScoped
class ProfileRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getOwnProfile(): ProfileResponse {
        return apiService.getOwnProfile()
    }

    suspend fun getProfileById(userId: Long?): ProfileResponse {
        return apiService.getProfileById(userId)
    }

    // Загрузка изображения профиля
    suspend fun uploadProfileImage(image: MultipartBody.Part): Response<Map<String, String>> {
        return apiService.uploadProfileImage(image)
    }

    suspend fun getLikedPictures(): Result<List<PictureResponse>> {
        return try {
            val response = apiService.getLikedPictures()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}