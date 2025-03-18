package com.example.diplomwork.model

data class PictureResponse(
    val id: Long,
    val imageUrl: String,
    val description: String,
    val likesCount: Int,
    val comments: List<CommentResponse>?,
    val isLikedByCurrentUser: Boolean = false
)



