package com.example.diplomwork.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("AppSession", Context.MODE_PRIVATE)
    private val KEY_TOKEN = "auth_token"
    private val KEY_SERVER_URL = "server_url"
    private val DEFAULT_SERVER_URL = "http://192.168.1.125:8081"

    fun saveAuthToken(token: String) {
        prefs.edit {
            putString(KEY_TOKEN, token)
        }
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun saveServerUrl(url: String) {
        prefs.edit {
            putString(KEY_SERVER_URL, url)
        }
    }

    fun getServerUrl(): String {
        return prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
    }

    fun clearSession() {
        // Сохраняем URL сервера даже при выходе из системы
        val serverUrl = getServerUrl()

        prefs.edit {
            clear()
            // Восстанавливаем URL сервера
            putString(KEY_SERVER_URL, serverUrl)
        }
    }

    fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }
}