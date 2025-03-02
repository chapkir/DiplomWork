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

object ApiClient {
    const val baseUrl = "http://192.168.1.125:8081/"
    private const val TAG = "ApiClient"
    private lateinit var sessionManager: SessionManager

    fun init(context: Context) {
        sessionManager = SessionManager(context)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Добавляем свой интерцептор для отслеживания запросов
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()

        // Логируем информацию о запросе
        Log.d(TAG, "Отправка запроса: ${original.method} ${original.url}")

        // Получаем токен и добавляем его в заголовок, если он существует
        if (::sessionManager.isInitialized) {
            sessionManager.getAuthToken()?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
                Log.d(TAG, "Добавлен токен авторизации")
            }
        }

        val request = requestBuilder.build()
        val response = chain.proceed(request)

        // Логируем информацию об ответе
        Log.d(TAG, "Получен ответ: ${response.code} для ${request.url}")

        if (!response.isSuccessful) {
            Log.e(TAG, "Ошибка запроса: ${response.code} ${response.message}")
        }

        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}