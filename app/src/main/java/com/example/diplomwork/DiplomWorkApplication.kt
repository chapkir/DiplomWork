package com.example.diplomwork

import android.app.Application
import coil.Coil
import com.example.diplomwork.network.ApiClient
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DiplomWorkApplication : Application()
{
    override fun onCreate() {
        super.onCreate()

        // Инициализация Coil с настройками
        Coil.setImageLoader(ApiClient.createImageLoader(this))
    }
}