package com.example.diplomwork.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.diplomwork.data.model.LocationResponse
import com.example.diplomwork.data.model.SpotPicturesResponse
import com.example.diplomwork.data.model.SpotResponse
import com.example.diplomwork.data.repos.LocationRepository
import com.example.diplomwork.data.repos.SpotRepository
import com.example.diplomwork.domain.usecase.DeletePictureUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

//@HiltViewModel
//class PicturesViewModel @Inject constructor(
//    private val pictureRepository: PictureRepository
//) : ViewModel() {
//
//    private val _pictures = MutableStateFlow<List<PictureResponse>>(emptyList())
//    val pictures: StateFlow<List<PictureResponse>> = _pictures
//
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading: StateFlow<Boolean> = _isLoading
//
//    private val _error = MutableStateFlow<String?>(null)
//    val error: StateFlow<String?> = _error
//
//    private var currentPage = 0
//    private val pageSize = 20
//    private var isLastPage = false
//
//    init { loadPictures() }
//
//    private fun loadPictures(searchQuery: String = "", isRefreshing: Boolean = false) {
//        if (_isLoading.value || isLastPage) return
//
//        viewModelScope.launch {
//            _isLoading.value = true
//            _error.value = null
//
//            try {
//                val result = if (searchQuery.isNotEmpty()) {
//                    pictureRepository.searchSpots(searchQuery, page = if (isRefreshing) 0 else currentPage, size = pageSize)
//                } else {
//                    pictureRepository.getPictures()
//                }
//
//                val picturesWithRatio = result.map { pic ->
//                        pic.copy(aspectRatio = (pic.imageWidth ?: 1f) / (pic.imageHeight ?: 1f))
//                }
//
//                if (isRefreshing) {
//                    _pictures.value = picturesWithRatio
//                    currentPage = 1
//                } else {
//                    _pictures.value += picturesWithRatio
//                    currentPage++
//                }
//
//                isLastPage = result.size < pageSize
//            } catch (e: Exception) {
//                _error.value = "Ошибка загрузки: ${e.message}"
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//
//    fun refreshPictures(searchQuery: String = "") {
//        loadPictures(searchQuery, isRefreshing = true)
//    }
//}

@HiltViewModel
class SpotsViewModel @Inject constructor(
    private val spotRepository: SpotRepository,
    private val deletePictureUseCase: DeletePictureUseCase
) : ViewModel() {

    private val currentUsername = spotRepository.getCurrentUsername()

    private val _spots = MutableStateFlow<List<SpotResponse>>(emptyList())
    val spots: StateFlow<List<SpotResponse>> = _spots.asStateFlow()

    private val _imagesUrls = MutableStateFlow<Map<Long, SpotPicturesResponse>>(emptyMap())
    val imagesUrls: StateFlow<Map<Long, SpotPicturesResponse>> = _imagesUrls

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _deleteStatus = MutableSharedFlow<String>(replay = 0)
    val deleteStatus: SharedFlow<String> = _deleteStatus.asSharedFlow()


    val spotsPagingFlow: Flow<PagingData<SpotResponse>> =
        spotRepository.getSpotsPagingFlow()
            .map { pagingData ->
                pagingData.map { picture ->
                    picture.copy(
                        isCurrentUserOwner = picture.username == currentUsername
                    )
                }
            }
            .cachedIn(viewModelScope)

    fun loadMorePicturesForSpot(spotId: Long, firstImage: String) {
        viewModelScope.launch {
            try {
                val response = spotRepository.getSpotPictures(spotId)

                val additional = response.pictures.filterNotNull().filterNot { it == firstImage }

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

    fun deletePicture(pictureId: Long) {
        viewModelScope.launch {
            val result = deletePictureUseCase.delete(pictureId)

            val message = if (result.isSuccess) {
                "Удаление успешно"
            } else {
                result.exceptionOrNull()?.message ?: "Ошибка удаления"
            }

            _deleteStatus.emit(message)
        }
    }
}

