package com.example.diplomwork.ui.screens.main_screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.diplomwork.ui.theme.DarkWhite
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.example.diplomwork.ui.screens.main_screen.bottom_menu.BottomMenu
import com.example.diplomwork.ui.screens.main_screen.content_grid.ContentGrid
import com.example.diplomwork.ui.screens.main_screen.top_bar.TopBar


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
//@Preview(showBackground = true)
@Composable
fun MainScreen(navController: NavHostController) {

    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setNavigationBarColor(Color.Black)
        //systemUiController.setStatusBarColor(Color.Black)
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
        ContentGrid(
            modifier = Modifier.padding(paddingValues),
            onImageClick = { imageRes ->
                navController.navigate("image_detail_screen/$imageRes")

                /* Передаем navController в ContentGrid через onImageClick
                */
            }
        )
    }
}
