package com.example.diplomwork.ui.screens.home_screen

import android.net.Uri
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.example.diplomwork.ui.screens.home_screen.bottom_menu.BottomMenu
import com.example.diplomwork.ui.screens.home_screen.content_grid.ContentGrid

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(onImageClick: (Long, String) -> Unit) {
    ContentGrid(
        modifier = Modifier.fillMaxSize(),
        onImageClick = { pin ->
            onImageClick(pin.id, pin.imageUrl)
        }
    )
}