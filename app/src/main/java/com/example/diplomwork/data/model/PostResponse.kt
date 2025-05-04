package com.example.diplomwork.data.model

data class PostResponse(
    val id: Long,
    val userAvatar: String?,
    val username: String,
    val userId: Long,
    val text: String,
    val imageUrl: String?,
    val likesCount: Int,
    val commentsCount: Int,
    val comments: List<CommentResponse>?,
    val isLikedByCurrentUser: Boolean,
    val isOwnPost: Boolean = false
)