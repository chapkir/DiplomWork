package com.example.diplomwork.data.repos

import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.data.api.ProfileApi
import com.example.diplomwork.data.model.EditProfileRequest
import com.example.diplomwork.data.model.PostResponse
import com.example.diplomwork.data.model.ProfileResponse
import com.example.diplomwork.data.model.SpotResponse
import dagger.hilt.android.scopes.ActivityScoped
import okhttp3.MultipartBody
import retrofit2.Response
import javax.inject.Inject

@ActivityScoped
class ProfileRepository @Inject constructor(
    private val api: ProfileApi,
    private val sessionManager: SessionManager
) {

    suspend fun getOwnProfile(): ProfileResponse {
        return api.getOwnProfile()
    }

    suspend fun getProfileById(userId: Long?): ProfileResponse {
        return api.getProfileById(userId)
    }

    suspend fun getOwnProfilePictures(): List<SpotResponse> {
        return api.getOwnProfilePictures()
    }

    suspend fun getOtherProfilePictures(userId: Long): List<SpotResponse> {
        return api.getOtherProfilePictures(userId)
    }

    suspend fun getOwnProfilePosts(): List<PostResponse> {
        return api.getOwnProfilePosts()
    }

    suspend fun uploadProfileImage(image: MultipartBody.Part): Response<Map<String, String>> {
        return api.uploadProfileImage(image)
    }

    suspend fun editProfile(editProfileRequest: EditProfileRequest) {
        return api.editProfile(editProfileRequest)
    }

    fun getOwnUserId(): Long {
        return sessionManager.userId ?: 0L
    }

    fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }

    suspend fun getOwnLikedPictures(): Result<List<SpotResponse>> {
        return try {
            val response = api.getOwnLikedPictures()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOtherLikedPictures(userId: Long): Result<List<SpotResponse>> {
        return try {
            val response = api.getOtherLikedPictures(userId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}