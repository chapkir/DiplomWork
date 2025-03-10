package com.example.diplomwork

import com.example.diplomwork.ui.components.AppNavigation
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ApiClient.init(this)
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
