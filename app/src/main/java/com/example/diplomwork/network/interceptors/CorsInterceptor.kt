package com.example.diplomwork.network.interceptors

import com.example.diplomwork.ui.util.AppConstants
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class CorsInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val requestBuilder = original.newBuilder()
            .header("Accept", "*/*")
            .header("Connection", "keep-alive")
            .header("Accept-Encoding", "gzip, deflate, br")
            .header("Origin", AppConstants.BASE_URL.trimEnd('/'))
            .header("Access-Control-Request-Method", original.method)
            .header("Access-Control-Request-Headers", "Authorization, Content-Type")
            .method(original.method, original.body)

        return chain.proceed(requestBuilder.build())
    }
}