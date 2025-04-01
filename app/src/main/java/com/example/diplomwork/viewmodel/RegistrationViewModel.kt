package com.example.diplomwork.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.model.LoginRequest
import com.example.diplomwork.model.RegisterRequest
import com.example.diplomwork.network.repos.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _step = MutableStateFlow(0)
    val step: StateFlow<Int> = _step

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun onUsernameChanged(newUsername: String) {
        _username.value = newUsername
    }

    fun onEmailChanged(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChanged(newPassword: String) {
        _password.value = newPassword
    }

    fun onNextStep() {
        if (_step.value < 2) {
            _step.value++
        }
    }

    fun onPreviousStep() {
        if (_step.value > 0) {
            _step.value--
        }
    }

    fun registerUser(context: Context, onCompleteRegistration: () -> Unit) {
        viewModelScope.launch {
            if (!_isLoading.value) {
                _isLoading.value = true
                try {
                    val registerResponse = authRepository.register(
                        RegisterRequest(username.value, email.value, password.value)
                    )

                    val loginResponse = authRepository.login(
                        LoginRequest(username.value, password.value)
                    )

                    sessionManager.saveAuthToken(loginResponse.token)

                    Toast.makeText(context, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                    onCompleteRegistration()

                } catch (e: Exception) {
                    _errorMessage.value = "Ошибка: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }
}