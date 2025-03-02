package com.example.diplomwork.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.ui.screens.home_screen.HomeScreen
import com.example.diplomwork.ui.screens.home_screen.bottom_menu.BottomMenu
import com.example.diplomwork.ui.screens.home_screen.top_bar.getTopBarForScreen
import com.example.diplomwork.ui.screens.image_detail_screen.ImageDetailScreen
import com.example.diplomwork.ui.screens.login_screen.LoginScreen
import com.example.diplomwork.ui.screens.profile_screen.ProfileScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val screensWithBottomBar = listOf(
        "home_screen", "info_screen", "add_screen",
        "favs_screen", "profile_screen", "login_screen"
    )
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = currentRoute != null &&
            !currentRoute.startsWith("image_detail")
    val topBar = getTopBarForScreen(currentRoute)

    Scaffold(
        topBar = {
            topBar()
        },
        bottomBar = {
            if (showBottomBar) {
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

            composable("login_screen") {
                LoginScreen(navController)
            }

            composable("profile_screen") {
                ProfileScreen(navController)
            }

            composable(
                "image_detail/{pinId}/{imageUrl}",
                arguments = listOf(
                    navArgument("pinId") { type = NavType.LongType },
                    navArgument("imageUrl") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                ImageDetailScreen(
                    pinId = backStackEntry.arguments?.getLong("pinId") ?: 0,
                    imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: "",
                    navController = navController
                )
            }

            composable("info_screen") { /* InfoScreen(navController) */ }
            composable("add_screen") { /* AddScreen(navController) */ }
            composable("favs_screen") { /* FavsScreen(navController) */ }
        }
    }
}
