package com.example.diplomwork.data.model

data class CommentRequest(
    val text: String
)

data class CommentPageData(
    val content: List<CommentResponse>,
    val pageNo: Int,
    val pageSize: Int,
    val totalElements: Int,
    val totalPages: Int,
    val last: Boolean
)

data class CommentResponse(
    val id: Long,
    val text: String,
    val username: String,
    val createdAt: String,
    val userProfileImageUrl: String?,
    val userId: Long
)