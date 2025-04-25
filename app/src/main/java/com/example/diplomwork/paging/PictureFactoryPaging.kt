package com.example.diplomwork.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.diplomwork.model.PictureResponse
import com.example.diplomwork.network.api.ApiService

class PictureFactoryPaging(
    private val apiService: ApiService
) : PagingSource<Int, PictureResponse>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PictureResponse> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize

            // Запрос к серверу (без пагинации)
            val response = apiService.getPictures()
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

    override fun getRefreshKey(state: PagingState<Int, PictureResponse>): Int? {
        return null
    }
}