package com.example.diplomwork.network.repos

import com.example.diplomwork.model.ProfileResponse
import com.example.diplomwork.network.ApiService
import dagger.hilt.android.scopes.ActivityScoped
import okhttp3.MultipartBody
import retrofit2.Response
import javax.inject.Inject

@ActivityScoped
class ProfileRepository @Inject constructor(
    private val apiService: ApiService
) {

    // Получение профиля пользователя
    suspend fun getProfile(): ProfileResponse {
        return apiService.getProfile()
    }

    // Загрузка изображения профиля
    suspend fun uploadProfileImage(image: MultipartBody.Part): Response<Map<String, String>> {
        return apiService.uploadProfileImage(image)
    }

    // Загрузка аватара пользователя
    suspend fun uploadAvatar(file: MultipartBody.Part): Response<String> {
        return apiService.uploadAvatar(file)
    }
}