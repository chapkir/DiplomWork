package com.example.diplomwork.data.model

data class SpotDetailResponse (
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

    val picturesCount: Int,

    val latitude: Double?,
    val longitude: Double?,
    val namePlace: String?,

    val fullhdImages: List<String>,
)