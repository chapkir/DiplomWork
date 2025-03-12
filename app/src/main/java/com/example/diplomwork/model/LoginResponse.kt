package com.example.diplomwork.model

data class LoginResponse(
    val token: String
)

data class UserExistsResponse(
    val exists: Boolean
)
