package com.example.diplomwork.presentation.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diplomwork.data.model.LocationRequest
import com.example.diplomwork.data.repos.LocationRepository
import com.example.diplomwork.data.repos.UploadRepository
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
class CreateSpotViewModel @Inject constructor(
    private val uploadRepository: UploadRepository,
    private val locationRepository: LocationRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _spotName: String = savedStateHandle.get<String>("spotName") ?: ""
    private val _spotAddress: String = savedStateHandle.get<String>("spotAddress") ?: ""
    private val _latitude: Double = savedStateHandle.get<Double>("latitude") ?: 0.0
    private val _longitude: Double = savedStateHandle.get<Double>("longitude") ?: 0.0

    data class CreateSpotData(
        val title: String = "",
        val description: String = "",
        val geo: String = "",
        val address: String = "",
        val rating: String = "1"
    )

    private val _createSpotData = MutableStateFlow(CreateSpotData(
        title = _spotName,
        geo = "$_latitude, $_longitude",
        address = _spotAddress
    ))
    val createSpotData: StateFlow<CreateSpotData> = _createSpotData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isError = MutableStateFlow<String?>(null)
    val isError: StateFlow<String?> = _isError

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun updateCreateContentData(update: CreateSpotData.() -> CreateSpotData) {
        val currentData = _createSpotData.value
        val newData = currentData.update()
        _createSpotData.value = newData
        _errorMessage.value = null
    }

    fun uploadContent(imageUris: List<Uri>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _isError.value = null
            try {
                val parts = imageUris.map { uri ->
                    val file = prepareFile(uri)
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("file", file.name, requestFile)
                }

                val titleBody = _createSpotData.value.title
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val descriptionBody = _createSpotData.value.description
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val ratingBody = _createSpotData.value.rating
                    .toRequestBody("text/plain".toMediaTypeOrNull())

                val response = uploadRepository.uploadSpot(
                    files = parts,
                    title = titleBody,
                    description = descriptionBody,
                    rating = ratingBody
                )

                if (response.isSuccessful) {
                    val pictureList = response.body()?.data ?: emptyList()
                    val pictureId = pictureList.firstOrNull()?.id ?: 0L

                    val locationRequest = LocationRequest(
                        pictureId = pictureId,
                        latitude = _latitude,
                        longitude = _longitude,
                        address = _spotAddress
                    )

                    val locationResponse = locationRepository.addLocation(locationRequest)

                    if (locationResponse.isSuccessful) {
                        onSuccess()
                    } else {
                        _isError.value = "Ошибка локации: ${locationResponse.code()}"
                    }
                } else {
                    _isError.value = "Ошибка загрузки: ${response.code()}"
                }
            } catch (e: Exception) {
                _isError.value = "Ошибка: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun prepareFile(uri: Uri): File {
        val (name, size) = getFileDetails(uri)
        require(size <= 50 * 1024 * 1024) { "Файл больше 50MB" }

        val tempFile = File.createTempFile("upload_", "_$name", context.cacheDir)

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Не удалось открыть файл по URI")

        inputStream.use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
        }

        require(tempFile.exists() && tempFile.length() > 0) { "Не удалось создать файл" }
        return tempFile
    }

    private fun getFileDetails(uri: Uri): Pair<String, Long> {
        var name = "image_${System.currentTimeMillis()}.jpg"
        var size = -1L

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIndex != -1) name = cursor.getString(nameIndex)
                if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
            }
        }

        return name to size
    }
}
