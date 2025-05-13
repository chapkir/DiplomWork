package com.example.diplomwork.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.data.model.CommentRequest
import com.example.diplomwork.data.model.CommentResponse
import com.example.diplomwork.data.model.PictureResponse
import com.example.diplomwork.data.repos.CommentRepository
import com.example.diplomwork.data.repos.PictureRepository
import com.example.diplomwork.domain.usecase.DeletePictureUseCase
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
    private val sessionManager: SessionManager,
    private val deletePictureUseCase: DeletePictureUseCase
) : ViewModel() {

    private val _pictureId: Long = savedStateHandle.get<Long>("pictureId") ?: 0L

    private val _uiState = MutableStateFlow(PictureDetailUiState())
    val uiState: StateFlow<PictureDetailUiState> = _uiState

    init {
        loadPictureData()
        loadCommentsForPicture()
    }

    private fun loadPictureData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val pictureResult = safeApiCall { pictureRepository.getPicture(_pictureId) }

            pictureResult.getOrNull()?.let { picture ->
                val currentUserUsername = sessionManager.username
                val isOwner = currentUserUsername == picture.username

                _uiState.value = _uiState.value.copy(
                    picture = picture,
                    pictureUsername = picture.username,
                    pictureUserId = picture.userId,
                    profileImageUrl = picture.userProfileImageUrl,
                    pictureTitle = picture.title,
                    pictureDescription = picture.description,
                    likesCount = picture.likesCount,
                    commentsCount = picture.commentsCount,
                    isLiked = picture.isLikedByCurrentUser,
                    isCurrentUserOwner = isOwner,
                    aspectRatio = picture.aspectRatio ?: 1f,
                    isLoading = false
                )
            }
        }
    }

    private fun loadCommentsForPicture() {
        viewModelScope.launch {
            try {
                val commentsResult = safeApiCall { commentRepository.getPictureComments(_pictureId) }
                _uiState.value = _uiState.value.copy(
                    comments = commentsResult.getOrNull() ?: emptyList(),
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
                safeApiCall { pictureRepository.unlikePicture(_pictureId) }
            } else {
                safeApiCall { pictureRepository.likePicture(_pictureId) }
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
                commentRepository.addPictureComment(_pictureId, commentRequest)
            }

            if (result.isSuccess) {
                val updatedComments = commentRepository.getPictureComments(_pictureId)
                _uiState.value = _uiState.value.copy(comments = updatedComments)
            } else {
                Log.e(
                    "PictureDetailViewModel",
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

data class PictureDetailUiState(
    val isLoading: Boolean = false,
    val picture: PictureResponse? = null,
    val pictureUsername: String = "",
    val pictureUserId: Long = 0L,
    val profileImageUrl: String? = null,
    val pictureTitle: String = "",
    val pictureDescription: String = "",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false,
    val comments: List<CommentResponse> = emptyList(),
    val deleteStatus: String = "",
    val isCurrentUserOwner: Boolean = false,
    val aspectRatio: Float = 1f,
)