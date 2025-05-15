package com.example.diplomwork.data.model

data class LocationRequest (
    val pictureId: Long,
    val latitude: Double,
    val longitude: Double,
    val placeName: String
)

data class LocationResponse (
    val id: Long,
    val pictureId: Long,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val placeName: String,
    val createdAt: String
)