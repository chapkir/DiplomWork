package com.example.diplomwork.ui.navigation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(navController: NavHostController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        ?.substringAfterLast('.') ?: ""

    val hiddenScreens =
        listOf(
            ViewPictureDetailScreenData::class.simpleName,
            Login::class.simpleName,
            Register::class.simpleName
        )

    val showBottomBar = currentRoute?.let { route ->
        hiddenScreens.none { it != null && route.startsWith(it) }
    } ?: true

    val isDialogOpen = remember { mutableStateOf(false) }
    val openDialog = { isDialogOpen.value = true }

    var shouldRefresh by remember { mutableStateOf(false) }
    var lastRefreshTimestamp by remember { mutableStateOf(0L) }
    var searchQuery by remember { mutableStateOf("") }
    var lastSearchJob by remember { mutableStateOf<Job?>(null) }
    var isSearchActive by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val triggerRefresh = {
        shouldRefresh = true
        lastRefreshTimestamp = System.currentTimeMillis()
        Log.d("AppNavigation", "Запрошено обновление главного экрана в $lastRefreshTimestamp")
        Unit
    }

    // Функция для задержки поиска
    fun performSearch(query: String) {
        lastSearchJob?.cancel()
        lastSearchJob = coroutineScope.launch {
            delay(400) // Задержка в 400 мс
            searchQuery = query
            isSearchActive = query.isNotEmpty()
            Log.d("AppNavigation", "Выполняется поиск по запросу: $query")
            if (currentRoute != Home::class.simpleName) {
                navController.navigate(Home)
            }
        }
    }

    // Обработка системной кнопки назад для закрытия поиска
    BackHandler(enabled = isSearchActive) {
        performSearch("")
    }

    Scaffold(
        topBar = {
            GetTopBars(
                currentRoute = currentRoute,
                onSearch = { query ->
                    performSearch(query)
                },
                onRefresh = triggerRefresh
            )
        },
        bottomBar = {
            if (showBottomBar) BottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    if (route == Home && searchQuery.isNotEmpty()) {
                        searchQuery = ""
                        isSearchActive = false
                    }
                    navController.navigate(route) {
                        popUpTo(route) { inclusive = false }
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
                        navController.navigate(ViewPictureDetailScreenData(pictureId, imageUrl))
                    },
                    shouldRefresh = shouldRefresh,
                    onRefreshComplete = { shouldRefresh = false },
                    searchQuery = searchQuery
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
                        navController.navigate(Login) {
                            popUpTo(Login) { inclusive = true }
                        }
                    },
                    onImageClick = { pictureId, imageUrl ->
                        navController.navigate(ViewPictureDetailScreenData(pictureId, imageUrl)) {
                            popUpTo(Profile) { inclusive = false }
                        }
                    }
                )
            }
            composable<ViewPictureDetailScreenData> { backStackEntry ->
                val viewPictureDetailScreenData =
                    backStackEntry.toRoute<ViewPictureDetailScreenData>()
                PictureDetailScreen(
                    viewPictureDetailScreenData,
                    onNavigateBack = { navController.popBackStack() }
                )
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
