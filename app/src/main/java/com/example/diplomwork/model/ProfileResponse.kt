package com.example.diplomwork.model

data class ProfileResponse(
    val id: Long,
    val username: String,
    val email: String,
    val pins: List<PinResponse>
)