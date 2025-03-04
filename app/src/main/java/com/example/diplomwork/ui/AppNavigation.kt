package com.example.diplomwork.ui

import android.net.Uri
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
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = currentRoute != null && !currentRoute.startsWith("image_detail")
    val topBar = getTopBarForScreen(currentRoute)

    Scaffold(
        topBar = { topBar() },
        bottomBar = {
                if (showBottomBar) BottomMenu(
                    currentRoute = currentRoute ?: "",
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(route) { inclusive = true }
                        }
                    })
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (sessionManager.isLoggedIn()) "home_screen" else "login_screen",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home_screen") {
                HomeScreen(
                    onImageClick = { pinId, imageUrl ->
                        val encodedUrl = Uri.encode(imageUrl)
                        navController.navigate("image_detail/$pinId/$encodedUrl") {
                            popUpTo("image_detail/$pinId/$encodedUrl") { inclusive = true }
                        }
                    })
            }
            composable("login_screen") {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("profile_screen") {
                            popUpTo("login_screen") { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() })
            }
            composable("profile_screen") {
                ProfileScreen(
                    onLogout = {
                        sessionManager.clearSession()
                        navController.navigate("home_screen") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate("login_screen") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onImageClick = { pinId, imageUrl ->
                        val encodedUrl = java.net.URLEncoder.encode(imageUrl, "UTF-8")
                        navController.navigate("image_detail/$pinId/$encodedUrl") {
                            popUpTo("image_detail/$pinId/$encodedUrl") { inclusive = true }
                        }
                    }
                )
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
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToLogin = { navController.navigate("login_screen") }
                )
            }
            composable("info_screen") {
                /* InfoScreen() */
            }
            composable("add_screen") {
                /* AddScreen() */
            }
            composable("favs_screen") {
                /* FavsScreen() */
            }
        }
    }
}
