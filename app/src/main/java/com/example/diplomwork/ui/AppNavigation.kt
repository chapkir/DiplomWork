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
        // Экран главного списка изображений
        composable("main_screen") {
            MainScreen(navController = navController)
        }

        // Экран с подробной информацией об изображении
        composable("image_detail_screen/{imageRes}") { backStackEntry ->
            val imageRes = backStackEntry.arguments?.getString("imageRes")?.toIntOrNull() ?: 0
            ImageDetailScreen(
                imageRes = imageRes,
                likesCount = 0,
                comments = listOf("Комментарий 1", "Комментарий 2"),
                onLikeClick = { /* Добавить обработку лайка */ }
            )
        }
    }
}
