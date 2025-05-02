package com.example.diplomwork.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.data.model.EditProfileRequest
import com.example.diplomwork.data.repos.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    data class EditProfileData(
        val firstName: String? = null,
        val email: String? = null,
        val gender: String? = null,
        val bio: String? = null,
        val city: String? = null
    )

    private val _editProfileData = MutableStateFlow(EditProfileData())
    val editProfileData: StateFlow<EditProfileData> = _editProfileData

    private val _isProfileSaved = MutableStateFlow(false)
    val isProfileSaved: StateFlow<Boolean> = _isProfileSaved

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    fun updateProfileData(update: EditProfileData.() -> EditProfileData) {
        _editProfileData.value = _editProfileData.value.update()
    }

    fun saveProfile() {
        viewModelScope.launch {
            try {
                _isProfileSaved.value = false
                _successMessage.value = null
                val editProfileValue = _editProfileData.value
                _isLoading.value = true
                profileRepository.editProfile(
                    EditProfileRequest(
                        editProfileValue.firstName,
                        editProfileValue.city,
                        editProfileValue.gender,
                        editProfileValue.email,
                        editProfileValue.bio,
                    )
                )
                _isProfileSaved.value = true
                _successMessage.value = "Профиль обновлён"
            } catch (e: Exception) {
                _successMessage.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetSavedFlag() {
        _isProfileSaved.value = false
    }
}