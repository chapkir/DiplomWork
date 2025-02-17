import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.diplomwork.frontend.ui.screens.main_screen.MainScreen

@Composable
fun AppNavigation(navController: NavHostController) {

    NavHost( // Эта штука хранит в себе экран который на данный момент отображается

        navController = navController, // Эта штука отвечает за переключения между экранами
        startDestination = "main_screen" // Ее начальное значение это главный экран
    ) {

        // Маршрут для главного экрана
        composable("main_screen") {
            MainScreen(navController = navController)
        }

        // Маршрут для экрана с изображением принимает параметр интовый imageRes
        composable("image_detail_screen/{imageRes}") { backStackEntry ->
            val imageRes = backStackEntry.arguments?.getString("imageRes")?.toIntOrNull() ?: 0
            ImageDetailScreen( // Отображение экрана с изображением ком лайк и тд
                imageRes = imageRes,
                likesCount = 0,
                comments = listOf("Комментарий 1", "Комментарий 2"), // Тут я так понимаю надо из БД черпать комы
                onLikeClick = { /* Добавить обработку лайка */ }
            )
        }
    }
}
