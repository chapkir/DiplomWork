package com.example.diplomwork.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import java.util.Date
import androidx.core.content.edit
import com.example.diplomwork.util.AppConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "app_session"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRATION = "token_expiration"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_USERNAME = "username"

        private const val TAG = "SessionManager"
        private const val DEFAULT_SERVER_URL = AppConstants.BASE_URL
        private const val TOKEN_REFRESH_MARGIN_MS = 5 * 60 * 1000L
    }

    var serverUrl: String
        get() {
            val url = prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL
            Log.d(TAG, "Получен URL сервера: $url")
            return if (url.contains(Regex("192\\.168\\.|10\\.|172\\.(1[6-9]|2[0-9]|3[0-1])\\."))) {
                Log.d(TAG, "Обнаружен локальный IP в URL, возвращаем DDNS вместо него")
                DEFAULT_SERVER_URL
            } else url
        }
        set(value) {
            Log.d(TAG, "Сохранение URL сервера: $value")
            prefs.edit() { putString(KEY_SERVER_URL, value) }
        }

    val userId: String?
        get() = authToken?.let { decodeJwtPayload(it)?.sub }

    var username: String?
        get() = prefs.getString(KEY_USERNAME, null)
        set(value) {
            prefs.edit() { putString(KEY_USERNAME, value) }
            Log.d(TAG, "Сохранен username - $username")
        }

    var authToken: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) {
            prefs.edit() { putString(KEY_TOKEN, value) }
            Log.d(TAG, "Сохранен auth токен - $authToken")
        }

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        private set(value) {
            prefs.edit() { putString(KEY_REFRESH_TOKEN, value) }
            Log.d(TAG, "Сохранен refresh токен - $refreshToken")
        }

    var tokenExpiration: Long?
        get() = prefs.getLong(KEY_EXPIRATION, -1).takeIf { it != -1L }
        private set(value) {
            value?.let {
                prefs.edit() { putLong(KEY_EXPIRATION, it - TOKEN_REFRESH_MARGIN_MS) }
                Log.d(TAG, "Токен истекает: ${Date(it)}")
            }
        }

    fun saveAuthData(token: String, refreshToken: String?) {
        authToken = token
        tokenExpiration = decodeJwtPayload(token)?.exp?.times(1000)
        val claims = decodeJwtPayload(token)
        refreshToken?.let { this.refreshToken = it }
        Log.d(TAG, "Сохранены аутентификационные данные")
        Log.d(TAG, "userId из токена: ${claims?.iat} или ${claims?.exp}")
    }

    private fun decodeJwtPayload(token: String): JwtClaims? {
        return try {
            val payload = Base64.decode(token.split(".").getOrNull(1), Base64.DEFAULT)
            Gson().fromJson(String(payload, Charsets.UTF_8), JwtClaims::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при декодировании JWT: ${e.message}")
            null
        }
    }

    fun isLoggedIn(): Boolean = authToken != null

    fun isTokenExpired(): Boolean {
        return tokenExpiration?.let {
            val expired = it <= System.currentTimeMillis()
            if (expired) Log.d(TAG, "Токен истек: ${Date(it)}")
            expired
        } ?: false
    }

    fun willTokenExpireSoon(timeMarginMs: Long = TOKEN_REFRESH_MARGIN_MS): Boolean {
        return tokenExpiration?.let {
            val soonExpired = it <= (System.currentTimeMillis() + timeMarginMs)
            if (soonExpired) Log.d(TAG, "Токен истечет скоро: ${Date(it)}")
            soonExpired
        } ?: false
    }

    fun hasRefreshToken(): Boolean = refreshToken != null

    fun clearSession() {
        val savedServerUrl = serverUrl
        prefs.edit() { clear() }
        serverUrl = savedServerUrl
        Log.d(TAG, "Сессия очищена")
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        authToken = accessToken
        this.refreshToken = refreshToken
        tokenExpiration = decodeJwtPayload(accessToken)?.exp?.times(1000)
        Log.d(TAG, "Токены обновлены через saveTokens")
    }
}

data class JwtClaims(
    val sub: String? = null,
    val exp: Long? = null,
    val iat: Long? = null,
    val username: String? = null
)