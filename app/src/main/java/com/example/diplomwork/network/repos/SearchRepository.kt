package com.example.diplomwork.network.repos

import com.example.diplomwork.model.ApiResponse
import com.example.diplomwork.model.PageResponse
import com.example.diplomwork.model.PictureResponse
import com.example.diplomwork.network.api.ApiService
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class SearchRepository @Inject constructor(
    private val apiService: ApiService
) {

    // Поиск картинок
    suspend fun searchPictures(query: String, page: Int, size: Int): ApiResponse<PageResponse<PictureResponse>> {
        return apiService.searchPictures(query, page, size)
    }
}