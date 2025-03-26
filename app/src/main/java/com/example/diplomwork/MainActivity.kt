package com.example.diplomwork

import com.example.diplomwork.ui.navigation.AppNavigation
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.example.diplomwork.network.ApiClient
import com.example.diplomwork.system_settings.SetSystemBarsColor
import com.example.diplomwork.ui.theme.ColorForBottomMenu
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.util.PreferencesCleaner
import android.util.Log

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Сбрасываем настройки URL, если обнаружен локальный IP
        if (PreferencesCleaner.hasLocalIpInUrl(this)) {
            Log.d("MainActivity", "Обнаружен локальный IP в настройках, выполняется сброс")
            PreferencesCleaner.resetServerUrl(this)
        }

        // Инициализация ApiClient
        Log.d("MainActivity", "Инициализация ApiClient")
        ApiClient.init(this)

        // Получаем сохраненный URL сервера и устанавливаем его
        val sessionManager = SessionManager(this)
        val serverUrl = sessionManager.getServerUrl()
        Log.d("MainActivity", "URL сервера из SessionManager: $serverUrl")

        // устанавливаем URL DDNS
        val ddnsUrl = "http://spotsychlen.ddns.net:8081/" // Замените на свой DDNS
        ApiClient.setBaseUrl(ddnsUrl)
        Log.d("MainActivity", "Установлен URL сервера: $ddnsUrl")

        // Сохраняем DDNS URL в SharedPreferences
        sessionManager.setServerUrl(ddnsUrl)
        Log.d("MainActivity", "URL сервера сохранен в SessionManager")

        // Проверяем базовый URL в ApiClient после установки
        Log.d("MainActivity", "Текущий базовый URL в ApiClient: ${ApiClient.getBaseUrl()}")

        // Проверяем состояние авторизации
        val isLoggedIn = sessionManager.isLoggedIn()
        val token = sessionManager.getAuthToken()
        Log.d("MainActivity", "Состояние авторизации: $isLoggedIn")
        Log.d("MainActivity", "Токен: ${token?.take(10)}...")

        enableEdgeToEdge()
        setContent {
            SetSystemBarsColor(
                statusBarColor = ColorForBottomMenu,
                navigationBarColor = Color.Black
            )
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}
