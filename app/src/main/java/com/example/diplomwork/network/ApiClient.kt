package com.example.diplomwork.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



object ApiClient {

    val baseUrl = "http://192.168.134.109:8081"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}