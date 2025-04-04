package com.example.diplomwork.model

data class PostResponse(
    val id: Long,
    val userAvatar: String?,
    val username: String,
    val text: String,
    val imageUrl: String?,
    val likesCount: Int,
    val comments: List<CommentResponse>?,
    val isLikedByCurrentUser: Boolean = false,
)