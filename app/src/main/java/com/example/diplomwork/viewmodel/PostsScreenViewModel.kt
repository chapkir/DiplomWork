package com.example.diplomwork.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.model.CommentRequest
import com.example.diplomwork.model.CommentResponse
import com.example.diplomwork.model.PostResponse
import com.example.diplomwork.network.repos.CommentRepository
import com.example.diplomwork.network.repos.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class PostsScreenViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {

    private val _posts = MutableStateFlow<List<PostResponse>>(emptyList())
    val posts: StateFlow<List<PostResponse>> = _posts

    private val _comments = MutableStateFlow<List<CommentResponse>>(emptyList())
    val comments: StateFlow<List<CommentResponse>> = _comments

    private val _selectedPostId = MutableStateFlow(0L)
    val selectedPostId: StateFlow<Long> = _selectedPostId

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadPosts()
    }

    private fun loadPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = postRepository.getPosts()
                _posts.value = result
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleLike(postId: Long) {
        viewModelScope.launch {
            // Найди индекс поста
            val index = _posts.value.indexOfFirst { it.id == postId }
            if (index == -1) return@launch

            val post = _posts.value[index]
            val wasLiked = post.isLikedByCurrentUser
            val newLikesCount = if (!wasLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)

            // 1. Обнови локальный список (оптимистично)
            val updatedPost = post.copy(
                isLikedByCurrentUser = !wasLiked,
                likesCount = newLikesCount
            )
            _posts.value = _posts.value.toMutableList().apply { set(index, updatedPost) }

            try {
                // 2. Отправь на сервер
                if (wasLiked) {
                    val response = postRepository.unlikePost(postId)
                    if (!response.isSuccessful) {
                        throw HttpException(response)
                    }
                } else {
                    val response = postRepository.likePost(postId)
                    if (!response.isSuccessful) {
                        throw HttpException(response)
                    }
                }
            } catch (e: Exception) {
                // 3. Откат в случае ошибки
                val revertedPost = post.copy(
                    isLikedByCurrentUser = wasLiked,
                    likesCount = post.likesCount
                )
                _posts.value = _posts.value.toMutableList().apply { set(index, revertedPost) }
                _error.value = e.message ?: "Ошибка при обновлении лайка"
                Log.e("PostsViewModel", "Ошибка при лайке: ${e.message}")
            }
        }
    }

    fun addComment(postId: Long, commentText: String) {
        viewModelScope.launch {
            if (commentText.isBlank()) return@launch

            try {
                val commentRequest = CommentRequest(text = commentText)
                commentRepository.addPostComment(postId, commentRequest)
                _comments.value = commentRepository.getPostComments(postId)
            } catch (e: Exception) {
                Log.e("PictureDetailViewModel", "Error adding comment: ${e.message}")
            }
        }
    }

    fun selectPost(id: Long) {
        _selectedPostId.value = id
    }

}