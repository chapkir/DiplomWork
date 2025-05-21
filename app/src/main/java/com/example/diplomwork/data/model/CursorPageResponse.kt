package com.example.diplomwork.data.model

data class CursorPageResponse<T>(
    val content: List<T>,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
    val pageSize: Int,
    val totalElements: Int,
    val nextCursor: String?
)
