package com.example.diplomwork.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.model.PictureResponse
import com.example.diplomwork.network.ApiService
import com.example.diplomwork.network.repos.PictureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val pictureRepository: PictureRepository
) : ViewModel() {

    private val _pictures = MutableStateFlow<List<PictureResponse>>(emptyList())
    val pictures: StateFlow<List<PictureResponse>> = _pictures

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var currentPage = 0
    private val pageSize = 20
    private var isLastPage = false

    init {
        loadPictures()
    }

    private fun loadPictures(searchQuery: String = "", isRefreshing: Boolean = false) {
        if (_isLoading.value || isLastPage) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = if (searchQuery.isNotEmpty()) {
                    // Для поиска картинок
                    pictureRepository.searchPictures(searchQuery, page = if (isRefreshing) 0 else currentPage, size = pageSize)
                } else {
                    // Для получения всех картинок
                    pictureRepository.getPictures()
                }

                if (isRefreshing) {
                    _pictures.value = result
                    currentPage = 1
                } else {
                    _pictures.value += result
                    currentPage++
                }

                isLastPage = result.size < pageSize
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshPictures(searchQuery: String = "") {
        loadPictures(searchQuery, isRefreshing = true)
    }
}