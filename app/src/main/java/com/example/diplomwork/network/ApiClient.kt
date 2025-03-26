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
import com.example.diplomwork.model.TokenRefreshRequest
import com.example.diplomwork.model.TokenRefreshResponse
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException

object ApiClient {
    // Default DDNS URL
    private var serverUrl = "http://spotsychlen.ddns.net:8081/"

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

    // Мутекс для синхронизации обновления токенов
    private val refreshTokenMutex = Mutex()

    // Флаг для отслеживания процесса обновления токена
    private var isRefreshingToken = false

    // Максимальное количество попыток обновления токена
    private const val MAX_REFRESH_ATTEMPTS = 3

    // Счетчик попыток обновления токена
    private var refreshAttempts = 0

    fun init(context: Context) {
        sessionManager = SessionManager(context)
        // Получаем сохраненный URL сервера
        var savedUrl = sessionManager.getServerUrl()

        // Проверяем, не локальный ли это IP
        val localIpRegex = Regex("192\\.168\\.|10\\.|172\\.(1[6-9]|2[0-9]|3[0-1])\\.")
        if (savedUrl.contains(localIpRegex)) {
            // Если это локальный IP, заменяем на DDNS
            Log.d(TAG, "Найден локальный IP в URL: $savedUrl, заменяем на DDNS")
            savedUrl = "http://spotsychlen.ddns.net:8081/"
            sessionManager.setServerUrl(savedUrl)
        }

        serverUrl = savedUrl
        if (!serverUrl.endsWith("/")) {
            serverUrl += "/"
        }
        Log.d(TAG, "ApiClient инициализирован с URL: $serverUrl")
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

    // Добавляем свой интерцептор для отслеживания запросов и обновления токенов
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()

        // Пропускаем запросы на обновление токена и вход
        val skipAuthPaths = listOf("/api/auth/login", "/api/auth/refresh", "/api/auth/register")
        val shouldSkipAuth = skipAuthPaths.any { original.url.toString().contains(it) }

        val requestBuilder = original.newBuilder()

        // Логируем информацию о запросе
        Log.d(TAG, "Отправка запроса: ${original.method} ${original.url}")

        if (::sessionManager.isInitialized && !shouldSkipAuth) {
            // Проверяем, не истек ли токен
            if ((sessionManager.isTokenExpired() || sessionManager.willTokenExpireSoon()) && sessionManager.hasRefreshToken()) {
                Log.d(TAG, "Токен истек или скоро истечет, пытаемся обновить")

                // Обновляем токен перед выполнением запроса
                val refreshSuccess = refreshTokenIfNeeded()

                if (!refreshSuccess) {
                    Log.e(TAG, "Не удалось обновить токен, очищаем сессию")
                    sessionManager.clearSession()
                }
            }

            // После возможного обновления токена добавляем актуальный токен в запрос
            sessionManager.getAuthToken()?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
                Log.d(TAG, "Добавлен токен авторизации: Bearer ${token.take(10)}...")
            } ?: Log.w(TAG, "Токен авторизации отсутствует")
        } else if (!::sessionManager.isInitialized) {
            Log.w(TAG, "SessionManager не инициализирован")
        } else if (shouldSkipAuth) {
            Log.d(TAG, "Пропускаем авторизацию для: ${original.url}")
        }

        val request = requestBuilder.build()

