package com.example.diplomwork.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.model.LoginRequest
import com.example.diplomwork.network.repos.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // Состояния для логина и пароля
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> get() = _username

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> get() = _password

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> get() = _loginError

    // Логика авторизации
    fun login() {
        if (_username.value.isBlank() || _password.value.isBlank()) {
            _loginError.value = "Логин и пароль не могут быть пустыми"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = authRepository.login(LoginRequest(_username.value, _password.value))
                sessionManager.saveAuthData(response.token, response.refreshToken)
                sessionManager.username = _username.value
                _loginError.value = null
            } catch (e: Exception) {
                _loginError.value = "Ошибка авторизации: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Обработчики изменений для логина и пароля
    fun onUsernameChange(newUsername: String) {
        _username.value = newUsername
        _loginError.value = null
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        _loginError.value = null
    }
}