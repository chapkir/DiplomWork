package com.example.diplomwork.network

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.example.diplomwork.auth.SessionManager
import java.net.SocketTimeoutException
import java.io.IOException
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy

object ApiClient {
    // Заменяем жестко закодированный IP на базовый URL, который можно изменить
    private const val DEFAULT_SERVER_URL = "http://192.168.1.181:8081/"
    private var serverUrl = DEFAULT_SERVER_URL

    // Геттер для получения текущего базового URL
    fun getBaseUrl(): String = serverUrl

    // Сеттер для изменения базового URL (можно использовать для настройки)
    fun setBaseUrl(url: String) {
        serverUrl = if (url.endsWith("/")) url else "$url/"
        recreateRetrofit()
    }

    private const val TAG = "ApiClient"
    private lateinit var sessionManager: SessionManager
    private lateinit var retrofit: Retrofit
    private lateinit var _apiService: ApiService
    private lateinit var _imageUploadService: ImageUploadService

    fun init(context: Context) {
        sessionManager = SessionManager(context)
        createRetrofit()
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val corsInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("Accept", "*/*")
            .header("Connection", "keep-alive")
            .header("Accept-Encoding", "gzip, deflate, br")
            .header("Origin", serverUrl.trimEnd('/'))
            .header("Access-Control-Request-Method", original.method)
            .header("Access-Control-Request-Headers", "Authorization, Content-Type")
            .method(original.method, original.body)

        chain.proceed(requestBuilder.build())
    }

    // Добавляем свой интерцептор для отслеживания запросов
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()

        // Логируем информацию о запросе
        Log.d(TAG, "Отправка запроса: ${original.method} ${original.url}")
        Log.d(TAG, "Заголовки запроса: ${original.headers}")

        // Получаем токен и добавляем его в заголовок, если он существует
        if (::sessionManager.isInitialized) {
            sessionManager.getAuthToken()?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
                Log.d(TAG, "Добавлен токен авторизации: Bearer ${token.take(10)}...")
            }
        }

        val request = requestBuilder.build()
        try {
            val response = chain.proceed(request)

            // Логируем информацию об ответе
            Log.d(TAG, "Получен ответ: ${response.code} для ${request.url}")
            Log.d(TAG, "Заголовки ответа: ${response.headers}")

            val responseBody = response.peekBody(Long.MAX_VALUE).string()
            Log.d(TAG, "Тело ответа: $responseBody")

            if (!response.isSuccessful) {
                Log.e(TAG, "Ошибка запроса: ${response.code} ${response.message}")
                Log.e(TAG, "Тело ответа с ошибкой: $responseBody")
            }

            response
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Таймаут соединения: ${e.message}")
            Log.e(TAG, "Стек вызовов: ${e.stackTraceToString()}")
            throw e
        } catch (e: IOException) {
            Log.e(TAG, "Ошибка сети: ${e.message}")
            Log.e(TAG, "Стек вызовов: ${e.stackTraceToString()}")
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Неожиданная ошибка: ${e.message}")
            Log.e(TAG, "Стек вызовов: ${e.stackTraceToString()}")
            throw e
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .addInterceptor(corsInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        // Добавляем обработчик ошибок для повторных попыток
        .addInterceptor { chain ->
            val request = chain.request()
            var response: okhttp3.Response? = null
            var retryCount = 0
            val maxRetries = 3
            var exception: IOException? = null

            while (retryCount < maxRetries) {
                try {
                    // Если это не первая попытка, добавляем случайную задержку
                    if (retryCount > 0) {
                        val backoffMs = (2000L * (retryCount)) + (Math.random() * 1000).toLong()
                        Log.d(TAG, "Повторная попытка #$retryCount через $backoffMs мс: ${request.url}")
                        Thread.sleep(backoffMs)
                    }

                    response = chain.proceed(request)

                    // Проверяем специфичные ошибки, требующие повторной попытки
                    if (response.code in listOf(408, 429, 500, 502, 503, 504, 410) && retryCount < maxRetries - 1) {
                        Log.w(TAG, "Код ответа ${response.code} требует повторной попытки")
                        response.close()
                        retryCount++
                        continue
                    }

                    // Для успешных ответов или ошибок, не требующих повторных попыток
                    return@addInterceptor response
                } catch (e: IOException) {
                    exception = e
                    if (retryCount < maxRetries - 1) {
                        Log.w(TAG, "Попытка #$retryCount не удалась с ошибкой: ${e.message}")
                        retryCount++
                    } else {
                        Log.e(TAG, "Все попытки исчерпаны")
                        throw e
                    }
                }
            }

            // Если мы здесь, значит все попытки исчерпаны
            throw exception ?: IOException("Неизвестная ошибка после $maxRetries попыток")
        }
        .build()

    private fun createRetrofit() {
        retrofit = Retrofit.Builder()
            .baseUrl(serverUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        _apiService = retrofit.create(ApiService::class.java)
        _imageUploadService = retrofit.create(ImageUploadService::class.java)
    }

    private fun recreateRetrofit() {
        if (::retrofit.isInitialized) {
            createRetrofit()
        }
    }

    val apiService: ApiService
        get() = _apiService

    val imageUploadService: ImageUploadService
        get() = _imageUploadService

    fun createImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.05) // Увеличиваем размер кэша
                    .build()
            }
            .respectCacheHeaders(false) // Игнорируем заголовки кэширования
            .crossfade(true)
            .error(android.R.drawable.ic_menu_report_image) // Изображение по умолчанию при ошибке
            .build()
    }
}