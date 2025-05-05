package com.example.diplomwork.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.data.repos.PictureRepository
import com.example.diplomwork.domain.usecase.DeletePictureUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
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
//                    pictureRepository.searchPictures(searchQuery, page = if (isRefreshing) 0 else currentPage, size = pageSize)
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
class PicturesViewModel @Inject constructor(
    private val pictureRepository: PictureRepository,
    private val sessionManager: SessionManager,
    private val deletePictureUseCase: DeletePictureUseCase
) : ViewModel() {

    private val currentUsername = sessionManager.username

    val picturesPagingFlow = pictureRepository.getPagingPictures()
        .map { pagingData ->
            pagingData.map { picture ->
                picture.copy(
                    isCurrentUserOwner = picture.username == currentUsername
                )
            }
        }
        .cachedIn(viewModelScope)

    private val _deleteStatus = MutableSharedFlow<String>(replay = 0)
    val deleteStatus: SharedFlow<String> = _deleteStatus.asSharedFlow()

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