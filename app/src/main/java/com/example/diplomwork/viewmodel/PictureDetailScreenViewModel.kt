package com.example.diplomwork.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.model.Comment
import com.example.diplomwork.model.CommentRequest
import com.example.diplomwork.network.repos.PictureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PictureDetailScreenViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pictureRepository: PictureRepository
) : ViewModel() {

    private val pictureId: Long = savedStateHandle.get<Long>("pictureId") ?: 0L

    private val _pictureDescription = MutableStateFlow("")
    val pictureDescription: StateFlow<String> = _pictureDescription

    private val _likesCount = MutableStateFlow(0)
    val likesCount: StateFlow<Int> = _likesCount

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _profileImageUrl = MutableStateFlow("")
    val profileImageUrl: StateFlow<String> = _profileImageUrl

    private val _pictureUsername = MutableStateFlow("")
    val pictureUsername: StateFlow<String> = _pictureUsername

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadPictureData()
    }

    private fun loadPictureData() {
        viewModelScope.launch {
            try {
                Log.d("PictureDetailViewModel", "Начинаем загрузку данных пина с ID: $pictureId")
                val picture = pictureRepository.getPicture(pictureId)

                // Логируем полученные данные для отладки
                Log.d("PictureDetailViewModel", "Загружен пин: ID=${picture.id}, " +
                        "URL=${picture.imageUrl}, " +
                        "Автор=${picture.username}, " +
                        "Лайки=${picture.likesCount}, " +
                        "Описание='${picture.description}'")

                _pictureUsername.value = picture.username
                _profileImageUrl.value = picture.userProfileImageUrl
                _pictureDescription.value = picture.description
                _likesCount.value = picture.likesCount
                _isLiked.value = picture.isLikedByCurrentUser

                _comments.value = pictureRepository.getComments(pictureId)
                Log.d("PictureDetailViewModel", "Загружено ${_comments.value.size} комментариев")
            } catch (e: Exception) {
                Log.e("PictureDetailViewModel", "Error loading data: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleLike() {
        viewModelScope.launch {
            val wasLiked = _isLiked.value
            _isLiked.value = !wasLiked
            _likesCount.value =
                if (!wasLiked) _likesCount.value + 1
                else (_likesCount.value - 1).coerceAtLeast(0)

            try {
                if (wasLiked) {
                    pictureRepository.unlikePicture(pictureId)
                } else {
                    pictureRepository.likePicture(pictureId)
                }
            } catch (e: Exception) {
                Log.e("PictureDetailViewModel", "Error updating like: ${e.message}")
                _isLiked.value = wasLiked
                _likesCount.value =
                    if (wasLiked) _likesCount.value + 1
                    else (_likesCount.value - 1).coerceAtLeast(0)
            }
        }
    }

    fun addComment(commentText: String) {
        viewModelScope.launch {
            if (commentText.isBlank()) return@launch

            try {
                val commentRequest = CommentRequest(text = commentText)
                pictureRepository.addComment(pictureId, commentRequest)
                _comments.value = pictureRepository.getComments(pictureId)
            } catch (e: Exception) {
                Log.e("PictureDetailViewModel", "Error adding comment: ${e.message}")
            }
        }
    }
}
