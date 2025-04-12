package com.example.diplomwork.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.model.PictureResponse
import com.example.diplomwork.model.ProfileResponse
import com.example.diplomwork.network.repos.ProfileRepository
import com.example.diplomwork.util.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val profileRepository: ProfileRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _userId: Long? = savedStateHandle["userId"]
    private val _username: String = savedStateHandle["username"] ?: ""

    private val _profileData = MutableStateFlow<ProfileResponse?>(null)
    val profileData: StateFlow<ProfileResponse?> = _profileData

    private val _likedPictures = MutableStateFlow<List<PictureResponse>>(emptyList())
    val likedPictures: StateFlow<List<PictureResponse>> = _likedPictures

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _avatarUpdateCounter = MutableStateFlow(0)
    val avatarUpdateCounter: StateFlow<Int> = _avatarUpdateCounter

    private val _isOwnProfile = MutableStateFlow(false)
    val isOwnProfile: StateFlow<Boolean> = _isOwnProfile


    init {
        if(_username == sessionManager.username) loadProfile()
        else loadProfile(userId = _userId)
    }

    private fun loadProfile(userId: Long? = null) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                if (userId != null) {
                    _profileData.value = profileRepository.getProfileById(userId)
                } else {
                    _profileData.value = profileRepository.getOwnProfile()
                    _isOwnProfile.value = true
                }
                Log.d("ProfileViewModel", "Загружен профиль: ${_profileData.value?.username}")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Ошибка при загрузке профиля: ${e.message}")
                _error.value = "Ошибка при загрузке профиля"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Загружаем лайкнутые пины
    fun loadLikedPictures() {
        viewModelScope.launch {
            try {
                val result = profileRepository.getLikedPictures()

                if (result.isSuccess) {
                    _likedPictures.value = result.getOrNull() ?: emptyList()
                    Log.d(
                        "ProfileViewModel",
                        "Загружено ${_likedPictures.value.size} лайкнутых пинов"
                    )
                } else {
                    _error.value = "Ошибка при загрузке лайкнутых пинов"
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Ошибка при загрузке лайкнутых пинов: ${e.message}")
                _error.value = "Ошибка при загрузке лайкнутых пинов"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Загрузка аватара на сервер
    fun uploadAvatarToServer(uri: Uri, context: Context) {
        _isUploading.value = true
        viewModelScope.launch {
            try {
                val imageFile = ImageUtils.copyUriToFile(context, uri)

                if (imageFile != null) {
                    val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                    val body =
                        MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

                    val response = profileRepository.uploadProfileImage(body)

                    if (response.isSuccessful) {
                        val updatedProfile = response.body()
                        _profileData.value?.profileImageUrl = updatedProfile?.get("profileImageUrl")
                        _avatarUpdateCounter.value++
                        loadProfile()  // Загружаем обновленный профиль
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Неизвестная ошибка"
                        _error.value = "Ошибка при загрузке аватара: $errorMessage"
                    }
                } else {
                    _error.value = "Не удалось подготовить изображение"
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Ошибка при загрузке аватара: ${e.message}")
                _error.value = "Ошибка при загрузке аватара"
            } finally {
                _isUploading.value = false
            }
        }
    }

    // Проверка авторизации
    fun checkAuth(onNavigateToLogin: () -> Unit) {
        viewModelScope.launch {
            try {
                if (!sessionManager.isLoggedIn()) {
                    onNavigateToLogin()
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Ошибка проверки сессии: ${e.message}")
            }
        }
    }
}