package com.example.diplomwork.system_settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun SetSystemBarsColor(
    statusBarColor: Color = Color.Black,
    navigationBarColor: Color = Color.Black
) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()

    LaunchedEffect(Unit) {
        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = useDarkIcons
        )
        systemUiController.setNavigationBarColor(
            color = navigationBarColor,
            darkIcons = useDarkIcons
        )
    }
}
