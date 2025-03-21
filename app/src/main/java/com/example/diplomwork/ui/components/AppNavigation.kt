package com.example.diplomwork.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.ui.components.bottom_menu.BottomMenu
import com.example.diplomwork.ui.components.top_bar.GetTopBars
import com.example.diplomwork.ui.screens.add_picture_screen.OpenGalleryAndSavePicture
import com.example.diplomwork.ui.screens.home_screen.HomeScreen
import com.example.diplomwork.ui.screens.picture_detail_screen.PictureDetailScreen
import com.example.diplomwork.ui.screens.login_screen.LoginScreen
import com.example.diplomwork.ui.screens.profile_screen.ProfileScreen
import com.example.diplomwork.ui.screens.registration_screen.RegisterScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val hiddenScreens = listOf("picture_detail", "login_screen", "registration_screen")
    val showBottomBar = currentRoute != null && hiddenScreens.none { currentRoute.startsWith(it)}
    val topBar = GetTopBars(currentRoute)

    val isDialogOpen = remember { mutableStateOf(false) }
    val openDialog = { isDialogOpen.value = true }

    var shouldRefresh by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { GetTopBars(currentRoute) },
        bottomBar = {
            if (showBottomBar) BottomMenu(
                currentRoute = currentRoute ?: "",
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(route) { inclusive = true }
                    }
                },
                onAddClicked = openDialog
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (sessionManager.isLoggedIn()) "home_screen" else "login_screen",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home_screen") {
                HomeScreen(
                    onImageClick = { pictureId, imageUrl ->
                        val encodedUrl = Uri.encode(imageUrl)
                        navController.navigate("picture_detail/$pictureId/$encodedUrl") {
                            popUpTo("picture_detail/$pictureId/$encodedUrl") { inclusive = true }
                        }
                    },
                    shouldRefresh = shouldRefresh,
                    onRefreshComplete = { shouldRefresh = false }
                )
            }
            composable("login_screen") {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("profile_screen") {
                            popUpTo("login_screen") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate("registration_screen") },
                    onNavigateBack = { navController.popBackStack() }
                )
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
                    onImageClick = { pictureId, imageUrl ->
                        val encodedUrl = java.net.URLEncoder.encode(imageUrl, "UTF-8")
                        navController.navigate("picture_detail/$pictureId/$encodedUrl") {
                            popUpTo("picture_detail/$pictureId/$encodedUrl") { inclusive = true }
                        }
                    }
                )
            }
            composable(
                "picture_detail/{pictureId}/{imageUrl}",
                arguments = listOf(
                    navArgument("pictureId") { type = NavType.LongType },
                    navArgument("imageUrl") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                PictureDetailScreen(
                    pictureId = backStackEntry.arguments?.getLong("pictureId") ?: 0,
                    imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: "",
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToLogin = { navController.navigate("login_screen") }
                )
            }
            composable("posts_screen") {
                /* InfoScreen() */
            }

            composable("notice_screen") {
                /* FavsScreen() */
            }
            composable("registration_screen") {
                RegisterScreen(onCompleteRegistration = {
                    navController.navigate("home_screen"){
                        popUpTo("registration_screen") { inclusive = true }
                    }
                })
            }
        }
    }
    if (isDialogOpen.value) {
        OpenGalleryAndSavePicture(
            isDialogOpen = isDialogOpen,
            context = context,
            onRefresh = { shouldRefresh = true }
        )
    }
}
