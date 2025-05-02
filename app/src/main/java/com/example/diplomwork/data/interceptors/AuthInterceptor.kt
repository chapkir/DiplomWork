package com.example.diplomwork.data.interceptors

import com.example.diplomwork.auth.SessionManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager,
    private val refreshTokenMutex: Mutex
) : Interceptor {
    private var isRefreshingToken = false

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val skipAuthPaths = listOf("/api/auth/login", "/api/auth/refresh", "/api/auth/register")
        val shouldSkipAuth = skipAuthPaths.any { original.url.toString().contains(it) }

        val requestBuilder = original.newBuilder()

        if (!shouldSkipAuth) {
            if (sessionManager.isTokenExpired() || sessionManager.willTokenExpireSoon()) {
                val refreshSuccess = refreshTokenIfNeeded()

                if (!refreshSuccess) {
                    sessionManager.clearSession()
                }
            }

            sessionManager.authToken?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
        }

        val request = requestBuilder.build()

        try {
            val response = chain.proceed(request)

            if (response.header("X-Token-Expired") == "true" || response.code == 401) {
                if (sessionManager.hasRefreshToken() && !shouldSkipAuth) {
                    var refreshSuccess = false
                    var newResponse: Response? = null

                    runBlocking {
                        refreshTokenMutex.withLock {
                            if (!isRefreshingToken) {
                                refreshSuccess = refreshTokenIfNeeded(forceRefresh = true)
                            }
                        }
                    }

                    if (refreshSuccess) {
                        response.close()

                        val newToken = sessionManager.authToken
                        if (newToken != null) {
                            val newRequest = original.newBuilder()
                                .header("Authorization", "Bearer $newToken")
                                .build()

                            newResponse = chain.proceed(newRequest)
                            return newResponse!!
                        }
                    }
                }
            }

            return response
        } catch (e: Exception) {
            throw e
        }
    }

    private fun refreshTokenIfNeeded(forceRefresh: Boolean = false): Boolean {
        if (!forceRefresh && !sessionManager.isTokenExpired() && !sessionManager.willTokenExpireSoon()) {
            return true
        }

        if (!sessionManager.hasRefreshToken()) {
            return false
        }

        var success = false
        try {
            isRefreshingToken = true
            val refreshToken = sessionManager.refreshToken

            if (refreshToken != null) {
                // Call your API to refresh the token here and save it to SessionManager
            }
        } catch (e: Exception) {
            // Handle the error and reset flags
        } finally {
            isRefreshingToken = false
        }

        return success
    }
}