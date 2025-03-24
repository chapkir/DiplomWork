package com.example.diplomwork.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Утилитный класс для работы с изображениями
 */
object ImageUtils {
    private const val TAG = "ImageUtils"
    private const val COMPRESS_QUALITY = 80

    /**
     * Исправляет URL Яндекс.Диска, добавляя параметр disposition=inline если его нет
     * и декодирует HTML-закодированные символы
     */
    fun fixYandexDiskUrl(url: String): String {
        if (url.isEmpty()) {
            return url
        }

        // Сначала заменяем закодированные амперсанды на обычные
        val decodedUrl = url.replace("&amp;", "&")

        // Если параметр disposition уже есть, просто возвращаем декодированный URL
        if (decodedUrl.contains("disposition=")) {
            return decodedUrl
        }

        // Проверяем, является ли URL ссылкой на Яндекс.Диск
        if (decodedUrl.contains("disk.yandex.ru") ||
            decodedUrl.contains("downloader.disk.yandex.ru") ||
            decodedUrl.contains("preview.disk.yandex.ru")) {

            return if (decodedUrl.contains("?")) {
                "$decodedUrl&disposition=inline"
            } else {
                "$decodedUrl?disposition=inline"
            }
        }

        return decodedUrl
    }

    /**
     * Создает временный файл для изображения
     */
    fun createImageFile(context: Context, prefix: String = "img"): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "${prefix}_${timeStamp}"
        return File.createTempFile(
            imageFileName,
            ".jpg",
            context.cacheDir
        )
    }

    /**
     * Копирует изображение из Uri в файл с оптимизацией
     */
    fun copyUriToFile(context: Context, uri: Uri, maxWidth: Int = 1024, maxHeight: Int = 1024): File? {
        return try {
            val tempFile = createImageFile(context)

            // Сначала получаем размеры изображения для определения параметров сжатия
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }

            // Определяем коэффициент сжатия
            var sampleSize = 1
            if (options.outHeight > maxHeight || options.outWidth > maxWidth) {
                val heightRatio = Math.round(options.outHeight.toFloat() / maxHeight.toFloat())
                val widthRatio = Math.round(options.outWidth.toFloat() / maxWidth.toFloat())
                sampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
            }

            // Загружаем изображение с применением сжатия
            val loadOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }

            var bitmap: Bitmap? = null
            context.contentResolver.openInputStream(uri)?.use { input ->
                bitmap = BitmapFactory.decodeStream(input, null, loadOptions)
            }

            // Сохраняем сжатое изображение в файл
            bitmap?.let {
                FileOutputStream(tempFile).use { out ->
                    it.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, out)
                }
                bitmap?.recycle()
                Log.d(TAG, "Изображение успешно сжато и сохранено: ${tempFile.absolutePath}")
                tempFile
            } ?: run {
                Log.e(TAG, "Не удалось декодировать изображение")
                null
            }
        } catch (e: IOException) {
            Log.e(TAG, "Ошибка при копировании изображения", e)
            null
        }
    }

    /**
     * Получает имя файла из Uri
     */
    fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndex("_display_name")
                    if (columnIndex != -1) {
                        result = cursor.getString(columnIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1 && cut != null) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "unknown_${System.currentTimeMillis()}.jpg"
    }

    /**
     * Сжимает существующий файл изображения
     */
    fun compressImageFile(file: File, quality: Int = COMPRESS_QUALITY): Boolean {
        return try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            bitmap.recycle()
            Log.d(TAG, "Изображение успешно сжато: ${file.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при сжатии изображения", e)
            false
        }
    }
}