        try {
            // Выполняем запрос
            val response = chain.proceed(request)

            Log.d(TAG, "Получен ответ: ${response.code} для ${request.url}")

            // Проверяем заголовок X-Token-Expired и код 401
            if (response.header("X-Token-Expired") == "true" || response.code == 401) {
                if (response.header("X-Token-Expired") == "true") {
                    Log.d(TAG, "Получен заголовок X-Token-Expired")
                } else if (response.code == 401) {
                    Log.d(TAG, "Получен код 401 Unauthorized")
                }

                // Проверяем, есть ли у нас refresh token и не выполняем ли мы уже обновление
                if (sessionManager.hasRefreshToken() && !shouldSkipAuth) {
                    var refreshSuccess = false
                    var newResponse: Response? = null

                    runBlocking {
                        refreshTokenMutex.withLock {
                            if (!isRefreshingToken) {
                                refreshSuccess = refreshTokenIfNeeded(forceRefresh = true)
                            }
                        }
                    }

                    // Если обновление токена успешно, повторяем запрос с новым токеном
                    if (refreshSuccess) {
                        response.close() // Закрываем старый ответ

                        val newToken = sessionManager.getAuthToken()
                        if (newToken != null) {
                            val newRequest = original.newBuilder()
                                .header("Authorization", "Bearer $newToken")
                                .build()

                            Log.d(TAG, "Повторный запрос с новым токеном: ${original.url}")

                            // Выполняем запрос заново
                            try {
                                newResponse = chain.proceed(newRequest)
                                return@Interceptor newResponse!!
                            } catch (e: Exception) {
                                Log.e(TAG, "Ошибка при повторном запросе: ${e.message}")
                                throw e
                            }
                        } else {
                            Log.e(TAG, "Не удалось получить новый токен после обновления")
                        }
                    } else {
                        Log.d(TAG, "Не удалось обновить токен, возвращаем оригинальный ответ")
                    }
                } else {
                    if (!sessionManager.hasRefreshToken()) {
                        Log.d(TAG, "Нет refresh токена, очищаем сессию")
                    } else if (shouldSkipAuth) {
                        Log.d(TAG, "Запрос для пути, который не требует авторизации")
                    } else if (isRefreshingToken) {
                        Log.d(TAG, "Процесс обновления токена уже выполняется")
                    }
                    sessionManager.clearSession()
                }
            }

            return@Interceptor response
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при выполнении запроса: ${e.message}")
            Log.e(TAG, "Стек вызовов: ${e.stackTraceToString()}")
            throw e
        }
    }

    // Метод для обновления токена доступа
    private fun refreshTokenIfNeeded(forceRefresh: Boolean = false): Boolean {
        // Проверяем, нужно ли обновлять токен
        if (!forceRefresh && !sessionManager.isTokenExpired() && !sessionManager.willTokenExpireSoon()) {
            return true  // Токен не требует обновления
        }

        // Проверяем наличие refresh токена
        if (!sessionManager.hasRefreshToken()) {
            Log.e(TAG, "Отсутствует refresh токен для обновления")
            return false
        }

        // Проверяем лимит попыток обновления
        if (refreshAttempts >= MAX_REFRESH_ATTEMPTS) {
            Log.e(TAG, "Достигнут лимит попыток обновления токена: $MAX_REFRESH_ATTEMPTS")
            refreshAttempts = 0  // Сбрасываем счетчик
            return false
        }

        var success = false

        try {
            isRefreshingToken = true
            refreshAttempts++  // Увеличиваем счетчик попыток

            Log.d(TAG, "Обновление токена, попытка #$refreshAttempts")
            val refreshToken = sessionManager.getRefreshToken()

            if (refreshToken != null) {
                val username = sessionManager.getUsername()
                Log.d(TAG, "Отправляем запрос на обновление токена для пользователя: $username")

                runBlocking {
                    try {
                        val tokenRequest = TokenRefreshRequest(refreshToken)
                        val response = _apiService.refreshToken(tokenRequest)

                        // Сохраняем новые токены
                        Log.d(TAG, "Получен новый токен доступа")
                        sessionManager.saveAuthData(response.accessToken, response.refreshToken)
                        Log.d(TAG, "Токены успешно обновлены")

                        // Сбрасываем счетчик попыток при успехе
                        refreshAttempts = 0
                        success = true
                    } catch (e: HttpException) {
                        Log.e(TAG, "HTTP ошибка при обновлении токена: ${e.code()}")
                        Log.e(TAG, "Сообщение: ${e.message()}")

                        // Если сервер отверг refresh токен, очищаем сессию
                        if (e.code() == 401 || e.code() == 403) {
                            Log.e(TAG, "Refresh токен недействителен, очищаем сессию")
                            sessionManager.clearSession()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Ошибка при обновлении токена: ${e.message}")
                        e.printStackTrace()
                    }
                }
            } else {
                Log.e(TAG, "Refresh токен равен null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Исключение при обновлении токена: ${e.message}")
            e.printStackTrace()
        } finally {
            isRefreshingToken = false
        }

        return success
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

        Log.d(TAG, "Создан Retrofit клиент с базовым URL: $serverUrl")
    }

    private fun recreateRetrofit() {
        if (::retrofit.isInitialized) {
            Log.d(TAG, "Пересоздание Retrofit клиента с URL: $serverUrl")
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
                        // Используем механизм обновления токена из основного клиента
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