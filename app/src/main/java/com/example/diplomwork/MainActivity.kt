package com.example.diplomwork

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.diplomwork.presentation.ui.navigation.AppNavigation
import com.example.diplomwork.presentation.ui.theme.DiplomWorkTheme
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "Получен токен: $token")
                // Здесь можно отправить токен на сервер
            } else {
                Log.e("FCM", "Ошибка при получении токена")
            }
        }

        enableEdgeToEdge()
        setContent {
            DiplomWorkTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}
