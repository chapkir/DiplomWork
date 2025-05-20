package com.example.diplomwork.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.data.model.LoginRequest
import com.example.diplomwork.data.repos.AuthRepository
import com.example.diplomwork.data.repos.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    data class ChangePasswordData(
        val oldPassword: String = "",
        val newPassword: String = "",
        val confirmPassword: String = ""
    )

    private val _changePasswordData = MutableStateFlow(ChangePasswordData())
    val changePasswordData: StateFlow<ChangePasswordData> = _changePasswordData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isNewPasswordStrong = MutableStateFlow(false)
    val isNewPasswordStrong: StateFlow<Boolean> = _isNewPasswordStrong

    private val _isPasswordsMatch = MutableStateFlow(true)
    val isPasswordsMatch: StateFlow<Boolean> = _isPasswordsMatch


    fun updateChangePasswordData(update: ChangePasswordData.() -> ChangePasswordData) {
        val currentData = _changePasswordData.value
        val newData = currentData.update()

        _changePasswordData.value = newData

        _isPasswordsMatch.value =
            when{
                changePasswordData.value.newPassword == "" -> true
                newData.newPassword == changePasswordData.value.confirmPassword -> true
                changePasswordData.value.confirmPassword == "" -> true
                else -> false
            }

        _isNewPasswordStrong.value = isPasswordStrong(newData.newPassword)

        _errorMessage.value = null
    }

    fun changePassword(onComplete: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true

            val changeData = _changePasswordData.value

            if (!isCurrentStepValid()) {
                _errorMessage.value = "Проверьте введённые данные"
                _isLoading.value = false
                return@launch
            }

            try {
                val username = userRepository.getOwnUsername()
                val loginResult = authRepository.login(
                    LoginRequest(username, changeData.oldPassword)
                )

                if (loginResult.token.isEmpty()) {
                    _errorMessage.value = "Неверный старый пароль"
                    _isLoading.value = false
                    return@launch
                }

                // Меняем пароль
                userRepository.changePassword(
                    oldPassword = changeData.oldPassword,
                    newPassword = changeData.newPassword
                )

                _errorMessage.value = null
                onComplete()

            } catch (e: Exception) {
                _errorMessage.value = "Ошибка изменения пароля: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun isCurrentStepValid(): Boolean {
        val data = _changePasswordData.value

        return when {
            data.oldPassword.isBlank() -> false
            !isNewPasswordStrong.value -> false
            else -> true
        }
    }

    private fun isPasswordStrong(password: String): Boolean {
        val lengthCriteria = password.length >= 8
        //val digitCriteria = password.any { it.isDigit() }
        //val letterCriteria = password.any { it.isLetter() }
        //val specialCharCriteria = password.any { "!@#$%^&*()-_+=<>?".contains(it) }

        return lengthCriteria //&& digitCriteria && letterCriteria && specialCharCriteria
    }
}