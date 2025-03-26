package com.example.diplomwork.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import coil.request.CachePolicy
import coil.size.Scale
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Утилита для работы с изображениями
 */
object ImageUtils {
    private const val TAG = "ImageUtils"

    /**
     * Копирует содержимое Uri в файл
     */
    fun copyUriToFile(context: Context, uri: Uri): File? {
        return try {
            // Создаем временный файл
            val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)

            // Копируем содержимое Uri в файл
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            Log.d(TAG, "Файл успешно создан: ${tempFile.absolutePath}")
            tempFile
        } catch (e: IOException) {
            Log.e(TAG, "Ошибка при копировании файла: ${e.message}")
            null
        }
    }

    /**
     * Создает MD5 хеш для URL, используется для имен файлов кэша
     */
    fun hashUrl(url: String): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(url.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Проверяет, есть ли изображение в кэше
     */
    fun isImageCached(context: Context, url: String): Boolean {
        val hash = hashUrl(url)
        val cacheFile = File(context.cacheDir, "images/$hash")
        return cacheFile.exists() && cacheFile.length() > 0
    }

    /**
     * Получает изображение из кэша или загружает, если его нет
     */
    suspend fun getImage(context: Context, url: String, maxRetries: Int = 3): Bitmap? = withContext(Dispatchers.IO) {
        val hash = hashUrl(url)
        val cacheDir = File(context.cacheDir, "images").apply { mkdirs() }
        val cacheFile = File(cacheDir, hash)

        // Проверяем кэш
        if (cacheFile.exists() && cacheFile.length() > 0) {
            try {
                return@withContext BitmapFactory.decodeFile(cacheFile.absolutePath)
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при чтении из кэша: ${e.message}")
                // Если не удалось прочитать из кэша, удаляем файл и загружаем заново
                cacheFile.delete()
            }
        }

        // Загружаем изображение
        var retryCount = 0
        var lastException: Exception? = null

        while (retryCount < maxRetries) {
            try {
                // Добавляем параметр cache_bust для предотвращения кэширования на сервере
                val urlWithBust = if (url.contains("cache_bust=")) {
                    url.replace(Regex("cache_bust=\\d+"), "cache_bust=${System.currentTimeMillis()}")
                } else {
                    val separator = if (url.contains("?")) "&" else "?"
                    "$url${separator}cache_bust=${System.currentTimeMillis()}"
                }

                val connection = URL(urlWithBust).openConnection()
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.addRequestProperty("User-Agent", "Mozilla/5.0")

                connection.connect()

                val responseCode = if (connection is java.net.HttpURLConnection) {
                    connection.responseCode
                } else {
                    200 // Если не HTTP-соединение, считаем, что всё ОК
                }

                if (responseCode == 410) {
                    Log.w(TAG, "Получен код 410 для URL: $urlWithBust")
                    retryCount++
                    lastException = Exception("HTTP 410: Gone")
                    // Ждем перед повторной попыткой
                    Thread.sleep(1000L * retryCount)
                    continue
                }

                // Проверяем успешность ответа
                if (connection is java.net.HttpURLConnection && connection.responseCode != 200) {
                    throw Exception("HTTP ${connection.responseCode}: ${connection.responseMessage}")
                }

                // Получаем изображение из потока
                val bitmap = BitmapFactory.decodeStream(connection.inputStream)

                // Сохраняем в кэш
                if (bitmap != null) {
                    try {
                        cacheFile.parentFile?.mkdirs()
                        FileOutputStream(cacheFile).use { outputStream ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                        }
                        Log.d(TAG, "Изображение сохранено в кэш: $hash")
                    } catch (e: Exception) {
                        Log.e(TAG, "Ошибка при сохранении в кэш: ${e.message}")
                    }
                }

                return@withContext bitmap
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при загрузке изображения (попытка ${retryCount + 1}): ${e.message}")
                lastException = e
                retryCount++
                // Ждем перед повторной попыткой
                Thread.sleep(1000L * retryCount)
            }
        }

        Log.e(TAG, "Все попытки загрузки исчерпаны для URL: $url")
        lastException?.let { throw it }
        return@withContext null
    }

    /**
     * Очищает кэш изображений
     */
    fun clearImageCache(context: Context): Boolean {
        val cacheDir = File(context.cacheDir, "images")
        return if (cacheDir.exists()) {
            cacheDir.deleteRecursively()
        } else {
            true
        }
    }
}