package com.example.diplomwork.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.util.Date

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREF_NAME = "app_session"
        const val KEY_TOKEN = "auth_token"
        const val KEY_EXPIRATION = "token_expiration"
        const val KEY_SERVER_URL = "server_url"

        private const val TAG = "SessionManager"

        // URL сервера по умолчанию
        private const val DEFAULT_SERVER_URL = "http://192.168.1.125:8081"
    }

    /**
     * Получает URL сервера
     */
    fun getServerUrl(): String {
        return prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
    }

    /**
     * Устанавливает URL сервера
     */
    fun setServerUrl(url: String) {
        val editor = prefs.edit()
        editor.putString(KEY_SERVER_URL, url)
        editor.apply()
    }

    /**
     * Сохраняет токен аутентификации и извлекает из него время истечения срока действия
     */
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(KEY_TOKEN, token)

        // Извлекаем время истечения из JWT токена, если возможно
        try {
            // Парсим JWT без верификации подписи, нам нужны только payload данные
            val parts = token.split(".")
            if (parts.size == 3) {
                val claims = decodeJwtPayload(parts[1])
                val exp = claims?.exp
                if (exp != null) {
                    Log.d(TAG, "Токен истекает: ${Date(exp * 1000)}")
                    editor.putLong(KEY_EXPIRATION, exp * 1000)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при извлечении времени истечения токена: ${e.message}")
        }

        editor.apply()
    }

    /**
     * Декодирует payload JWT токена
     */
    private fun decodeJwtPayload(encodedPayload: String): JwtClaims? {
        return try {
            val payload = android.util.Base64.decode(
                encodedPayload.replace("-", "+").replace("_", "/"),
                android.util.Base64.DEFAULT
            )
            val payloadJson = String(payload, Charsets.UTF_8)
            val gson = com.google.gson.Gson()
            gson.fromJson(payloadJson, JwtClaims::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при декодировании JWT payload: ${e.message}")
            null
        }
    }

    /**
     * Получает токен аутентификации
     */
    fun getAuthToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    /**
     * Получает время истечения токена в миллисекундах
     */
    fun getTokenExpiration(): Long? {
        val expiration = prefs.getLong(KEY_EXPIRATION, -1)
        return if (expiration != -1L) expiration else null
    }

    /**
     * Проверяет, залогинен ли пользователь
     */
    fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }

    /**
     * Проверяет, истек ли срок действия токена
     */
    fun isTokenExpired(): Boolean {
        val expiration = getTokenExpiration()
        return expiration != null && expiration < System.currentTimeMillis()
    }

    /**
     * Очищает сессию
     */
    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}

/**
 * Класс для хранения данных JWT payload
 */
data class JwtClaims(
    val sub: String? = null,
    val exp: Long? = null,
    val iat: Long? = null
)