package com.example.diplomwork.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.model.CommentRequest
import com.example.diplomwork.model.CommentResponse
import com.example.diplomwork.model.PictureResponse
import com.example.diplomwork.network.repos.CommentRepository
import com.example.diplomwork.network.repos.PictureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PictureDetailScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pictureRepository: PictureRepository,
    private val commentRepository: CommentRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _pictureId: Long = savedStateHandle.get<Long>("pictureId") ?: 0L

    private val _uiState = MutableStateFlow(PictureDetailUiState())
    val uiState: StateFlow<PictureDetailUiState> = _uiState

    init {
        loadPictureData()
    }

    private fun loadPictureData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val pictureResult = safeApiCall { pictureRepository.getPicture(_pictureId) }
            val commentsResult = safeApiCall { commentRepository.getPictureComments(_pictureId) }

            pictureResult.getOrNull()?.let { picture ->
                val currentUserUsername = sessionManager.username
                val isOwner = currentUserUsername == picture.username
                val aspectRatio = (picture.imageWidth ?: 1f) / (picture.imageHeight ?: 1f)

                _uiState.value = _uiState.value.copy(
                    picture = picture,
                    pictureUsername = picture.username,
                    pictureUserId = picture.userId,
                    profileImageUrl = picture.userProfileImageUrl,
                    pictureDescription = picture.description,
                    likesCount = picture.likesCount,
                    isLiked = picture.isLikedByCurrentUser,
                    isCurrentUserOwner = isOwner,
                    aspectRatio = aspectRatio
                )
            }

            _uiState.value = _uiState.value.copy(
                comments = commentsResult.getOrNull() ?: emptyList(),
                isLoading = false
            )
        }
    }

    fun deletePicture() {
        viewModelScope.launch {
            val result = safeApiCall { pictureRepository.deletePicture(_pictureId) }

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(deleteStatus = "Удаление успешно")
            } else {
                _uiState.value = _uiState.value.copy(deleteStatus = "Ошибка удаления")
            }
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
                safeApiCall { pictureRepository.unlikePicture(_pictureId) }
            } else {
                safeApiCall { pictureRepository.likePicture(_pictureId) }
            }

            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isLiked = wasLiked,
                    likesCount = (_uiState.value.likesCount + if (wasLiked) 1 else -1).coerceAtLeast(0)
                )
            }
        }
    }

    fun addComment(commentText: String) {
        viewModelScope.launch {
            if (commentText.isBlank()) return@launch

            val result = safeApiCall {
                val commentRequest = CommentRequest(text = commentText)
                commentRepository.addPictureComment(_pictureId, commentRequest)
            }

            if (result.isSuccess) {
                val updatedComments = commentRepository.getPictureComments(_pictureId)
                _uiState.value = _uiState.value.copy(comments = updatedComments)
            } else {
                Log.e("PictureDetailViewModel", "Error adding comment: ${result.exceptionOrNull()?.message}")
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

data class PictureDetailUiState(
    val isLoading: Boolean = false,
    val picture: PictureResponse? = null,
    val pictureUsername: String = "",
    val pictureUserId: Long = 0L,
    val profileImageUrl: String? = null,
    val pictureDescription: String = "",
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val comments: List<CommentResponse> = emptyList(),
    val deleteStatus: String = "",
    val isCurrentUserOwner: Boolean = false,
    val aspectRatio: Float = 1f,
)