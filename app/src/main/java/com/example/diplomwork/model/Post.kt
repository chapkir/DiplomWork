package com.example.diplomwork.model

data class Post(
    val id: Long,
    val userAvatar: Int,
    val username: String,
    val text: String,
    val imageUrl: Int?,
    val likesCount: Int,
    val comments: List<CommentResponse>?,
    val isLikedByCurrentUser: Boolean = false,
)