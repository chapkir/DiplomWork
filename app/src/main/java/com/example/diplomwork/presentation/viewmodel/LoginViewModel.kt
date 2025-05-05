package com.example.diplomwork.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.data.model.LoginRequest
import com.example.diplomwork.data.repos.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError

    private val _loginSuccess = MutableStateFlow<Boolean?>(null)
    val loginSuccess: StateFlow<Boolean?> = _loginSuccess

    fun login() {
        if (_username.value.isBlank() || _password.value.isBlank()) {
            _loginError.value = "Заполните все поля"
            _loginSuccess.value = false
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _loginError.value = null
            _loginSuccess.value = null

            try {
                val response = authRepository.login(
                    LoginRequest(_username.value, _password.value)
                )

                sessionManager.saveAuthData(response.token, response.refreshToken)
                sessionManager.username = _username.value

                _loginSuccess.value = true
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is HttpException -> when (e.code()) {
                        401 -> "Неверный логин или пароль"
                        500 -> "Неверный логин или пароль"
                        else -> "Ошибка ${e.code()}: ${e.message()}"
                    }

                    is IOException -> "Проблема с интернетом. Проверьте соединение"

                    else -> "Неизвестная ошибка: ${e.localizedMessage}"
                }

                _loginError.value = errorMessage
                _loginSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onUsernameChange(newUsername: String) {
        _username.value = newUsername
        _loginError.value = null
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        _loginError.value = null
    }
}