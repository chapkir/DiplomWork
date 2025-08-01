package com.example.diplomwork.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.data.model.CommentRequest
import com.example.diplomwork.data.model.CommentResponse
import com.example.diplomwork.data.model.SpotDetailResponse
import com.example.diplomwork.data.repos.SpotRepository
import com.example.diplomwork.domain.usecase.DeletePictureUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpotDetailScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val spotRepository: SpotRepository,
    private val deletePictureUseCase: DeletePictureUseCase
) : ViewModel() {

    private val _pictureId: Long = savedStateHandle.get<Long>("pictureId") ?: 0L

    private val _uiState = MutableStateFlow(SpotDetailUiState())
    val uiState: StateFlow<SpotDetailUiState> = _uiState

    init {
        loadSpotData()
        loadCommentsForPicture()
    }

    private fun loadSpotData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val pictureResult = safeApiCall { spotRepository.getSpotDetail(_pictureId) }

            pictureResult.getOrNull()?.let { picture ->
                val currentUserUsername = spotRepository.getCurrentUsername()
                val isOwner = currentUserUsername == picture.username

                _uiState.value = _uiState.value.copy(
                    picture = picture,
                    pictureUsername = picture.username,
                    pictureUserId = picture.userId,
                    rating = picture.rating,
                    profileImageUrl = picture.userProfileImageUrl,
                    pictureTitle = picture.title,
                    placeName = picture.namePlace ?: "",
                    latitude = picture.latitude ?: 0.0,
                    longitude = picture.longitude ?: 0.0,
                    pictureDescription = picture.description,
                    likesCount = picture.likesCount,
                    commentsCount = picture.commentsCount,
                    isLiked = picture.isLikedByCurrentUser,
                    isCurrentUserOwner = isOwner,
                    picturesCount = picture.picturesCount,
                    fullhdImages = picture.fullhdImages,
                    isLoading = false
                )
            }
        }
    }

    private fun loadCommentsForPicture() {
        viewModelScope.launch {
            try {
                val commentsResult = safeApiCall { spotRepository.getSpotComments(_pictureId) }
                _uiState.value = _uiState.value.copy(
                    comments = commentsResult.getOrNull()?.content ?: emptyList(),
                )
            } catch (e: Exception) {
                Log.e("PictureViewModel", "Ошибка загрузки комментариев: ${e.message}")
            }
        }
    }

    fun deletePicture() {
        viewModelScope.launch {
            val result = deletePictureUseCase.delete(_pictureId)

            _uiState.value = _uiState.value.copy(
                deleteStatus =
                    if (result.isSuccess) "Удаление успешно"
                    else result.exceptionOrNull()?.message ?: "Ошибка удаления"
            )
        }
    }

    fun toggleLike() {
        viewModelScope.launch {
            val wasLiked = _uiState.value.isLiked
            _uiState.value = _uiState.value.copy(
                isLiked = !wasLiked,
                likesCount = (_uiState.value.likesCount + if (!wasLiked) 1 else -1).coerceAtLeast(0)
            )

            val result = if (wasLiked) {
                safeApiCall { spotRepository.unlikePicture(_pictureId) }
            } else {
                safeApiCall { spotRepository.likePicture(_pictureId) }
            }

            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isLiked = wasLiked,
                    likesCount = (_uiState.value.likesCount + if (wasLiked) 1 else -1).coerceAtLeast(
                        0
                    )
                )
            }
        }
    }

    fun addComment(commentText: String) {
        viewModelScope.launch {
            if (commentText.isBlank()) return@launch

            val result = safeApiCall {
                val commentRequest = CommentRequest(text = commentText)
                spotRepository.addSpotComment(_pictureId, commentRequest)
            }

            if (result.isSuccess) {
                val updatedComments = spotRepository.getSpotComments(_pictureId)
                _uiState.value = _uiState.value.copy(
                    comments = updatedComments.content,
                    commentsCount = updatedComments.totalPages
                )
            } else {
                Log.e(
                    "SpotDetailViewModel",
                    "ErrorColor adding comment: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    private inline fun <T> safeApiCall(call: () -> T): Result<T> {
        return try {
            Result.success(call())
        } catch (e: Exception) {
            Log.e("PictureDetailViewModel", "API Call failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}

data class SpotDetailUiState(
    val isLoading: Boolean = false,
    val picture: SpotDetailResponse? = null,
    val pictureUsername: String = "",
    val rating: Double = 1.0,
    val pictureUserId: Long = 0L,
    val profileImageUrl: String? = null,
    val pictureTitle: String = "",
    val placeName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val pictureDescription: String = "",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false,
    val comments: List<CommentResponse> = emptyList(),
    val deleteStatus: String = "",
    val isCurrentUserOwner: Boolean = false,
    val picturesCount: Int = 1,
    val fullhdImages: List<String> = emptyList()
)