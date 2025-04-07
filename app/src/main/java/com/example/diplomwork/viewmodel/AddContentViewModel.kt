package com.example.diplomwork.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.network.repos.UploadRepository
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
import javax.inject.Inject

@HiltViewModel
class AddContentViewModel @Inject constructor(
    private val uploadRepository: UploadRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isError = MutableStateFlow<String?>(null)
    val isError: StateFlow<String?> = _isError

    fun uploadContent(type: String, imageUri: Uri?, description: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _isError.value = null
            try {
                val file = prepareFile(imageUri ?: throw Exception("URI отсутствует"))
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = when (type) {
                    "Picture" -> uploadRepository.uploadImage(body, descriptionBody)
                    "Post" -> uploadRepository.uploadPost(body, descriptionBody)
                    else -> throw IllegalArgumentException("Неизвестный тип: $type")
                }

                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    _isError.value = "Ошибка сервера: ${response.code()}"
                }
            } catch (e: Exception) {
                _isError.value = "Ошибка загрузки: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun prepareFile(uri: Uri): File {
        val (name, size) = getFileDetails(uri)
        require(size <= 50 * 1024 * 1024) { "Файл больше 50MB" }

        val tempFile = File(context.cacheDir, name).apply { createNewFile() }
        context.contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
        }

        require(tempFile.exists() && tempFile.length() > 0) { "Не удалось создать файл" }
        return tempFile
    }

    private fun getFileDetails(uri: Uri): Pair<String, Long> {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        val name = cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex != -1) it.getString(nameIndex) else null
        } ?: "image_${System.currentTimeMillis()}.jpg"

        val size = context.contentResolver.query(uri, null, null, null, null)?.use {
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            if (it.moveToFirst() && sizeIndex != -1) it.getLong(sizeIndex) else -1
        } ?: -1

        return name to size
    }
}
