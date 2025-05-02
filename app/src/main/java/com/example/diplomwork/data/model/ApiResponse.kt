package com.example.diplomwork.data.model

// Общая обертка для ответов API
data class ApiResponse<T>(
    val status: String,
    val message: String,
    val data: T
)

// Класс для пагинированных ответов
data class PageResponse<T>(
    val content: List<T>,
    val totalPages: Int,
    val totalElements: Long,
    val size: Int,
    val number: Int,
    val numberOfElements: Int,
    val first: Boolean,
    val last: Boolean
)