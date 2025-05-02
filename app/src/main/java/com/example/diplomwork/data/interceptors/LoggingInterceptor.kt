package com.example.diplomwork.data.interceptors

import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor

class LoggingInterceptor @Inject constructor() : Interceptor {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        return loggingInterceptor.intercept(chain)
    }
}