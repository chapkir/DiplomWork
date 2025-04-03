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
import com.example.diplomwork.system_settings.SetSystemBarsColor
import com.example.diplomwork.ui.theme.ColorForBackground
import com.example.diplomwork.util.ImageUtils
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //ApiClient.init(this)

        // Очищаем кэш изображений при каждом запуске
        //clearImageCaches()

        enableEdgeToEdge()
        setContent {
            SetSystemBarsColor(
                statusBarColor = ColorForBackground,
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
