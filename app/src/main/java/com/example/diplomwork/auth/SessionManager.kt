package com.example.diplomwork.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import java.util.Date

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREF_NAME = "app_session"
        const val KEY_TOKEN = "auth_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_EXPIRATION = "token_expiration"
        const val KEY_SERVER_URL = "server_url"
        const val KEY_USERNAME = "username"

        private const val TAG = "SessionManager"

        // Значение URL по умолчанию - используем DDNS
        private const val DEFAULT_SERVER_URL = "http://spotsychlen.ddns.net:8081"

        // Время в миллисекундах, которое нужно вычесть из времени истечения
        // для предварительного обновления токена (5 минут)
        private const val TOKEN_REFRESH_MARGIN_MS = 5 * 60 * 1000L
    }

    /**
     * Получает URL сервера
     */
    fun getServerUrl(): String {
        val url = prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
        Log.d(TAG, "Получен URL сервера: $url")

        // Проверка на локальный IP-адрес
        val localIpRegex = Regex("192\\.168\\.|10\\.|172\\.(1[6-9]|2[0-9]|3[0-1])\\.")
        if (url.contains(localIpRegex)) {
            Log.d(TAG, "Обнаружен локальный IP в URL, возвращаем DDNS вместо него")
            return DEFAULT_SERVER_URL
        }

        return url
    }

    /**
     * Устанавливает URL сервера
     */
    fun setServerUrl(url: String) {
        Log.d(TAG, "Сохранение URL сервера: $url")
        val editor = prefs.edit()
        editor.putString(KEY_SERVER_URL, url)
        editor.apply()
    }

    /**
     * Сохраняет имя пользователя
     */
    fun saveUsername(username: String) {
        val editor = prefs.edit()
        editor.putString(KEY_USERNAME, username)
        editor.apply()
    }

    /**
     * Получает имя пользователя
     */
    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    /**
     * Сохраняет токен аутентификации и извлекает из него время истечения срока действия
     */
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(KEY_TOKEN, token)

        // Извлекаем время истечения из JWT токена
        try {
            // Парсим JWT без верификации подписи, нам нужны только payload данные
            val parts = token.split(".")
            if (parts.size == 3) {
                val claims = decodeJwtPayload(parts[1])
                val exp = claims?.exp
                if (exp != null) {
                    val expirationDate = Date(exp * 1000)
                    Log.d(TAG, "Токен истекает: $expirationDate")
                    // Сохраняем время истечения с запасом для раннего обновления
                    editor.putLong(KEY_EXPIRATION, (exp * 1000) - TOKEN_REFRESH_MARGIN_MS)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при извлечении времени истечения токена: ${e.message}")
        }

        editor.apply()
    }

    /**
     * Сохраняет refresh токен
     */
    fun saveRefreshToken(refreshToken: String) {
        val editor = prefs.edit()
        editor.putString(KEY_REFRESH_TOKEN, refreshToken)
        editor.apply()
        Log.d(TAG, "Сохранен refresh токен")
    }

    /**
     * Получает refresh токен
     */
    fun getRefreshToken(): String? {
        val token = prefs.getString(KEY_REFRESH_TOKEN, null)
        Log.d(TAG, "Получен refresh токен: ${token?.take(10)}...")
        return token
    }

    /**
     * Сохраняет аутентификационные данные
     */
    fun saveAuthData(token: String, refreshToken: String?) {
        saveAuthToken(token)
        refreshToken?.let { saveRefreshToken(it) }
        Log.d(TAG, "Сохранены аутентификационные данные")
    }

    /**
     * Декодирует payload JWT токена
     */
    private fun decodeJwtPayload(encodedPayload: String): JwtClaims? {
        return try {
            val padding = when (encodedPayload.length % 4) {
                0 -> ""
                1 -> "==="
                2 -> "=="
                3 -> "="
                else -> ""
            }

            val payload = Base64.decode(
                encodedPayload.replace("-", "+").replace("_", "/") + padding,
                Base64.DEFAULT
            )
            val payloadJson = String(payload, Charsets.UTF_8)
            val gson = Gson()
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
        val token = prefs.getString(KEY_TOKEN, null)
        Log.d(TAG, "Получен auth токен: ${token?.take(10)}...")
        return token
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
        val isLoggedIn = getAuthToken() != null
        Log.d(TAG, "Проверка статуса авторизации: $isLoggedIn")
        return isLoggedIn
    }

    /**
     * Проверяет, истек ли срок действия токена
     */
    fun isTokenExpired(): Boolean {
        val expiration = getTokenExpiration()
        val now = System.currentTimeMillis()
        val isExpired = expiration != null && expiration <= now

        if (isExpired) {
            Log.d(TAG, "Токен истек. Текущее время: ${Date(now)}, время истечения: ${expiration?.let { Date(it) }}")
        }

        return isExpired
    }

    /**
     * Проверяет, истечет ли токен в ближайшее время
     */
    fun willTokenExpireSoon(timeMarginMs: Long = TOKEN_REFRESH_MARGIN_MS): Boolean {
        val expiration = getTokenExpiration()
        val now = System.currentTimeMillis()
        val willExpireSoon = expiration != null && expiration <= (now + timeMarginMs)

        if (willExpireSoon) {
            Log.d(TAG, "Токен истечет в ближайшее время. Текущее время: ${Date(now)}, время истечения: ${expiration?.let { Date(it) }}")
        }

        return willExpireSoon
    }

    /**
     * Проверяет, есть ли refresh токен
     */
    fun hasRefreshToken(): Boolean {
        val hasToken = getRefreshToken() != null
        Log.d(TAG, "Проверка наличия refresh токена: $hasToken")
        return hasToken
    }

    /**
     * Очищает сессию
     */
    fun clearSession() {
        val editor = prefs.edit()
        val serverUrl = getServerUrl()
        editor.clear()
        editor.apply()

        // Сохраняем только URL сервера
        setServerUrl(serverUrl)

        Log.d(TAG, "Сессия очищена")
    }
}

/**
 * Класс для хранения данных JWT payload
 */
data class JwtClaims(
    val sub: String? = null,
    val exp: Long? = null,
    val iat: Long? = null,
    val username: String? = null
)