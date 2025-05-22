package com.example.diplomwork.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.R
import com.example.diplomwork.data.model.SpotResponse
import com.example.diplomwork.data.repos.SpotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val spotRepository: SpotRepository
) : ViewModel() {

    data class CategoryCard(
        val title: String,
        val count: String,
        val imageRes: Int
    )

    data class UiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val spots: List<SpotResponse> = emptyList()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val categoryList = listOf(
        CategoryCard("Гастрономия", "110 мест", R.drawable.gastronomy_2),
        CategoryCard("Вечерние прогулки", "60 мест", R.drawable.evening),
        CategoryCard("Праздники", "186 мест", R.drawable.holidays),
        CategoryCard("Достопримечательности", "42 места", R.drawable.attractions)
    )

    fun loadSpotsByCategory(categoryName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching {
                spotRepository.getSpotsByCategory(categoryName)
            }.onSuccess { spots ->
                _uiState.update {
                    it.copy(spots = spots, isLoading = false)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = "Ошибка: ${error.message ?: "Неизвестная"}",
                        isLoading = false
                    )
                }
            }
        }
    }
}