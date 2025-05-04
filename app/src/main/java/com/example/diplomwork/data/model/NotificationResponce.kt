package com.example.diplomwork.data.model

data class NotificationResponse(
    val id: Long,
    val type: String,
    val message: String,
    val senderId: Long,
    val senderUsername: String,
    val senderProfileImageUrl: String?,
    val pinId: Long?,
    val postId: Long?,
    val pinImageUrl: String?,
    val postImageUrl: String?,
    val createdAt: String,
    val read: Boolean
)