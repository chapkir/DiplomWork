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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
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

    private val _comments = MutableStateFlow<Map<Long, List<CommentResponse>>>(emptyMap())
    val comments: StateFlow<Map<Long, List<CommentResponse>>> = _comments

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
                loadAllComments(result)
            } catch (e: Exception) {
                _error.value = e.message ?: "Ошибка загрузки постов"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadAllComments(posts: List<PostResponse>) {
        viewModelScope.launch {
            val result = posts.map { post ->
                async {
                    val comments = runCatching {
                        commentRepository.getPostComments(post.id)
                    }.getOrDefault(emptyList())
                    post.id to comments
                }
            }.awaitAll().toMap()

            _comments.value = result
        }
    }

    fun toggleLike(postId: Long) {
        viewModelScope.launch {
            val post = _posts.value.find { it.id == postId } ?: return@launch
            val wasLiked = post.isLikedByCurrentUser
            val newLikesCount = if (!wasLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)

            updatePost(postId) {
                copy(
                    isLikedByCurrentUser = !wasLiked,
                    likesCount = newLikesCount
                )
            }

            try {
                val response = if (wasLiked) {
                    postRepository.unlikePost(postId)
                } else {
                    postRepository.likePost(postId)
                }

                if (!response.isSuccessful) throw HttpException(response)

            } catch (e: Exception) {
                // Откат
                updatePost(postId) { post }
                _error.value = e.message ?: "Ошибка при обновлении лайка"
                Log.e("PostsViewModel", "Ошибка при лайке: ${e.message}")
            }
        }
    }

    fun loadCommentsForPost(postId: Long) {
        viewModelScope.launch {
            try {
                val postComments = commentRepository.getPostComments(postId)
                _comments.update { it.toMutableMap().apply { put(postId, postComments) } }
            } catch (e: Exception) {
                Log.e("PostsViewModel", "Ошибка загрузки комментариев: ${e.message}")
            }
        }
    }

    fun addComment(postId: Long, commentText: String) {
        if (commentText.isBlank()) return
        viewModelScope.launch {
            try {
                commentRepository.addPostComment(postId, CommentRequest(text = commentText))
                val updatedComments = commentRepository.getPostComments(postId)
                _comments.update { it.toMutableMap().apply { put(postId, updatedComments) } }
            } catch (e: Exception) {
                Log.e("PostsViewModel", "Ошибка при добавлении комментария: ${e.message}")
            }
        }
    }

    fun selectPost(id: Long) {
        _selectedPostId.value = id
    }

    private fun updatePost(postId: Long, update: PostResponse.() -> PostResponse) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) post.update() else post
        }
    }
}
