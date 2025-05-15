package com.example.diplomwork.data.repos

import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.data.model.EditProfileRequest
import com.example.diplomwork.data.model.PictureResponse
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

    suspend fun getOwnProfilePictures(): List<PictureResponse> {
        return apiService.getOwnProfilePictures()
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

    suspend fun getLikedPictures(): Result<List<PictureResponse>> {
        return try {
            val response = apiService.getLikedPictures()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}