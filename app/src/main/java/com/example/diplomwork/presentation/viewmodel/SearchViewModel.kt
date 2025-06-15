package com.example.diplomwork.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.data.model.SpotPicturesResponse
import com.example.diplomwork.data.model.SpotResponse
import com.example.diplomwork.data.repos.SearchRepository
import com.example.diplomwork.domain.usecase.LoadSpotPicturesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val loadSpotPicturesUseCase: LoadSpotPicturesUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _imagesUrls = MutableStateFlow<Map<Long, SpotPicturesResponse>>(emptyMap())
    val imagesUrls: StateFlow<Map<Long, SpotPicturesResponse>> = _imagesUrls

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SpotResponse>>(emptyList())
    val searchResults: StateFlow<List<SpotResponse>> = _searchResults.asStateFlow()

    private val _noResults = MutableStateFlow(false)
    val noResults: StateFlow<Boolean> = _noResults.asStateFlow()

    private val _error = MutableSharedFlow<String?>()
    val error: SharedFlow<String?> = _error

    private val _isPaginating = MutableStateFlow(false)
    val isPaginating: StateFlow<Boolean> = _isPaginating

    private var currentPage = 0
    private var isLastPage = false

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun performSearch(reset: Boolean = false) {
        if (_isLoading.value || _isPaginating.value || (!reset && isLastPage)) return

        viewModelScope.launch {
            if (reset) {
                _isLoading.value = true
                currentPage = 0
                isLastPage = false
                _searchResults.value = emptyList()
                _noResults.value = false
            } else {
                _isPaginating.value = true
            }

            try {
                val page = searchRepository.searchSpots(
                    query = _searchQuery.value,
                    page = currentPage,
                    size = 10
                )

                val newContent = page.content
                _noResults.value = reset && newContent.isEmpty()

                _searchResults.value = if (reset) {
                    newContent
                } else {
                    _searchResults.value + newContent
                }

                isLastPage = page.last
                currentPage++
            } catch (e: Exception) {
                _error.emit("Ошибка сети: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
                _isPaginating.value = false
            }
        }
    }

    fun loadMorePicturesForSpot(spotId: Long, firstImage: String) {
        viewModelScope.launch {
            try {
                val additional = loadSpotPicturesUseCase(spotId, firstImage)

                _imagesUrls.update { currentMap ->
                    currentMap + (spotId to SpotPicturesResponse(additional))
                }

            } catch (e: Exception) {
                Log.e("ViewModel", "Ошибка загрузки картинок для $spotId: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
