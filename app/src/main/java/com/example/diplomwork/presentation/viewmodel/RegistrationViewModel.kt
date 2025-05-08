package com.example.diplomwork.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.data.model.LoginRequest
import com.example.diplomwork.data.model.RegisterRequest
import com.example.diplomwork.data.model.UserExistsResponse
import com.example.diplomwork.data.repos.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    data class RegisterData(
        var username: String = "",
        val firstName: String = "",
        val email: String = "",
        val birthDate: String = "",
        val password: String = ""
    )

    private val _registerData = MutableStateFlow(RegisterData())
    val registerData: StateFlow<RegisterData> = _registerData

    private val _step = MutableStateFlow(0)
    val step: StateFlow<Int> = _step

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isUsernameExists = MutableStateFlow(false)
    val isUsernameExists: StateFlow<Boolean> = _isUsernameExists

    fun updateRegisterData(update: RegisterData.() -> RegisterData) {
        val currentData = _registerData.value
        val newData = currentData.update()

        _registerData.value = newData

        if (currentData.username != newData.username) {
            _isUsernameExists.value = false
        }

        _errorMessage.value = null
    }

    fun nextStep() {
        if (_step.value < 3) _step.value += 1
    }

    fun previousStep() {
        if (_step.value > 0) _step.value -= 1
    }

    suspend fun checkUsernameExists(): Boolean {
        _isLoading.value = true
        var result = _isUsernameExists.value
        try {
            result = authRepository.checkUsernameExists(_registerData.value.username).exists
            _isUsernameExists.value = result
        }
        catch(e: Exception){
            _errorMessage.value = "Ошибка: ${e.message}"
        }
        finally {
            _isLoading.value = false
        }
        return result
    }

    private fun validateRequiredFields(): Boolean {
        val f = _registerData.value
        return f.username.isNotBlank() && f.firstName.isNotBlank()
                && f.email.isNotBlank() && f.birthDate.isNotBlank()
                && f.password.isNotBlank()
    }

    fun register(onCompleteRegistration: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            if (!validateRequiredFields()) {
                _errorMessage.value = "Заполните все обязательные поля"
                return@launch
            }

            try {
                _errorMessage.value = null

                val registerValue = _registerData.value

                val registerResponse = authRepository.register(
                    RegisterRequest(
                        registerValue.username,
                        registerValue.email,
                        registerValue.password,
                        registerValue.firstName,
                        registerValue.birthDate
                    )
                )

                val loginResponse = authRepository.login(
                    LoginRequest(registerValue.username, registerValue.password)
                )

                sessionManager.authToken = loginResponse.token
                onCompleteRegistration()

            } catch (e: Exception) {
                _errorMessage.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun isCurrentStepValid(): Boolean {
        return when (_step.value) {
            0 -> {
                val username = registerData.value.username
                val firstName = registerData.value.firstName
                val usernameRegex = "^[a-zA-Z0-9_]{3,15}$".toRegex()

                username.matches(usernameRegex)
                        && firstName.isNotBlank()
            }

            1 -> {
                val password = registerData.value.password
                val passwordRegex = "^\\S{8,}$".toRegex()

                password.matches(passwordRegex)
            }

            2 -> {
                val email = registerData.value.email
                val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()

                email.isNotBlank()
                        && email.matches(emailRegex)
                        && !email.contains(" ")
            }

            3 -> true

            else -> false
        }
    }
}