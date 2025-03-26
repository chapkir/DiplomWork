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
import com.example.diplomwork.ui.util.ImageUtils
import android.util.Log
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация ApiClient
        Log.d("MainActivity", "Инициализация ApiClient")
        ApiClient.init(this)

        // Проверяем и сбрасываем настройки URL
        checkAndResetServerUrl()

        // Очищаем кэш изображений при каждом запуске
        clearImageCaches()

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

    private fun checkAndResetServerUrl() {
        // Сбрасываем настройки URL, если обнаружен локальный IP
        if (PreferencesCleaner.hasLocalIpInUrl(this)) {
            Log.d("MainActivity", "Обнаружен локальный IP в настройках, выполняется сброс")
            PreferencesCleaner.resetServerUrl(this)
        }

        // Получаем сохраненный URL сервера и устанавливаем его
        val sessionManager = SessionManager(this)
        val serverUrl = sessionManager.getServerUrl()
        Log.d("MainActivity", "URL сервера из SessionManager: $serverUrl")

        // Принудительно устанавливаем URL DDNS
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
    }

    private fun clearImageCaches() {
        // Запускаем очистку кэша в фоновом потоке
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Очищаем наш пользовательский кэш изображений
                val success = ImageUtils.clearImageCache(this@MainActivity)
                Log.d("MainActivity", "Очистка пользовательского кэша: $success")

                // Очищаем кэш Coil
                val cacheDir = File(cacheDir, "image_cache")
                if (cacheDir.exists()) {
                    val deletedSize = cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
                    val deleted = cacheDir.deleteRecursively()
                    Log.d("MainActivity", "Очистка кэша Coil: удалено ${deletedSize / 1024} KB, успешно: $deleted")
                }

                // Очищаем HTTP кэш
                val httpCacheDir = File(cacheDir, "http-cache")
                if (httpCacheDir.exists()) {
                    val deletedSize = httpCacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
                    val deleted = httpCacheDir.deleteRecursively()
                    Log.d("MainActivity", "Очистка HTTP кэша: удалено ${deletedSize / 1024} KB, успешно: $deleted")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Ошибка при очистке кэша: ${e.message}")
            }
        }
    }
}
