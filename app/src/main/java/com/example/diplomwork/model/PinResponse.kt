package com.example.diplomwork.model

data class PinResponse(
    val id: Long,
    val imageUrl: String,
    val description: String,
    val likesCount: Int,
    val comments: List<CommentResponse>?,
    val isLikedByCurrentUser: Boolean = false
)