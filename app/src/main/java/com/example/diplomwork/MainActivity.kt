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
import android.util.Log

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация ApiClient
        Log.d("MainActivity", "Инициализация ApiClient")
        ApiClient.init(this)

        // Получаем сохраненный URL сервера и устанавливаем его
        val sessionManager = SessionManager(this)
        val serverUrl = sessionManager.getServerUrl()
        Log.d("MainActivity", "URL сервера: $serverUrl")
        ApiClient.setBaseUrl(serverUrl)

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
