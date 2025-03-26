package com.example.diplomwork.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.model.Comment
import com.example.diplomwork.model.CommentRequest
import com.example.diplomwork.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PictureDetailScreenViewModel(private val pictureId: Long) : ViewModel() {

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
                val picture = ApiClient.apiService.getPicture(pictureId)
                _pictureUsername.value = picture.username
                _profileImageUrl.value = picture.userProfileImageUrl
                _pictureDescription.value = picture.description
                _likesCount.value = picture.likesCount
                _isLiked.value = picture.isLikedByCurrentUser

                _comments.value = ApiClient.apiService.getComments(pictureId)
            } catch (e: Exception) {
                Log.e("PictureDetailViewModel", "Error loading data: ${e.message}")
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
                    ApiClient.apiService.unlikePicture(pictureId)
                } else {
                    ApiClient.apiService.likePicture(pictureId)
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
                ApiClient.apiService.addComment(pictureId, commentRequest)
                _comments.value = ApiClient.apiService.getComments(pictureId)
            } catch (e: Exception) {
                Log.e("PictureDetailViewModel", "Error adding comment: ${e.message}")
            }
        }
    }
}
