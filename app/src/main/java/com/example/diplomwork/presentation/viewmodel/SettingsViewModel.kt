package com.example.diplomwork.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.data.repos.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting

    private val _deleteResult = MutableSharedFlow<String>()
    val deleteResult: SharedFlow<String> = _deleteResult

    fun deleteAccount() {
        viewModelScope.launch {
            _isDeleting.value = true
            try {
                val response = authRepository.deleteAccount()
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
}