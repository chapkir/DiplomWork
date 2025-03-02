package com.example.diplomwork.model

data class Comment(
    val id: Long,
    val text: String,
    val username: String,
    val createdAt: String
)

data class CommentRequest(
    val text: String
)

data class CommentResponse(
    val id: Long,
    val text: String,
    val username: String,
    val createdAt: String
)