package com.example.diplomwork.util

import android.content.Context
import android.util.Log
import com.example.diplomwork.auth.SessionManager

/**
 * Утилитарный класс для очистки и сброса SharedPreferences
 */
object PreferencesCleaner {
    private const val TAG = "PreferencesCleaner"

    /**
     * Полностью очищает настройки URL сервера
     */
    fun resetServerUrl(context: Context) {
        try {
            Log.d(TAG, "Выполняется сброс URL сервера")

            // Очищаем через SessionManager
            val sessionManager = SessionManager(context)
            val ddnsUrl = "http://spotsychlen.ddns.net:8081"
            sessionManager.setServerUrl(ddnsUrl)

            // Дополнительно очищаем напрямую SharedPreferences
            val prefs = context.getSharedPreferences(SessionManager.PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(SessionManager.KEY_SERVER_URL, ddnsUrl).apply()

            Log.d(TAG, "URL сервера сброшен на $ddnsUrl")
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при сбросе URL сервера: ${e.message}")
        }
    }

    /**
     * Проверяет, содержит ли сохраненный URL локальный IP-адрес
     */
    fun hasLocalIpInUrl(context: Context): Boolean {
        val sessionManager = SessionManager(context)
        val url = sessionManager.getServerUrl()
        val localIpRegex = Regex("192\\.168\\.|10\\.|172\\.(1[6-9]|2[0-9]|3[0-1])\\.")

        return url.contains(localIpRegex)
    }
}