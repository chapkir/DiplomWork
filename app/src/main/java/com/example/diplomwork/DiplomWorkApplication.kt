package com.example.diplomwork

import android.app.Application
import android.content.Context
import android.util.Log
import coil.Coil
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.network.ApiClient
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DiplomWorkApplication : Application()
{
    override fun onCreate() {
        super.onCreate()

        // Сбрасываем настройки URL сервера при первом запуске
        resetServerUrlIfNeeded()

        // Инициализация Coil с настройками
        Coil.setImageLoader(ApiClient.createImageLoader(this))
    }

    private fun resetServerUrlIfNeeded() {
        try {
            val prefs = getSharedPreferences("app_first_run", Context.MODE_PRIVATE)
            val isFirstRun = prefs.getBoolean("is_first_run", true)

            if (isFirstRun) {
                Log.d("DiplomWorkApplication", "Первый запуск приложения, сбрасываем URL сервера")

                // Очищаем кэш приложения
                cacheDir.deleteRecursively()

                // Устанавливаем новый URL в SessionManager
                val sessionManager = SessionManager(this)
                sessionManager.setServerUrl("http://spotsychlen.ddns.net:8081")

                // Отмечаем, что первый запуск выполнен
                prefs.edit().putBoolean("is_first_run", false).apply()
            }
        } catch (e: Exception) {
            Log.e("DiplomWorkApplication", "Ошибка при сбросе URL: ${e.message}")
        }
    }
}