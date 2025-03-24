package com.example.diplomwork.model

data class ProfileResponse(
    val id: Long,
    val username: String,
    val email: String,
    val profileImageUrl: String? = null,
    val pins: List<PictureResponse>,
    val pinsCount: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val bio: String? = null
)