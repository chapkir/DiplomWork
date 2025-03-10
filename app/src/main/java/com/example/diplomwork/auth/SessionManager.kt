package com.example.diplomwork.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("AppSession", Context.MODE_PRIVATE)
    private val KEY_TOKEN = "auth_token"

    fun saveAuthToken(token: String) {
        prefs.edit {
            putString(KEY_TOKEN, token)
        }
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun clearSession() {
        prefs.edit {
            clear()
        }
    }

    fun isLoggedIn(): Boolean {
        return getAuthToken() != null
    }
}