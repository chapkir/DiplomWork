package com.example.diplomwork.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.diplomwork.data.model.SpotResponse
import com.example.diplomwork.data.api.ApiService
import com.example.diplomwork.data.api.SpotApi

class PictureFactoryPaging(
    private val api: SpotApi
) : PagingSource<Int, SpotResponse>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SpotResponse> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize

            // Запрос к серверу (без пагинации)
            val response = api.getSpot()
            val allPictures = response.shuffled()

            val fromIndex = page * pageSize
            val toIndex = minOf(fromIndex + pageSize, allPictures.size)

            if (fromIndex >= allPictures.size) {
                return LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null
                )
            }

            val currentPage = allPictures.subList(fromIndex, toIndex)

            LoadResult.Page(
                data = currentPage,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (toIndex < allPictures.size) page + 1 else null
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, SpotResponse>): Int? {
        return null
    }
}