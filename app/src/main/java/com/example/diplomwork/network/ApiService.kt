package com.example.diplomwork.network

import com.example.diplomwork.model.Pin
import com.example.diplomwork.model.LoginRequest
import com.example.diplomwork.model.LoginResponse
import com.example.diplomwork.model.ProfileResponse
import com.example.diplomwork.model.Comment
import com.example.diplomwork.model.CommentRequest
import com.example.diplomwork.model.CommentResponse
import com.example.diplomwork.model.PinResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("api/pins/all")
    suspend fun getPins(): List<PinResponse>

    @GET("api/pins/{id}")
    suspend fun getPin(@Path("id") id: Long): PinResponse

    @GET("api/pins/search")
    suspend fun searchPins(@Query("query") query: String): List<PinResponse>

    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @GET("api/profile")
    suspend fun getProfile(): ProfileResponse

    @POST("api/pins/{pinId}/likes")
    suspend fun likePin(@Path("pinId") pinId: Long): Response<Unit>

    @DELETE("api/pins/{pinId}/likes")
    suspend fun unlikePin(@Path("pinId") pinId: Long): Response<Unit>

    @GET("api/pins/{pinId}/comments")
    suspend fun getComments(@Path("pinId") pinId: Long): List<Comment>

    @POST("api/pins/{pinId}/comments")
    suspend fun addComment(
        @Path("pinId") pinId: Long,
        @Body comment: CommentRequest
    ): CommentResponse

    @GET("api/profile/liked-pins")
    suspend fun getLikedPins(): List<PinResponse>

    @Multipart
    @POST("api/pins/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("description") description: String
    ): PinResponse
}