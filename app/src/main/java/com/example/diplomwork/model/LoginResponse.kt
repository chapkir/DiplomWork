package com.example.diplomwork.model

data class LoginResponse(
    val token: String,
    val refreshToken: String? = null,
    val tokenType: String = "Bearer"
)

data class UserExistsResponse(
    val exists: Boolean
)

data class TokenRefreshRequest(
    val refreshToken: String
)

data class TokenRefreshResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer"
)
