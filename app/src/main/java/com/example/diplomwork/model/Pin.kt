package com.example.diplomwork.model

data class Pin(
    val id: Long,
    val imageUrl: String,
    val description: String = "",
    val likesCount: Int = 0,
    val comments: List<Comment> = emptyList(),
    val username: String? = null,
    val isLikedByCurrentUser: Boolean = false
)