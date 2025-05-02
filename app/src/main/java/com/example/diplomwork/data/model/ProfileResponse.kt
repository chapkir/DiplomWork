package com.example.diplomwork.data.model

data class EditProfileRequest(
    val firstName: String? = null,
    val city: String? = null,
    val gender: String? = null,
    val email: String? = null,
    val bio: String? = null
)

data class ProfileResponse(
    val id: Long,
    val username: String,
    var firstName: String,
    val city: String,
    val gender: String,
    val email: String,
    var profileImageUrl: String? = null,
    val pins: List<PictureResponse>,
    val posts: List<PostResponse>,
    val pinsCount: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val bio: String,
    val birthDate: String
)