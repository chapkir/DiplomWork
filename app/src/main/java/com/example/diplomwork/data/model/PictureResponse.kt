package com.example.diplomwork.data.model

data class PictureResponse(

    val id: Long,
    val imageUrl: String,

    val description: String,
    val title: String,
    val rating: Double,

    val likesCount: Int,
    val commentsCount: Int,

    val userId: Long,
    val username: String,
    val isCurrentUserOwner: Boolean = false,
    val userProfileImageUrl: String?,
    val isLikedByCurrentUser: Boolean = false,

    val imageWidth: Float?,
    val imageHeight: Float?,
    val aspectRatio: Float?,

    val fullhdImageUrl: String,
    val thumbnailImageUrl: String,
)





