package com.example.diplomwork.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.data.repos.AuthRepository
import com.example.diplomwork.data.repos.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    data class FeedbackData(
        val whatLiked: String? = null,
        val whatDisliked: String? = null,
        val recommendations: String? = null,
    )

    private val _isLogout = MutableStateFlow<Result<String>?>(null)
    val isLogout: StateFlow<Result<String>?> = _isLogout

    private val _feedbackData = MutableStateFlow(FeedbackData())
    val feedbackData: StateFlow<FeedbackData> = _feedbackData

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting

    private val _deleteResult = MutableSharedFlow<String>()
    val deleteResult: SharedFlow<String> = _deleteResult

    fun logout() {
        viewModelScope.launch {
            try {
                val response = authRepository.logout()
                if (response.isSuccessful) {
                    val message = response.body()?.get("message") ?: "Выход выполнен"
                    _isLogout.value = Result.success(message)
                } else {
                    _isLogout.value = Result.failure(Exception("Ошибка выхода: ${response.code()}"))
                }
            } catch (e: Exception) {
                _isLogout.value = Result.failure(e)
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _isDeleting.value = true
            try {
                val response = userRepository.deleteAccount()
                if (response.isSuccessful) {
                    _deleteResult.emit("Аккаунт успешно удалён")

                } else {
                    _deleteResult.emit("Ошибка удаления аккаунта: ${response.code()}")
                }
            } catch (e: Exception) {
                _deleteResult.emit("Ошибка: ${e.localizedMessage ?: "Неизвестная ошибка"}")
            } finally {
                _isDeleting.value = false
            }
        }
    }

    fun updateWhatLiked(value: String) {
        _feedbackData.value = _feedbackData.value.copy(whatLiked = value)
    }

    fun updateWhatDisliked(value: String) {
        _feedbackData.value = _feedbackData.value.copy(whatDisliked = value)
    }

    fun updateRecommendations(value: String) {
        _feedbackData.value = _feedbackData.value.copy(recommendations = value)
    }

    fun sendFeedback() {
        Log.i(
            "Feedback",
            "Плюсы: ${_feedbackData.value.whatLiked}" +
                    "Минусы: ${_feedbackData.value.whatDisliked}" +
                    "Рекомендации: ${_feedbackData.value.recommendations}"
        )
    }

}