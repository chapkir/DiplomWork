package com.example.diplomwork.data.model

data class PictureResponse(
    val id: Long,
    val imageUrl: String,
    val description: String,
    val title: String,
    val imageWidth: Float?,
    val imageHeight: Float?,
    val aspectRatio: Float?,
    val likesCount: Int,
    val commentsCount: Int,
    val comments: List<CommentResponse>?,
    val isLikedByCurrentUser: Boolean = false,
    val username: String,
    val userId: Long,
    val userProfileImageUrl: String?,
    val isCurrentUserOwner: Boolean = false
)



