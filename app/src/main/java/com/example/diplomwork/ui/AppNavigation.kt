package com.example.diplomwork.ui
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.diplomwork.model.Comment
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
                initialComments = listOf(
                    Comment(id = 1, text = "Комментарий 1", username = "User1"),
                    Comment(id = 2, text = "Комментарий 2", username = "User2")
                ),
                onLikeClick = {
                    // Реализуйте вызов API для лайка здесь, если необходимо
                },
                onCommentSubmit = { comment ->
                    // Реализуйте вызов API для отправки комментария здесь
                    println("Новый комментарий: $comment")
                }
            )
        }
    }
}