package com.example.diplomwork.ui.screens.main_screen
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
import com.example.diplomwork.ui.theme.DarkWhite
import com.example.diplomwork.ui.screens.main_screen.bottom_menu.BottomMenu
import com.example.diplomwork.ui.screens.main_screen.content_grid.ContentGrid
import com.example.diplomwork.ui.screens.main_screen.top_bar.TopBar


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(navController: NavHostController) {
    val systemUiController = rememberSystemUiController()
    SideEffect {
        systemUiController.setNavigationBarColor(Color.Black)
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomMenu(navController)
        }
    ) { paddingValues ->
        ContentGrid(
            modifier = Modifier.padding(paddingValues),
            onImageClick = { imageUrl ->
                navController.navigate("image_detail_screen?imageUrl=${Uri.encode(imageUrl)}")
            }
        )
    }
}