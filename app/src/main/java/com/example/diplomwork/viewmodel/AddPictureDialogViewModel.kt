package com.example.diplomwork.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.network.ImageUploadService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val imageUploadService: ImageUploadService
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun uploadImage(
        uri: Uri,
        description: String,
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val file = createTempFileFromUri(context, uri)
                if (file != null) {
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                    val descriptionBody =
                        description.toRequestBody("text/plain".toMediaTypeOrNull())

                    val response = imageUploadService.uploadImage(body, descriptionBody)
                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        onError("Ошибка: ${response.message()}")
                    }
                } else {
                    onError("Ошибка при подготовке файла")
                }
            } catch (e: Exception) {
                onError("Ошибка загрузки: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}

private fun createTempFileFromUri(context: Context, uri: Uri): File? {
    return try {
        val fileName = getFileName(context, uri)
        val tempFile = File(context.cacheDir, fileName)
        tempFile.createNewFile()

        val fileSize = getFileSize(context, uri)
        if (fileSize > 50 * 1024 * 1024) {
            throw IllegalArgumentException("Файл слишком большой. Максимальный размер: 50MB")
        }

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val bufferedInputStream = inputStream.buffered(8192)
            FileOutputStream(tempFile).buffered(8192).use { outputStream ->
                val buffer = ByteArray(8192)
                var bytesRead: Int

                while (bufferedInputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                outputStream.flush()
            }
        }

        tempFile
    } catch (e: Exception) {
        Log.e("FileUtils", "Ошибка при подготовке файла: ${e.localizedMessage}")
        null
    }
}

private fun getFileName(context: Context, uri: Uri): String {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex != -1 && cursor.moveToFirst()) {
            cursor.getString(nameIndex)
        } else null
    } ?: "image_${System.currentTimeMillis()}.jpg"
}

private fun getFileSize(context: Context, uri: Uri): Long {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        cursor.moveToFirst()
        cursor.getLong(sizeIndex)
    } ?: -1
}