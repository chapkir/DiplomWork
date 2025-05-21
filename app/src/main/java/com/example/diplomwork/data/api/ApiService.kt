package com.example.diplomwork.data.api

import com.example.diplomwork.data.model.ApiResponse
import com.example.diplomwork.data.model.ApiResponseWrapper
import com.example.diplomwork.data.model.ChangePasswordRequest
import com.example.diplomwork.data.model.CommentRequest
import com.example.diplomwork.data.model.CommentResponse
import com.example.diplomwork.data.model.CommentResponseWrapper
import com.example.diplomwork.data.model.CursorPageResponse
import com.example.diplomwork.data.model.EditProfileRequest
import com.example.diplomwork.data.model.FcmTokenRequest
import com.example.diplomwork.data.model.LocationRequest
import com.example.diplomwork.data.model.LocationResponse
import com.example.diplomwork.data.model.LoginRequest
import com.example.diplomwork.data.model.LoginResponse
import com.example.diplomwork.data.model.NotificationResponse
import com.example.diplomwork.data.model.PageResponse
import com.example.diplomwork.data.model.PostResponse
import com.example.diplomwork.data.model.ProfileResponse
import com.example.diplomwork.data.model.RegisterRequest
import com.example.diplomwork.data.model.RegisterResponse
import com.example.diplomwork.data.model.SpotDetailResponse
import com.example.diplomwork.data.model.SpotPicturesResponse
import com.example.diplomwork.data.model.SpotResponse
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

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: TokenRefreshRequest): TokenRefreshResponse

    @POST("api/auth/logout")
    suspend fun logout(): Response<Map<String, String>>

    @POST("api/auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): RegisterResponse

    @GET("api/users/exists/{username}")
    suspend fun checkUsernameExists(@Path("username") username: String): UserExistsResponse
}


interface ProfileApi {
    @GET("api/profile")
    suspend fun getOwnProfile(): ProfileResponse

    @GET("api/profile/{userId}")
    suspend fun getProfileById(@Path("userId") userId: Long?): ProfileResponse

    @GET("api/profile/spots")
    suspend fun getOwnProfilePictures(): List<SpotResponse>

    @GET("api/profile/{userId}/pictures")
    suspend fun getOtherProfilePictures(@Path("userId") userId: Long?): List<SpotResponse>

    @GET("api/profile/liked-pins")
    suspend fun getOwnLikedPictures(): List<SpotResponse>

    @GET("api/profile/likesPictures/{userId}")
    suspend fun getOtherLikedPictures(@Path("userId") userId: Long?): List<SpotResponse>

    @PUT("api/profile/edit")
    suspend fun editProfile(@Body editProfileRequest: EditProfileRequest)

    @Multipart
    @POST("api/profile/image")
    suspend fun uploadProfileImage(@Part file: MultipartBody.Part): Response<Map<String, String>>

    @GET("api/profile/posts")
    suspend fun getOwnProfilePosts(): List<PostResponse>
}


interface SpotApi {

    @GET("api/spots")
    suspend fun getSpots(
        @Query("cursor") cursor: String? = null,
        @Query("size") size: Int = 5,
        @Query("sortDirection") sortDirection: String = "desc"
    ): Response<ApiResponseWrapper<CursorPageResponse<SpotResponse>>>

    @GET("api/spots/all")
    suspend fun getAllSpots(): List<SpotResponse>

    @GET("api/spots/{spotId}/pictures")
    suspend fun getSpotPictures(@Path("spotId") spotId: Long): SpotPicturesResponse

    @GET("api/pins/{id}")
    suspend fun getSpotDetail(@Path("id") id: Long): SpotDetailResponse

    @DELETE("api/pins/{id}")
    suspend fun deleteSpot(@Path("id") id: Long): Response<Unit>

    @GET("api/search/pins")
    suspend fun searchSpots(
        @Query("query") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): ApiResponse<PageResponse<SpotResponse>>

    @Multipart
    @POST("api/pins/upload")
    suspend fun uploadSpot(
        @Part files: List<MultipartBody.Part>,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("rating") rating: RequestBody
    ): Response<ApiResponseWrapper<SpotResponse>>

    @POST("api/pins/{pictureId}/likes")
    suspend fun likeSpot(@Path("pictureId") pictureId: Long): Response<Unit>

    @DELETE("api/pins/{pictureId}/likes")
    suspend fun unlikeSpot(@Path("pictureId") pictureId: Long): Response<Unit>

    @GET("api/pins/{pictureId}/comments")
    suspend fun getSpotComments(@Path("pictureId") pictureId: Long): CommentResponseWrapper

    @POST("api/pins/{pictureId}/comments")
    suspend fun addSpotComment(
        @Path("pictureId") pictureId: Long,
        @Body comment: CommentRequest
    ): CommentResponse
}


interface FollowApi {
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

    @GET("api/follows/{followerId}/following/{followingId}")
    suspend fun checkFollowing(
        @Path("followerId") followerId: Long,
        @Path("followingId") followingId: Long
    ): Response<Boolean>
}

interface LocationApi {
    @POST("api/locations")
    suspend fun addLocation(@Body spot: LocationRequest): Response<Unit>

    @GET("api/locations/pictures/{pictureId}")
    suspend fun getSpotLocation(@Path("pictureId") pictureId: Long): LocationResponse
}


interface UserApi {
    @DELETE("api/users/me")
    suspend fun deleteAccount(): Response<Unit>

    @PUT("api/me/password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<Unit>
}


interface NotificationApi {
    @GET("api/notifications")
    suspend fun getNotifications(): List<NotificationResponse>

    @POST("api/fcm/token")
    suspend fun sendFcmToken(@Body request: FcmTokenRequest): Response<Unit>
}


interface PostApi {
    @GET("api/posts")
    suspend fun getPosts(): List<PostResponse>

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
}