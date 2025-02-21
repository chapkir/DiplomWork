package com.example.diplomwork.ui
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.diplomwork.ui.screens.main_screen.MainScreen
import com.example.diplomwork.ui.screens.image_detail_screen.ImageDetailScreen



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
                initialLikesCount = 0,
                initialComments = listOf("Комментарий 1", "Комментарий 2"),
                onLikeClick = { /* Здесь можно добавить обработку лайка — например, вызов API */ },
                onCommentSubmit = { comment ->
                    // Здесь можно добавить обработку отправки комментария, например, вызов API.
                    println("Новый комментарий: $comment")
                }
            )
        }
    }
}