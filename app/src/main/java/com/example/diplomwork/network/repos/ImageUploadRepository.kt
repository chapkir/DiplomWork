package com.example.diplomwork.network.repos

import com.example.diplomwork.network.ImageUploadService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ImageRepository @Inject constructor(
    private val imageUploadService: ImageUploadService
) {

    suspend fun uploadImage(file: MultipartBody.Part, description: String): Flow<Result<Unit>> {
        return flow {
            try {
                //val response = imageUploadService.uploadImage(file, description)
                //if (response.isSuccessful) {
                //    emit(Result.success(Unit)) // Загружено успешно
                //} else {
               //     emit(Result.failure(Exception("Ошибка загрузки: ${response.message()}")))
                //}
            } catch (e: Exception) {
                emit(Result.failure(e)) // Обработка ошибок
            }
        }
    }
}