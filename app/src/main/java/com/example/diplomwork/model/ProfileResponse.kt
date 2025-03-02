package com.example.diplomwork.model

data class ProfileResponse(
    val username: String,
    val email: String,
    val pins: List<Pin>
)