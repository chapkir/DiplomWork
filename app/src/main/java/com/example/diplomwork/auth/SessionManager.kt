package com.example.diplomwork.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import java.util.Date
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREF_NAME = "app_session"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRATION = "token_expiration"
        const val KEY_SERVER_URL = "server_url"
        private const val KEY_USERNAME = "username"

        private const val TAG = "SessionManager"
        private const val DEFAULT_SERVER_URL = "http://spotsychlen.ddns.net:8081"
        private const val TOKEN_REFRESH_MARGIN_MS = 5 * 60 * 1000L
    }

    fun getServerUrl(): String {
        val url = prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
        Log.d(TAG, "Получен URL сервера: $url")

        if (url.contains(Regex("192\\.168\\.|10\\.|172\\.(1[6-9]|2[0-9]|3[0-1])\\."))) {
            Log.d(TAG, "Обнаружен локальный IP в URL, возвращаем DDNS вместо него")
            return DEFAULT_SERVER_URL
        }
        return url
    }

    fun setServerUrl(url: String) {
        Log.d(TAG, "Сохранение URL сервера: $url")
        prefs.edit { putString(KEY_SERVER_URL, url) }
    }

    fun saveUsername(username: String) {
        prefs.edit { putString(KEY_USERNAME, username) }
    }

    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)

    fun saveAuthToken(token: String) {
        prefs.edit {
            putString(KEY_TOKEN, token)
            decodeJwtPayload(token)?.exp?.let {
                val expirationDate = Date(it * 1000)
                Log.d(TAG, "Токен истекает: $expirationDate")
                putLong(KEY_EXPIRATION, (it * 1000) - TOKEN_REFRESH_MARGIN_MS)
            }
        }
    }

    fun fetchAuthToken(): String? {
        return prefs.getString("auth_token", null)
    }

    fun saveRefreshToken(refreshToken: String) {
        prefs.edit { putString(KEY_REFRESH_TOKEN, refreshToken) }
        Log.d(TAG, "Сохранен refresh токен")
    }

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun saveAuthData(token: String, refreshToken: String?) {
        saveAuthToken(token)
        refreshToken?.let { saveRefreshToken(it) }
        Log.d(TAG, "Сохранены аутентификационные данные")
    }

    private fun decodeJwtPayload(token: String): JwtClaims? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null
            val payload = Base64.decode(parts[1], Base64.DEFAULT)
            Gson().fromJson(String(payload, Charsets.UTF_8), JwtClaims::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при декодировании JWT: ${e.message}")
            null
        }
    }

    fun getAuthToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getTokenExpiration(): Long? {
        return prefs.getLong(KEY_EXPIRATION, -1).takeIf { it != -1L }
    }

    fun isLoggedIn(): Boolean {
        val isLoggedIn = getAuthToken() != null
        Log.d(TAG, "Проверка статуса авторизации: $isLoggedIn")
        return isLoggedIn
    }

    fun isTokenExpired(): Boolean {
        return getTokenExpiration()?.let {
            val isExpired = it <= System.currentTimeMillis()
            if (isExpired) {
                Log.d(TAG, "Токен истек. Время истечения: ${Date(it)}")
            }
            isExpired
        } ?: false
    }

    fun willTokenExpireSoon(timeMarginMs: Long = TOKEN_REFRESH_MARGIN_MS): Boolean {
        return getTokenExpiration()?.let {
            val willExpireSoon = it <= (System.currentTimeMillis() + timeMarginMs)
            if (willExpireSoon) {
                Log.d(TAG, "Токен истечет скоро. Время истечения: ${Date(it)}")
            }
            willExpireSoon
        } ?: false
    }

    fun hasRefreshToken(): Boolean = getRefreshToken() != null

    fun clearSession() {
        val serverUrl = getServerUrl()
        prefs.edit().clear().apply()
        setServerUrl(serverUrl)
        Log.d(TAG, "Сессия очищена")
    }
}

data class JwtClaims(
    val sub: String? = null,
    val exp: Long? = null,
    val iat: Long? = null,
    val username: String? = null
)