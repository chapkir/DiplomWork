package com.example.diplomwork.data.model

data class LocationRequest (
    val pictureId: Long,
    val latitude: Double,
    val longitude: Double,
    val address: String
)

data class LocationResponse (
    val id: Long,
    val pictureId: Long,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val nameplace: String,
    val createdAt: String
)