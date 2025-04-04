package com.example.diplomwork.model

data class Picture(
    val id: Long,
    val imageUrl: String,
    val description: String = "",
    val likesCount: Int = 0,
    val comments: List<CommentResponse> = emptyList(),
    val username: String? = null,
    val isLikedByCurrentUser: Boolean = false
)