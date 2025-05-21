package com.example.diplomwork.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.data.model.ProfileResponse
import com.example.diplomwork.data.model.SpotPicturesResponse
import com.example.diplomwork.data.model.SpotResponse
import com.example.diplomwork.data.repos.FollowRepository
import com.example.diplomwork.data.repos.ProfileRepository
import com.example.diplomwork.data.repos.SpotRepository
import com.example.diplomwork.util.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val profileRepository: ProfileRepository,
    private val followRepository: FollowRepository,
    private val spotRepository: SpotRepository,
) : ViewModel() {

    private val _userId: Long = savedStateHandle["userId"] ?: 0L
    private val ownUserId = profileRepository.getOwnUserId()

    private val _profileData = MutableStateFlow<ProfileResponse?>(null)
    val profileData: StateFlow<ProfileResponse?> = _profileData

    private val _imagesUrls = MutableStateFlow<Map<Long, SpotPicturesResponse>>(emptyMap())
    val imagesUrls: StateFlow<Map<Long, SpotPicturesResponse>> = _imagesUrls

    private val _followersCount = MutableStateFlow(0)
    val followersCount: StateFlow<Int> = _followersCount

    private val _profilePictures = MutableStateFlow<List<SpotResponse>>(emptyList())
    val profilePictures: StateFlow<List<SpotResponse>> = _profilePictures

    private val _followState = MutableStateFlow<FollowState>(FollowState.Idle)
    val followState: StateFlow<FollowState> = _followState.asStateFlow()

    private val _likedPictures = MutableStateFlow<List<SpotResponse>>(emptyList())
    val likedPictures: StateFlow<List<SpotResponse>> = _likedPictures

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isLoadingPictures = MutableStateFlow(true)
    val isLoadingPictures: StateFlow<Boolean> = _isLoadingPictures

    private val _isLoadingLiked = MutableStateFlow(true)
    val isLoadingLiked: StateFlow<Boolean> = _isLoadingLiked

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    private val _error = MutableStateFlow(Errors())
    val error: StateFlow<Errors> = _error

    private val _avatarUpdateCounter = MutableStateFlow(0)
    val avatarUpdateCounter: StateFlow<Int> = _avatarUpdateCounter

    private val _isOwnProfile = MutableStateFlow(true)
    val isOwnProfile: StateFlow<Boolean> = _isOwnProfile

    private val _isSubscribed = MutableStateFlow(false)


    init {
        loadProfile()
    }

    private fun loadProfile() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                if (_userId == ownUserId || _userId == 0L) {
                    _profileData.value = profileRepository.getOwnProfile()
                    _isOwnProfile.value = true
                } else {
                    val profile = profileRepository.getProfileById(_userId)
                    _profileData.value = profile
                    _isOwnProfile.value = false
                    _followersCount.value = profile.followersCount
                    checkIfSubscribed(_userId)
                }
            } catch (e: Exception) {
                _error.value = _error.value.copy(errorLoadProfile = "Ошибка при загрузке профиля")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadProfilePictures() {
        if (_profilePictures.value.isNotEmpty()) return

        viewModelScope.launch {
            _isLoadingPictures.value = true
            try {

                val pictures = if (_userId == ownUserId || _userId == 0L) {
                    profileRepository.getOwnProfilePictures()
                } else {
                    profileRepository.getOtherProfilePictures(_userId)
                }

                _profilePictures.value = pictures

            } catch (e: Exception) {
                _error.value = _error.value.copy(errorLoadSpots = "Ошибка при загрузке мест")
            } finally {
                _isLoadingPictures.value = false
            }
        }
    }

    fun loadMorePicturesForSpot(spotId: Long, firstImage: String) {
        viewModelScope.launch {
            try {
                val response = spotRepository.getSpotPictures(spotId)

                val additional = response.pictures.filterNotNull().filterNot { it == firstImage }

                _imagesUrls.update { currentMap ->
                    currentMap + (spotId to SpotPicturesResponse(additional))
                }

            } catch (e: Exception) {
                Log.e("ViewModel", "Ошибка загрузки картинок для $spotId: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadLikedPictures() {
        if (_likedPictures.value.isNotEmpty()) return
        _isLoadingLiked.value = true
        viewModelScope.launch {
            try {
                val result = if (_userId == ownUserId || _userId == 0L) {
                    profileRepository.getOwnLikedPictures()
                } else {
                    profileRepository.getOtherLikedPictures(_userId)
                }

                if (result.isSuccess) {
                    _likedPictures.value = result.getOrNull() ?: emptyList()

                } else {
                    _error.value = _error.value.copy(
                        errorLoadLikes = "Ошибка при загрузке лайкнутых мест"
                    )
                }
            } catch (e: Exception) {
                _error.value =
                    _error.value.copy(errorLoadLikes = "Ошибка при загрузке лайкнутых мест")
            } finally {
                _isLoadingLiked.value = false
            }
        }
    }

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
                        _error.value = _error.value.copy(
                            errorUploadAvatar = "Ошибка при загрузке аватара: $errorMessage"
                        )
                    }
                } else {
                    _error.value = _error.value.copy(
                        errorUploadAvatar = "Не удалось подготовить изображение"
                    )
                }
            } catch (e: Exception) {
                _error.value = _error.value.copy(
                    errorUploadAvatar = "Ошибка при загрузке аватара"
                )
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun checkAuth(onNavigateToLogin: () -> Unit) {
        viewModelScope.launch {
            if (!profileRepository.isLoggedIn()) {
                onNavigateToLogin()
            }
        }
    }

    private fun checkIfSubscribed(followingId: Long? = 0L) {
        viewModelScope.launch {
            _followState.value = FollowState.Loading
            try {
                val response = followingId?.let { followRepository.checkFollowing(ownUserId, it) }
                if (response != null) {
                    if (response.isSuccessful) {
                        _isSubscribed.value = response.body() ?: false
                        _followState.value = FollowState.Success(isSubscribed = _isSubscribed.value)
                    } else {
                        _followState.value =
                            FollowState.Error("Ошибка при проверке подписки: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                _followState.value = FollowState.Error("Ошибка сети: ${e.localizedMessage}")
            }
        }
    }

    fun subscribe(followingId: Long) {
        if (ownUserId == 0L) {
            _followState.value = FollowState.Error("ID текущего пользователя не найден")
            return
        }

        viewModelScope.launch {
            _followState.value = FollowState.Loading
            try {
                val response = followRepository.subscribe(ownUserId, followingId)
                if (response.isSuccessful) {
                    _isSubscribed.value = true
                    _followState.value = FollowState.Success(isSubscribed = true)
                    incrementFollowers()
                } else {
                    _followState.value = FollowState.Error("Ошибка подписки: ${response.code()}")
                }
            } catch (e: Exception) {
                _followState.value = FollowState.Error("Ошибка сети: ${e.localizedMessage}")
            }
        }
    }

    fun unsubscribe(followingId: Long) {
        if (ownUserId == 0L) {
            _followState.value = FollowState.Error("ID текущего пользователя не найден")
            return
        }

        viewModelScope.launch {
            _followState.value = FollowState.Loading
            try {
                val response = followRepository.unsubscribe(ownUserId, followingId)
                if (response.isSuccessful) {
                    _isSubscribed.value = false
                    _followState.value = FollowState.Success(isSubscribed = false)
                    decrementFollowers()
                } else {
                    _followState.value = FollowState.Error("Ошибка отписки: ${response.code()}")
                }
            } catch (e: Exception) {
                _followState.value = FollowState.Error("Ошибка сети: ${e.localizedMessage}")
            }
        }
    }

    fun refreshPictures() {
        _profilePictures.value = emptyList()
        loadProfilePictures()
    }

    fun refreshLikesPictures() {
        _likedPictures.value = emptyList()
        loadLikedPictures()
    }

    private fun incrementFollowers() {
        _followersCount.update { it + 1 }
    }

    private fun decrementFollowers() {
        _followersCount.update { (it - 1).coerceAtLeast(0) }
    }

}

sealed class FollowState {
    data object Idle : FollowState()
    data object Loading : FollowState()
    data class Success(val isSubscribed: Boolean) : FollowState()
    data class Error(val message: String) : FollowState()
}

data class Errors(
    val errorLoadProfile: String? = null,
    val errorLoadSpots: String? = null,
    val errorLoadLikes: String? = null,
    val errorUploadAvatar: String? = null
)