package com.example.diplomwork.data.model

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String,
    val birthDate: String,
)

data class RegisterResponse(
    val username: String,
    val email: String
)