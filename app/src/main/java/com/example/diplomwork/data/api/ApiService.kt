package com.example.diplomwork.data.api

import com.example.diplomwork.data.model.ApiResponse
import com.example.diplomwork.data.model.CommentRequest
import com.example.diplomwork.data.model.CommentResponse
import com.example.diplomwork.data.model.EditProfileRequest
import com.example.diplomwork.data.model.LoginRequest
import com.example.diplomwork.data.model.LoginResponse
import com.example.diplomwork.data.model.NotificationResponse
import com.example.diplomwork.data.model.PageResponse
import com.example.diplomwork.data.model.PictureResponse
import com.example.diplomwork.data.model.PostResponse
import com.example.diplomwork.data.model.ProfileResponse
import com.example.diplomwork.data.model.RegisterRequest
import com.example.diplomwork.data.model.RegisterResponse
import com.example.diplomwork.data.model.TokenRefreshRequest
import com.example.diplomwork.data.model.TokenRefreshResponse
import com.example.diplomwork.data.model.UserExistsResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface ApiService {
    @GET("api/pins/all")
    suspend fun getPictures(): List<PictureResponse>

    @GET("api/posts")
    suspend fun getPosts(): List<PostResponse>

    @GET("api/pins/{id}")
    suspend fun getPicture(@Path("id") id: Long): PictureResponse

    @DELETE("api/pins/{id}")
    suspend fun deletePicture(@Path("id") id: Long): Response<Unit>

    @GET("api/search/pins")
    suspend fun searchPictures(
        @Query("query") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResponse<PictureResponse>>

    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: TokenRefreshRequest): TokenRefreshResponse

    @POST("api/auth/logout")
    suspend fun logout(): Response<Map<String, String>>

    @GET("api/profile")
    suspend fun getOwnProfile(): ProfileResponse

    @GET("api/profile/pictures")
    suspend fun getOwnProfilePictures(): List<PictureResponse>

    @GET("api/profile/posts")
    suspend fun getOwnProfilePosts(): List<PostResponse>

    @GET("api/profile/{userId}")
    suspend fun getProfileById(@Path("userId") userId: Long?): ProfileResponse

    @PUT("api/profile/edit")
    suspend fun editProfile(@Body editProfileRequest: EditProfileRequest)

    @POST("api/auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): RegisterResponse

    @POST("api/pins/{pictureId}/likes")
    suspend fun likePicture(@Path("pictureId") pictureId: Long): Response<Unit>

    @DELETE("api/pins/{pictureId}/likes")
    suspend fun unlikePicture(@Path("pictureId") pictureId: Long): Response<Unit>

    @GET("api/pins/{pictureId}/comments")
    suspend fun getPictureComments(@Path("pictureId") pictureId: Long): ApiResponse<List<CommentResponse>>

    @POST("api/pins/{pictureId}/comments")
    suspend fun addPictureComment(
        @Path("pictureId") pictureId: Long,
        @Body comment: CommentRequest
    ): CommentResponse

    @GET("api/profile/liked-pins")
    suspend fun getLikedPictures(): List<PictureResponse>

    @Multipart
    @POST("api/pins/upload")
    suspend fun uploadImage(
        @Part files: List<MultipartBody.Part>,
        @Part("description") description: RequestBody,
        @Part("title") title: RequestBody
    ): Response<PictureResponse>

    @Multipart
    @POST("api/posts/with-image")
    suspend fun uploadPost(
        @Part file: MultipartBody.Part,
        @Part("text") text: RequestBody
    ): Response<PostResponse>

    @DELETE("api/posts/{postId}")
    suspend fun deletePost(@Path("postId") postId: Long): Response<Unit>

    @POST("api/posts/{postId}/like")
    suspend fun likePost(@Path("postId") postId: Long): Response<Unit>

    @DELETE("api/posts/{postId}/like")
    suspend fun unlikePost(@Path("postId") postId: Long): Response<Unit>

    @GET("api/posts/{postId}/comments")
    suspend fun getPostComments(@Path("postId") postId: Long): List<CommentResponse>

    @POST("api/posts/{postId}/comments")
    suspend fun addPostComment(
        @Path("postId") postId: Long,
        @Body comment: CommentRequest
    ): CommentResponse

    @GET("api/users/exists/{username}")
    suspend fun checkUsernameExists(@Path("username") username: String): UserExistsResponse

    @Multipart
    @POST("api/profile/image")
    suspend fun uploadProfileImage(
        @Part file: MultipartBody.Part
    ): Response<Map<String, String>>

    @POST("api/follows/{followerId}/following/{followingId}")
    suspend fun subscribe(
        @Path("followerId") followerId: Long,
        @Path("followingId") followingId: Long
    ): Response<Unit>

    @DELETE("api/follows/{followerId}/following/{followingId}")
    suspend fun unsubscribe(
        @Path("followerId") followerId: Long,
        @Path("followingId") followingId: Long
    ): Response<Unit>

    @GET("api/notifications")
    suspend fun getNotifications(): List<NotificationResponse>

}