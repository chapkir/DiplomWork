package com.example.diplomwork.data.model

data class ChangePasswordRequest (
    val oldPassword: String,
    val newPassword: String
)