package com.example.diplomwork.data.repos

import com.example.diplomwork.data.api.SpotApi
import com.example.diplomwork.data.model.PageResponse
import com.example.diplomwork.data.model.SpotResponse
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SearchRepository @Inject constructor(
    private val api: SpotApi
) {
    suspend fun searchSpots(
        query: String,
        page: Int,
        size: Int
    ): PageResponse<SpotResponse> {
        return api.searchSpots(query, page, size)
    }
}