package com.example.diplomwork.data.repos

import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.data.model.EditProfileRequest
import com.example.diplomwork.data.model.SpotResponse
import com.example.diplomwork.data.model.ProfileResponse
import com.example.diplomwork.data.api.ApiService
import com.example.diplomwork.data.model.PostResponse
import dagger.hilt.android.scopes.ActivityScoped
import okhttp3.MultipartBody
import retrofit2.Response
import javax.inject.Inject

@ActivityScoped
class ProfileRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun getOwnProfile(): ProfileResponse {
        return apiService.getOwnProfile()
    }

    suspend fun getProfileById(userId: Long?): ProfileResponse {
        return apiService.getProfileById(userId)
    }

    suspend fun getOwnProfilePictures(): List<SpotResponse> {
        return apiService.getOwnProfilePictures()
    }

    suspend fun getOtherProfilePictures(userId: Long): List<SpotResponse> {
        return apiService.getOtherProfilePictures(userId)
    }

    suspend fun getOwnProfilePosts(): List<PostResponse> {
        return apiService.getOwnProfilePosts()
    }

    suspend fun uploadProfileImage(image: MultipartBody.Part): Response<Map<String, String>> {
        return apiService.uploadProfileImage(image)
    }

    suspend fun editProfile(editProfileRequest: EditProfileRequest) {
        return apiService.editProfile(editProfileRequest)
    }

    fun getOwnUsername(): String {
        return sessionManager.username ?: ""
    }

    fun getOwnUserId(): Long {
        return sessionManager.userId ?: 0L
    }

    fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }

    suspend fun getOwnLikedPictures(): Result<List<SpotResponse>> {
        return try {
            val response = apiService.getOwnLikedPictures()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOtherLikedPictures(userId: Long): Result<List<SpotResponse>> {
        return try {
            val response = apiService.getOtherLikedPictures(userId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}