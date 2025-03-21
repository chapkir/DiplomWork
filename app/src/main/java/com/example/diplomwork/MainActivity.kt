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
        ApiClient.init(this)

        // Получаем сохраненный URL сервера и устанавливаем его
        val sessionManager = SessionManager(this)
        val serverUrl = sessionManager.getServerUrl()
        ApiClient.setBaseUrl(serverUrl)
        Log.d("MainActivity", "Используется URL сервера: $serverUrl")

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
