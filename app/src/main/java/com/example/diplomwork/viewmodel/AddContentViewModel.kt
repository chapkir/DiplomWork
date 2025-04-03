package com.example.diplomwork.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.network.repos.UploadRepository
import com.example.diplomwork.ui.screens.add_picture_screen.OpenGalleryForAddPicture
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class AddContentViewModel @Inject constructor(
    private val uploadRepository: UploadRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _showPreview = MutableStateFlow(false)
    val showPreview: StateFlow<Boolean> = _showPreview

    private val _isError = MutableStateFlow<String?>(null)
    val isError: StateFlow<String?> = _isError

    private val _showAddPhotoScreen = MutableStateFlow(false)
    val showAddPhotoScreen: StateFlow<Boolean> = _showAddPhotoScreen

    private val _showAddPostScreen = MutableStateFlow(false)
    val showAddPostScreen: StateFlow<Boolean> = _showAddPostScreen

    fun onImageSelected(uri: Uri) {
        _selectedImageUri.value = uri
        _showPreview.value = true
    }

    fun onDismissPreview() {
        _showPreview.value = false
    }

    fun onAddPhotoClicked() {
        // Показываем экран добавления фото
        _showAddPhotoScreen.value = true
        _showAddPostScreen.value = false
    }

    fun onAddPostClicked() {
        // Показываем экран добавления поста
        _showAddPostScreen.value = true
        _showAddPhotoScreen.value = false
    }

    fun onDismissAddScreens() {
        _showAddPhotoScreen.value = false
        _showAddPostScreen.value = false
    }

    fun uploadImage(description: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val file = selectedImageUri.value?.let { uri -> createTempFileFromUri(uri) }
                if (file != null) {
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                    val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())

                    val response = uploadRepository.uploadImage(body, descriptionBody)
                    if (response.isSuccessful) {
                        _showPreview.value = false
                        _isError.value = null
                    } else {
                        _isError.value = "Ошибка при загрузке изображения"
                    }
                } else {
                    _isError.value = "Ошибка при подготовке файла"
                }
            } catch (e: Exception) {
                _isError.value = "Ошибка при загрузке: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun createTempFileFromUri(uri: Uri): File? {
        return try {
            val (fileName, fileSize) = getFileDetails(uri)
            if (fileSize > 50 * 1024 * 1024) throw IllegalArgumentException("Файл слишком большой. Максимальный размер: 50MB")

            val tempFile = File(context.cacheDir, fileName).apply { createNewFile() }
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.copyTo(tempFile.outputStream())
            }

            if (tempFile.exists() && tempFile.length() > 0) {
                tempFile
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("AddPhotoDialog", "Ошибка при подготовке файла", e)
            null
        }
    }

    private fun getFileDetails(uri: Uri): Pair<String, Long> {
        val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
        } ?: "image_${System.currentTimeMillis()}.jpg"

        val fileSize = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            cursor.getLong(sizeIndex)
        } ?: -1

        return fileName to fileSize
    }
}
