package com.example.diplomwork.data.model

data class SpotResponse(

    val id: Long,

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

    val aspectRatio: Float?,

    val thumbnailImageUrl: String,
    val picturesCount: Int,

    val latitude: Double?,
    val longitude: Double?,
    val namePlace: String?,

    val createdAt: String

)





