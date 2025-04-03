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
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class AddPictureDialogViewModel @Inject constructor(
    private val uploadRepository: UploadRepository,
    @ApplicationContext private val context: Context // добавим @ApplicationContext
) : ViewModel() {

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _showPreview = MutableStateFlow(false)
    val showPreview: StateFlow<Boolean> = _showPreview

    private val _isError = MutableStateFlow<String?>(null)
    val isError: StateFlow<String?> = _isError

    fun onImageSelected(uri: Uri) {
        _selectedImageUri.value = uri
        _showPreview.value = true
    }

    fun onDismissPreview() {
        _showPreview.value = false
    }

    fun uploadImage(description: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val file = createTempFileFromUri(selectedImageUri.value!!)
                if (file != null) {
                    val requestFile =
                        file.asRequestBody("image/*".toMediaTypeOrNull()) // уточнили MIME-тип
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                    val descriptionBody =
                        description.toRequestBody("text/plain".toMediaTypeOrNull())

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

    private fun createTempFileFromUri(uri: Uri): File? {
        return try {
            // Получаем имя файла
            val fileName = getFileName(uri)

            // Создаем временный файл
            val tempFile = File(context.cacheDir, fileName)
            tempFile.createNewFile()

            // Проверяем размер файла перед началом копирования
            val fileSize = getFileSize(uri)
            if (fileSize > 50 * 1024 * 1024) { // 50MB лимит
                throw IllegalArgumentException("Файл слишком большой. Максимальный размер: 50MB")
            }

            // Копируем данные с использованием буфера
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bufferedInputStream = inputStream.buffered(8192)
                FileOutputStream(tempFile).buffered(8192).use { outputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytesRead = 0L

                    while (bufferedInputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead

                        // Логируем прогресс
                        if (fileSize > 0) {
                            val progress = (totalBytesRead.toFloat() / fileSize * 100).toInt()
                            Log.d("AddPhotoDialog", "Прогресс подготовки файла: $progress%")
                        }
                    }
                    outputStream.flush()
                }
            }

            // Проверяем, что файл был создан успешно
            if (!tempFile.exists() || tempFile.length() == 0L) {
                throw IOException("Не удалось создать файл")
            }

            Log.d(
                "AddPhotoDialog",
                "Файл создан успешно: ${tempFile.absolutePath}, размер: ${tempFile.length()} байт"
            )
            tempFile
        } catch (e: Exception) {
            Log.e("AddPhotoDialog", "Ошибка при подготовке файла", e)
            null
        }
    }

    /**
     * Получает имя файла из URI
     */
    private fun getFileName(uri: Uri): String {
        // Пытаемся получить имя файла из метаданных
        val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                cursor.getString(nameIndex)
            } else null
        }

        // Если не получилось, генерируем уникальное имя
        return fileName ?: "image_${System.currentTimeMillis()}.jpg"
    }

    private fun getFileSize(uri: Uri): Long {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            cursor.getLong(sizeIndex)
        } ?: -1
    }
}