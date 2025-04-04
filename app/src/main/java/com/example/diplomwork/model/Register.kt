package com.example.diplomwork.model

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val username: String,
    val email: String
)