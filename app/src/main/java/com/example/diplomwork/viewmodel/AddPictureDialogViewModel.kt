package com.example.diplomwork.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composer.Companion.Empty
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.diplomwork.network.ImageUploadService
import com.example.diplomwork.network.repos.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _uploadStatus = MutableStateFlow<Result<Unit>>(Result.success(Unit))
    val uploadStatus: StateFlow<Result<Unit>> = _uploadStatus

    fun uploadImage(file: MultipartBody.Part, description: String) {
        viewModelScope.launch {
            imageRepository.uploadImage(file, description).collect { result ->
                _uploadStatus.value = result
            }
        }
    }
}