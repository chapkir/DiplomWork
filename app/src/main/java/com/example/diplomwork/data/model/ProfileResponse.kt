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

    val bio: String,

    var profileImageUrl: String? = null,

    val pinsCount: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,

)