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
    // Значение по умолчанию для эмулятора Android
    private const val DEFAULT_SERVER_URL = "http://10.0.2.2:8080/"
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
        // Получаем сохраненный URL сервера
        serverUrl = sessionManager.getServerUrl()
        if (!serverUrl.endsWith("/")) {
            serverUrl += "/"
        }
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
            } ?: run {
                Log.w(TAG, "Токен авторизации отсутствует")
            }
        } else {
            Log.w(TAG, "SessionManager не инициализирован")
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
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при выполнении запроса: ${e.message}")
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

    // Метод для создания OkHttpClient с интерсептором для добавления токена аутентификации
    fun createAuthenticatedClient(context: Context): OkHttpClient {
        val sessionManager = SessionManager(context)

        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // Увеличиваем таймаут для загрузки изображений
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val token = sessionManager.getAuthToken()

                // Логируем информацию о запросе
                Log.d(TAG, "Аутентифицированный запрос: ${originalRequest.method} ${originalRequest.url}")

                val newRequest = if (token != null) {
                    val tokenExpiration = sessionManager.getTokenExpiration()
                    val isExpired = tokenExpiration != null && tokenExpiration < System.currentTimeMillis()

                    if (isExpired) {
                        Log.d(TAG, "Токен истек, требуется повторная аутентификация")
                        // Здесь можно добавить логику для обновления токена
                        // Если доступен refresh token, обновляем токен
                    }

                    // Добавляем заголовок авторизации с токеном
                    originalRequest.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else {
                    originalRequest
                }

                // Выполняем запрос
                try {
                    val response = chain.proceed(newRequest)

                    // Проверяем ответ на 401 (Unauthorized)
                    if (response.code == 401) {
                        Log.d(TAG, "Получен код 401, очищаем токен")
                        sessionManager.clearSession()
                    }

                    response
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка при выполнении аутентифицированного запроса: ${e.message}")
                    throw e
                }
            }
            .build()
    }

    // Проверяет, является ли URL постоянной ссылкой или прокси-ссылкой
    // Возвращает оптимальный URL для загрузки изображения
    fun getOptimalImageUrl(url: String?, baseUrl: String): String {
        if (url.isNullOrEmpty()) {
            return ""
        }

        // Если это уже прокси-URL, возвращаем как есть
        if (url.contains("/api/pins/proxy-image")) {
            // Но всё равно добавляем параметр disposition, если это ссылка Яндекс.Диска
            if (url.contains("yandex") || url.contains("disk.") ||
                url.contains("downloader.") || url.contains("preview.")) {

                // Проверяем наличие disposition параметра
                if (!url.contains("disposition=")) {
                    return if (url.contains("?")) {
                        "$url&disposition=inline"
                    } else {
                        "$url?disposition=inline"
                    }
                }
            }
            return url
        }

        // Для всех ссылок Яндекс.Диска используем прокси-сервер для обхода ограничений
        if (url.contains("yandex") ||
            url.contains("disk.") ||
            url.contains("yadi.sk") ||
            url.contains("downloader.") ||
            url.contains("preview.")) {

            Log.d(TAG, "Использую прокси для URL Яндекс Диска: $url")

            try {
                // Сначала фиксируем URL с параметром disposition
                val fixedUrl = com.example.diplomwork.util.ImageUtils.fixYandexDiskUrl(url)
                val encodedUrl = java.net.URLEncoder.encode(fixedUrl, "UTF-8")
                val proxyUrl = "${baseUrl}api/pins/proxy-image?url=$encodedUrl"
                Log.d(TAG, "Создан прокси-URL: $proxyUrl")
                return proxyUrl
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при создании прокси-URL: ${e.message}")
                return com.example.diplomwork.util.ImageUtils.fixYandexDiskUrl(url)
            }
        }

        // Для остальных URL возвращаем как есть
        return url
    }
}