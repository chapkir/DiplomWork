package com.example.diplomwork.network

import com.example.diplomwork.model.LoginRequest
import com.example.diplomwork.model.LoginResponse
import com.example.diplomwork.model.ProfileResponse
import com.example.diplomwork.model.Comment
import com.example.diplomwork.model.CommentRequest
import com.example.diplomwork.model.CommentResponse
import com.example.diplomwork.model.PictureResponse
import com.example.diplomwork.model.RegisterRequest
import com.example.diplomwork.model.RegisterResponse
import com.example.diplomwork.model.TokenRefreshRequest
import com.example.diplomwork.model.TokenRefreshResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("api/pins/all")
    suspend fun getPictures(): List<PictureResponse>

    @GET("api/pins/{id}")
    suspend fun getPicture(@Path("id") id: Long): PictureResponse

    @GET("api/pins/search")
    suspend fun searchPictures(@Query("query") query: String): List<PictureResponse>

    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: TokenRefreshRequest): TokenRefreshResponse

    @POST("api/auth/logout")
    suspend fun logout(): Response<Map<String, String>>

    @GET("api/profile")
    suspend fun getProfile(): ProfileResponse

    @POST("api/pins/{pinId}/likes")
    suspend fun likePicture(@Path("pinId") pinId: Long): Response<Unit>

    @DELETE("api/pins/{pinId}/likes")
    suspend fun unlikePicture(@Path("pinId") pinId: Long): Response<Unit>

    @GET("api/pins/{pinId}/comments")
    suspend fun getComments(@Path("pinId") pinId: Long): List<Comment>

    @POST("api/pins/{pinId}/comments")
    suspend fun addComment(
        @Path("pinId") pictureId: Long,
        @Body comment: CommentRequest
    ): CommentResponse

    @GET("api/profile/liked-pins")
    suspend fun getLikedPins(): List<PictureResponse>

    @Multipart
    @POST("api/pins/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("description") description: String
    ): PictureResponse

    @GET("api/auth/check-user")
    suspend fun checkUserExists(@Query("login") login: String): Response<Boolean>

    @POST("api/auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): RegisterResponse

    @Multipart
    @POST("api/profile/image")
    suspend fun uploadProfileImage(
        @Part image: MultipartBody.Part
    ): Response<ProfileResponse>

    @Multipart
    @POST("api/profile/avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): Response<String>
}