package com.example.diplomwork.model

data class PictureResponse(
    val id: Long,
    val imageUrl: String,
    val description: String,
    val imageWidth: Float?,
    val imageHeight: Float?,
    val aspectRatio: Float?,
    val likesCount: Int,
    val comments: List<CommentResponse>?,
    val isLikedByCurrentUser: Boolean = false,
    val username: String,
    val userId: Long,
    val userProfileImageUrl: String?
)



