package com.example.diplomwork.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.diplomwork.data.api.SpotApi
import com.example.diplomwork.data.model.SpotResponse
import retrofit2.HttpException

class SpotPagingSource(
    private val api: SpotApi,
    private val pageSize: Int
) : PagingSource<String, SpotResponse>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, SpotResponse> {
        return try {
            val cursor = params.key
            val response = api.getSpots(cursor = cursor, size = pageSize)
            if (response.isSuccessful) {
                val data = response.body()?.data ?: return LoadResult.Page(emptyList(), null, null)
                val spots = data.content
                val nextCursor = spots.lastOrNull()?.id?.toString()?.takeIf { data.hasNext }

                LoadResult.Page(
                    data = spots,
                    prevKey = null,
                    nextKey = nextCursor
                )
            } else {
                LoadResult.Error(HttpException(response))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, SpotResponse>): String? {
        return null // можно улучшить, если появится поддержка cursor-смены
    }
}