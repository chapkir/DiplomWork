package com.example.diplomwork.ui
import ImageDetailScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.diplomwork.ui.screens.main_screen.MainScreen



@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "main_screen"
    ) {
        composable("main_screen") {
            MainScreen(navController = navController)
        }
// Маршрут для экрана деталей – принимает строковый параметр imageUrl
        composable("image_detail_screen?imageUrl={imageUrl}") { backStackEntry ->
            val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
            ImageDetailScreen(
                imageUrl = imageUrl,
                likesCount = 0,
                comments = listOf("Комментарий 1", "Комментарий 2"),
                onLikeClick = { /* Добавить обработку лайка */ }
            )
        }
    }
}