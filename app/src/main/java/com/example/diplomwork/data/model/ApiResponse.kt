package com.example.diplomwork.data.model

// Общая обертка для ответов API
data class ApiResponse<T>(
    val status: String,
    val message: String,
    val data: T
)

data class ApiResponseWrapper<T>(
    val data: T,
    val links: List<Link>? = null,
    val meta: Meta? = null
)

data class Link(
    val rel: String,
    val href: String,
    val method: String
)

data class Meta(
    val timestamp: String
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