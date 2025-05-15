package com.example.diplomwork.data.repos

import com.example.diplomwork.data.api.ApiService
import com.example.diplomwork.data.model.LocationRequest
import com.example.diplomwork.data.model.LocationResponse
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun addLocation(spot: LocationRequest): Response<Unit> {
        return apiService.addLocation(spot)
    }

    suspend fun getSpotLocation(spotId: Long): LocationResponse {
        return apiService.getSpotLocation(spotId)
    }
}