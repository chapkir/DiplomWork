package com.example.diplomwork.network

import com.example.diplomwork.model.Pin
import retrofit2.http.GET

interface ApiService {
    @GET("api/pins/all")
    suspend fun getPins(): List<Pin>
}