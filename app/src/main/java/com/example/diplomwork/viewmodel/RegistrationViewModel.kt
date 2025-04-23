package com.example.diplomwork.viewmodel

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

    data class RegisterData(
        val username: String = "",
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

    fun updateRegisterData(update: RegisterData.() -> RegisterData) {
        _registerData.value = _registerData.value.update()
        _errorMessage.value = null
    }

    fun nextStep() {
        if (_step.value < 3) _step.value += 1
    }

    fun previousStep() {
        if (_step.value > 0) _step.value -= 1
    }

    private fun validateRequiredFields(): Boolean {
        val f = _registerData.value
        return f.username.isNotBlank() && f.firstName.isNotBlank()
                && f.email.isNotBlank() && f.birthDate.isNotBlank()
                && f.password.isNotBlank()
    }

    fun register(onCompleteRegistration: () -> Unit) {
        viewModelScope.launch {
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
            0 -> registerData.value.username.isNotBlank()
                    && !registerData.value.username.contains(" ")
                    && registerData.value.firstName.isNotBlank()

            1 -> registerData.value.password.isNotBlank()
                    && registerData.value.password.length >= 8
                    && !registerData.value.password.contains(" ")

            2 -> registerData.value.email.isNotBlank()
                    && registerData.value.email.contains("@")
                    && !registerData.value.email.contains(" ")

            3 -> true

            else -> false
        }
    }
}