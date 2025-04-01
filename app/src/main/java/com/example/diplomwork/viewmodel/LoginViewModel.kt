package com.example.diplomwork.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.model.LoginRequest
import com.example.diplomwork.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: ApiService,  // Инжектируем API
    private val sessionManager: SessionManager  // Инжектируем сессию
) : ViewModel() {

    // Состояния для username, password, isLoading
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // SharedFlow для сообщений об ошибках или успешных действиях
    private val _loginEvent = MutableSharedFlow<LoginEvent>()
    val loginEvent: SharedFlow<LoginEvent> = _loginEvent

    // Логика авторизации
    fun login() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.login(LoginRequest(username.value, password.value))
                sessionManager.saveAuthData(response.token, response.refreshToken)
                sessionManager.saveUsername(username.value)
                _loginEvent.emit(LoginEvent.Success)
            } catch (e: Exception) {
                _loginEvent.emit(LoginEvent.Error(e.message ?: "Неизвестная ошибка"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Обновление username и password
    fun updateUsername(newUsername: String) {
        _username.value = newUsername
    }

    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }
}

// События, которые будут отправляться в SharedFlow
sealed class LoginEvent {
    object Success : LoginEvent()
    data class Error(val message: String) : LoginEvent()
}