package com.example.diplomwork.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.model.PictureResponse
import com.example.diplomwork.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContentGridViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _pictures = MutableStateFlow<List<PictureResponse>>(emptyList())
    val pictures: StateFlow<List<PictureResponse>> = _pictures

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadPictures(searchQuery: String = "") {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = if (searchQuery.isNotEmpty()) {
                    apiService.searchPictures(searchQuery).data.content
                } else {
                    apiService.getPictures()
                }
                _pictures.value = response
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки данных: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}