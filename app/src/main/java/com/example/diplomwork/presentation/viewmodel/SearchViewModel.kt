package com.example.diplomwork.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.data.api.SpotApi
import com.example.diplomwork.data.model.SpotResponse
import com.example.diplomwork.data.repos.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SpotResponse>>(emptyList())
    val searchResults: StateFlow<List<SpotResponse>> = _searchResults.asStateFlow()

    private val _error = MutableSharedFlow<String?>()
    val error: SharedFlow<String?> = _error

    private var currentPage = 0
    private var isLastPage = false

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun performSearch(reset: Boolean = false) {
        if (_isLoading.value || isLastPage) return

        viewModelScope.launch {
            _isLoading.value = true

            if (reset) {
                currentPage = 0
                isLastPage = false
                _searchResults.value = emptyList()
            }

            try {
                val response = searchRepository.searchPictures(
                    query = _searchQuery.value,
                    page = currentPage,
                    size = 20
                )

                if (response.status == "success") {
                    val page = response.data
                    val newContent = page.content

                    _searchResults.value = if (reset) {
                        newContent
                    } else {
                        _searchResults.value + newContent
                    }

                    isLastPage = page.last
                    currentPage++
                } else {
                    _error.emit(response.message ?: "Ошибка сервера")
                }
            } catch (e: Exception) {
                _error.emit("Ошибка сети: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
