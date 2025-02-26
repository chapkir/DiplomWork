package com.example.diplomwork.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.diplomwork.ui.screens.home_screen.HomeScreen
import com.example.diplomwork.ui.screens.home_screen.bottom_menu.BottomMenu
import com.example.diplomwork.ui.screens.home_screen.top_bar.getTopBarForScreen
import com.example.diplomwork.ui.screens.image_detail_screen.ImageDetailScreen
import com.example.diplomwork.ui.screens.login_screen.LoginScreen
import com.example.diplomwork.ui.screens.profile_screen.ProfileScreen


@Composable
fun AppNavigation(navController: NavHostController) {
    val screensWithBottomBar = listOf(
        "home_screen", "info_screen", "add_screen",
        "favs_screen", "profile_screen", "login_screen"
    )
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val topBar = getTopBarForScreen(currentRoute)

    Scaffold(
        topBar = {
            topBar()
        },
        bottomBar = {
            if (currentRoute in screensWithBottomBar) {
                BottomMenu(navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home_screen",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home_screen") {
                HomeScreen(navController)
            }

            composable("image_detail_screen?imageUrl={imageUrl}") { backStackEntry ->
                val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
                ImageDetailScreen(
                    imageUrl = imageUrl,
                    initialLikesCount = 0,
                    initialComments = listOf(
                    ),
                    onLikeClick = {
                    },
                    onCommentSubmit = { comment ->
                        println("Новый комментарий: $comment")
                    }
                )
            }
            composable("home_screen") { HomeScreen(navController) }

            composable("info_screen") { /* InfoScreen(navController) */ }
            composable("add_screen") { /* AddScreen(navController) */ }
            composable("favs_screen") { /* FavsScreen(navController) */ }

            composable("profile_screen") {
                ProfileScreen(navController = navController,
                    username = "Имя пользователя")
            }

            composable("login_screen") {
                LoginScreen(onLoginClick = { login, password ->
                    // Здесь надо добавить логику аутентификации
                    println("Вход: $login, $password")
                }, navController = navController)
            }
        }
    }
}
