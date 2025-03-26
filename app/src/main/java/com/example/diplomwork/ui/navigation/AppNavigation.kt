package com.example.diplomwork.ui.navigation

import android.net.Uri
import android.util.Log
import android.widget.Toast
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.ui.components.bottom_menu.BottomNavigationBar
import com.example.diplomwork.ui.components.top_bar.GetTopBars
import com.example.diplomwork.ui.screens.add_picture_screen.OpenGalleryAndSavePicture
import com.example.diplomwork.ui.screens.home_screen.HomeScreen
import com.example.diplomwork.ui.screens.login_screen.LoginScreen
import com.example.diplomwork.ui.screens.picture_detail_screen.PictureDetailScreen
import com.example.diplomwork.ui.screens.profile_screen.ProfileScreen
import com.example.diplomwork.ui.screens.registration_screen.RegisterScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val currentRoute = navController.
    currentBackStackEntryAsState().value?.destination?.route?.substringAfterLast('.')

    Log.i("appnav", "сейчас путь - $currentRoute")

    val hiddenScreens =
        listOf(
            ViewPictureDetailScreenData::class.simpleName,
            Login::class.simpleName,
            Register::class.simpleName
        )

    val showBottomBar = currentRoute?.let { route ->
        hiddenScreens.all { !route.startsWith(it ?: "") }
    } ?: true

    val isDialogOpen = remember { mutableStateOf(false) }
    val openDialog = { isDialogOpen.value = true }

    var shouldRefresh by remember { mutableStateOf(false) }
    var lastRefreshTimestamp by remember { mutableStateOf(0L) }

    val triggerRefresh = {
        shouldRefresh = true
        lastRefreshTimestamp = System.currentTimeMillis()
        Log.d("AppNavigation", "Запрошено обновление главного экрана в $lastRefreshTimestamp")
    }

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
            startDestination = if (sessionManager.isLoggedIn()) Home else Login,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable<Home> {
                HomeScreen(
                    onImageClick = { pictureId, imageUrl ->
                        val timestamp = System.currentTimeMillis()
                        Log.i("NAVIGATION_DEBUG", "$timestamp - НАЖАТИЕ НА ПИН С ID=$pictureId И URL=$imageUrl")
                        navController.navigate(ViewPictureDetailScreenData(pictureId, imageUrl))
                    },
                    shouldRefresh = shouldRefresh,
                    onRefreshComplete = { shouldRefresh = false }
                )
            }
            composable<Login> {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Profile) {
                            popUpTo(Login) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Register) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Profile> {
                ProfileScreen(
                    onLogout = {
                        sessionManager.clearSession()
                        navController.navigate(Home) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Login) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onImageClick = { pictureId, imageUrl ->
                        navController.navigate(ViewPictureDetailScreenData(pictureId, imageUrl)) {
                            popUpTo(ViewPictureDetailScreenData(pictureId, imageUrl))
                            { inclusive = true }
                        }
                    }
                )
            }
            composable<ViewPictureDetailScreenData> { backStackEntry ->
                val viewPictureDetailScreenData = backStackEntry.toRoute<ViewPictureDetailScreenData>()
                val timestamp = System.currentTimeMillis()
                Log.i("NAVIGATION_DEBUG", "$timestamp - ОТКРЫТИЕ ПИНА С ID=${viewPictureDetailScreenData.pictureId} И URL=${viewPictureDetailScreenData.imageUrl}")
                PictureDetailScreen(
                    viewPictureDetailScreenData,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToLogin = { navController.navigate(Login) }
                )
                Log.i("", "в навигации при отправке " + viewPictureDetailScreenData.pictureId + " " + viewPictureDetailScreenData.imageUrl)
            }
            composable<Posts> {
                /* InfoScreen() */
            }

            composable<Notification> {
                /* FavsScreen() */
            }
            composable<Register> {
                RegisterScreen(onCompleteRegistration = {
                    navController.navigate(Home) {
                        popUpTo(Register) { inclusive = true }
                    }
                })
            }
        }
    }
    if (isDialogOpen.value) {
        OpenGalleryAndSavePicture(
            isDialogOpen = isDialogOpen,
            context = context,
            onRefresh = {
                triggerRefresh()
            }
        )
    }
}
