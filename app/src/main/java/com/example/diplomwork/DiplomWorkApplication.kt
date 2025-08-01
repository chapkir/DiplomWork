package com.example.diplomwork

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.util.AppConstants
import com.example.diplomwork.util.ImageUtils
import dagger.hilt.android.HiltAndroidApp
import androidx.core.content.edit
import com.google.firebase.FirebaseApp
import com.yandex.mapkit.MapKitFactory

@HiltAndroidApp
class DiplomWorkApplication : Application()
{
    override fun onCreate() {
        super.onCreate()

        // Сбрасываем настройки URL сервера при первом запуске
        resetServerUrlIfNeeded()

        FirebaseApp.initializeApp(this)

        MapKitFactory.setApiKey("5a489e22-0d88-414a-8bd3-f99babb0b6c2")
        MapKitFactory.initialize(this)
    }

    private fun clearImageCache() {
        try {
            // Очищаем локальный кэш изображений
            val success = ImageUtils.clearImageCache(this)

            // Очищаем кэш Coil
            val cacheDir = cacheDir.resolve("image_cache")
            if (cacheDir.exists()) {
                val deletedSize = cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
                val deleted = cacheDir.deleteRecursively()
                Log.d("DiplomWorkApplication", "Очистка кэша Coil: удалено ${deletedSize / 1024} KB, успешно: $deleted")
            }

            Log.d("DiplomWorkApplication", "Кэш изображений очищен: $success")
        } catch (e: Exception) {
            Log.e("DiplomWorkApplication", "Ошибка при очистке кэша изображений: ${e.message}")
        }
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
                sessionManager.serverUrl = AppConstants.BASE_URL

                // Отмечаем, что первый запуск выполнен
                prefs.edit { putBoolean("is_first_run", false) }
            }
        } catch (e: Exception) {
            Log.e("DiplomWorkApplication", "Ошибка при сбросе URL: ${e.message}")
        }
    }
}