package com.example.diplomwork.network.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class RetryInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)
        var tryCount = 0

        while (!response.isSuccessful && tryCount < 3) {
            tryCount++
            response.close()
            response = chain.proceed(request)
        }

        return response
    }
}