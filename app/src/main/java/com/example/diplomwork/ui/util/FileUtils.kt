package com.example.diplomwork.ui.util

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Преобразует Uri в MultipartBody.Part для загрузки файлов
 */
fun Uri.toMultipartBody(context: Context, paramName: String): MultipartBody.Part? {
    return try {
        val file = getFileFromUri(context, this)
        val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        MultipartBody.Part.createFormData(paramName, file.name, requestFile)
    } catch (e: Exception) {
        Log.e("FileUtils", "Ошибка при преобразовании Uri в MultipartBody.Part: ${e.message}")
        null
    }
}

/**
 * Получает File из Uri
 */
private fun getFileFromUri(context: Context, uri: Uri): File {
    // Сначала попробуем получить реальный путь к файлу
    val projection = arrayOf(MediaStore.Images.Media.DATA)

    context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val path = cursor.getString(columnIndex)
            if (path != null) {
                val file = File(path)
                if (file.exists()) {
                    return file
                }
            }
        }
    }

    // Если не получилось, создаем временный файл
    val fileName = getFileName(context, uri) ?: "image_${System.currentTimeMillis()}.jpg"
    val tempFile = File(context.cacheDir, fileName)

    // Копируем содержимое из Uri в файл
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        FileOutputStream(tempFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }

    return tempFile
}

/**
 * Получает имя файла из Uri
 */
private fun getFileName(context: Context, uri: Uri): String? {
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    return cursor.getString(nameIndex)
                }
            }
        }
    }

    // Если имя не найдено, получаем его из пути
    return uri.path?.substringAfterLast('/')
}