package com.example.diplomwork.ui.main_screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.diplomwork.ui.theme.DarkWhite
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.ui.Alignment
import com.example.diplomwork.ui.main_screen.bottom_menu.BottomMenu
import com.example.diplomwork.ui.main_screen.content_grid.ContentGrid
import com.example.diplomwork.ui.main_screen.top_bar.TopBar


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showBackground = true)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val systemUiController = rememberSystemUiController()

    SideEffect {
        //systemUiController.setNavigationBarColor(Color.Black)
        systemUiController.setStatusBarColor(DarkWhite)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomMenu()
        }

    ) { paddingValues ->
        ContentGrid(Modifier.padding(paddingValues))
    }
}
