package com.example.diplomwork.ui.navigation

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
import com.example.diplomwork.ui.components.bottom_menu.BottomNavigationBar
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

    val hiddenScreens =
        listOf(
            NavigationItem.ViewPicture.route,
            NavigationItem.Login.route,
            NavigationItem.Register.route
        )

    val showBottomBar = currentRoute != null && hiddenScreens.none { currentRoute.startsWith(it)}
    val topBar = GetTopBars(currentRoute)

    val isDialogOpen = remember { mutableStateOf(false) }
    val openDialog = { isDialogOpen.value = true }

    var shouldRefresh by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { GetTopBars(currentRoute) },
        bottomBar = {
            if (showBottomBar) BottomNavigationBar(
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
            startDestination = if (sessionManager.isLoggedIn()) NavigationItem.Home.route else NavigationItem.Login.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(NavigationItem.Home.route) {
                HomeScreen(
                    onImageClick = { pictureId, imageUrl ->
                        val encodedUrl = Uri.encode(imageUrl)
                        navController.navigate("${NavigationItem.ViewPicture.route}/$pictureId/$encodedUrl") {
                            popUpTo("${NavigationItem.ViewPicture.route}/$pictureId/$encodedUrl") { inclusive = true }
                        }
                    },
                    shouldRefresh = shouldRefresh,
                    onRefreshComplete = { shouldRefresh = false }
                )
            }
            composable(NavigationItem.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(NavigationItem.Profile.route) {
                            popUpTo(NavigationItem.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(NavigationItem.Register.route) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(NavigationItem.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        sessionManager.clearSession()
                        navController.navigate(NavigationItem.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(NavigationItem.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onImageClick = { pictureId, imageUrl ->
                        val encodedUrl = java.net.URLEncoder.encode(imageUrl, "UTF-8")
                        navController.navigate(
                            "${NavigationItem.ViewPicture.route}/$pictureId/$encodedUrl") {
                            popUpTo(
                                "${NavigationItem.ViewPicture.route}/$pictureId/$encodedUrl")
                            { inclusive = true }
                        }
                    }
                )
            }
            composable(
                "${NavigationItem.ViewPicture.route}/{pictureId}/{imageUrl}",
                arguments = listOf(
                    navArgument("pictureId") { type = NavType.LongType },
                    navArgument("imageUrl") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                PictureDetailScreen(
                    pictureId = backStackEntry.arguments?.getLong("pictureId") ?: 0,
                    imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: "",
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToLogin = { navController.navigate(NavigationItem.Login.route) }
                )
            }
            composable(NavigationItem.Posts.route) {
                /* InfoScreen() */
            }

            composable(NavigationItem.Notification.route) {
                /* FavsScreen() */
            }
            composable(NavigationItem.Register.route) {
                RegisterScreen(onCompleteRegistration = {
                    navController.navigate(NavigationItem.Home.route){
                        popUpTo(NavigationItem.Register.route) { inclusive = true }
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
