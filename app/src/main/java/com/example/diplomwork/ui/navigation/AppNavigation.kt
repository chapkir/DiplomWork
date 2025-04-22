package com.example.diplomwork.ui.navigation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.diplomwork.ui.screens.create_content_screens.CreateContentScreen
import com.example.diplomwork.ui.screens.create_content_screens.WhatCreateBottomSheet
import com.example.diplomwork.ui.screens.gallery_screen.GalleryScreen
import com.example.diplomwork.ui.screens.pictures_screen.PicturesScreen
import com.example.diplomwork.ui.screens.login_screen.LoginScreen
import com.example.diplomwork.ui.screens.notification_screen.NotificationScreen
import com.example.diplomwork.ui.screens.picture_detail_screen.PictureDetailScreen
import com.example.diplomwork.ui.screens.posts_screen.PostsScreen
import com.example.diplomwork.ui.screens.profile_screen.ProfileScreen
import com.example.diplomwork.ui.screens.registration_screen.RegisterScreen
import com.example.diplomwork.ui.screens.settings_screens.EditProfileScreen
import com.example.diplomwork.ui.screens.settings_screens.SettingsScreen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(navController: NavHostController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        ?.substringAfterLast('.') ?: ""

    val hiddenScreens =
        listOf(
            PictureDetailScreenData::class.simpleName,
            Login::class.simpleName,
            Register::class.simpleName,
            GalleryScreenData::class.simpleName,
            CreateContentScreenData::class.simpleName
        )

    val showBottomBar = currentRoute.let { route ->
        hiddenScreens.none { it != null && route.startsWith(it) }
    }

    var shouldRefresh by remember { mutableStateOf(false) }
    var lastRefreshTimestamp by remember { mutableLongStateOf(0L) }
    var searchQuery by remember { mutableStateOf("") }
    var lastSearchJob by remember { mutableStateOf<Job?>(null) }
    var isSearchActive by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val openSheet = {
        coroutineScope.launch { sheetState.show() }
    }



    // Функция для задержки поиска
    fun performSearch(query: String) {
        lastSearchJob?.cancel()
        lastSearchJob = coroutineScope.launch {
            delay(400) // Задержка в 400 мс
            searchQuery = query
            isSearchActive = query.isNotEmpty()
            Log.d("AppNavigation", "Выполняется поиск по запросу: $query")
            if (currentRoute != Pictures::class.simpleName) {
                navController.navigate(Pictures)
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
                //onRefresh = triggerRefresh
            )
        },
        bottomBar = {
            if (showBottomBar) BottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    if (route == Pictures && searchQuery.isNotEmpty()) {
                        searchQuery = ""
                        isSearchActive = false
                    }
                    navController.navigate(route) {
                        popUpTo(route) { inclusive = false }
                    }
                },
                onAddClicked = { openSheet() }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (sessionManager.isLoggedIn()) Posts else Login,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable<Pictures> {
                PicturesScreen(
                    onImageClick = { pictureId, imageUrl ->
                        navController.navigate(PictureDetailScreenData(pictureId, imageUrl))
                    },
                    shouldRefresh = shouldRefresh,
                    onRefreshComplete = { shouldRefresh = false },
                    searchQuery = searchQuery
                )
            }
            composable<Login> {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(OwnProfile) {
                            popUpTo(Login) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Register) }
                )
            }
            composable<OtherProfileScreenData> {
                ProfileScreen(
                    onSettingsClick = {
                        navController.navigate(Settings) {
                            popUpTo(Settings) { inclusive = false }
                        }
                    },
                    onBack = { navController.popBackStack() },
                    onImageClick = { pictureId, imageUrl ->
                        navController.navigate(PictureDetailScreenData(pictureId, imageUrl))
                    }
                )
            }

            composable<OwnProfile> {
                ProfileScreen(
                    onSettingsClick = {
                        navController.navigate(Settings) {
                            popUpTo(Settings) { inclusive = false }
                        }
                    },
                    onBack = { navController.popBackStack() },
                    onImageClick = { pictureId, imageUrl ->
                        navController.navigate(PictureDetailScreenData(pictureId, imageUrl))
                    }
                )
            }
            composable<EditProfile> {
                EditProfileScreen(
                    onBack = { navController.popBackStack() },
                    onEditSuccess = {
                        navController.navigate(OwnProfile) {
                            popUpTo(EditProfile) { inclusive = true }
                        }
                    }
                )
            }
            composable<PictureDetailScreenData> { backStackEntry ->
                val pictureDetailScreenData =
                    backStackEntry.toRoute<PictureDetailScreenData>()

                PictureDetailScreen(
                    pictureDetailScreenData,
                    onNavigateBack = { navController.popBackStack() },
                    onProfileClick = { userId, username ->
                        navController.navigate(
                            OtherProfileScreenData(userId, username)
                        )
                    }
                )
            }
            composable<Posts> {
                PostsScreen(
                    onProfileClick = { userId, username ->
                        navController.navigate(
                            OtherProfileScreenData(userId, username)
                        )
                    }
                )
            }

            composable<Notification> {
                NotificationScreen(
                    onProfile = { userId, username ->
                        navController.navigate(OtherProfileScreenData(userId, username))
                    },
                    onNotificationContent = { pictureId, imageUrl ->
                        navController.navigate(PictureDetailScreenData(pictureId, imageUrl))
                    }
                )
            }

            composable<Settings> {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onEditProfileClick = {
                        navController.navigate(EditProfile) {
                            popUpTo(EditProfile)
                        }
                    },
                    onAccountManagementClick = {},
                    onPrivacyClick = {},
                    onLogoutClick = {
                        sessionManager.clearSession()
                        navController.navigate(Login) {
                            popUpTo(Login) { inclusive = true }
                        }
                    },
                    onHelpCenterClick = {},
                    onPrivacyPolicyClick = {}
                )
            }

            composable<Register> {
                RegisterScreen(
                    onCompleteRegistration = {
                        navController.navigate(Pictures) {
                            popUpTo(Register) { inclusive = true }
                        }
                    })
            }
            composable<GalleryScreenData> { backStackEntry ->
                val galleryScreenData = backStackEntry.toRoute<GalleryScreenData>()
                val whatContentCreate = galleryScreenData.whatContentCreate

                GalleryScreen(
                    onImageSelected = { uri ->
                        navController.navigate(
                            CreateContentScreenData(
                                uri.toString(),
                                whatContentCreate
                            )
                        )
                    },
                    onClose = { navController.popBackStack() }
                )
            }

            composable<CreateContentScreenData> { backStackEntry ->
                val createContentScreenData =
                    backStackEntry.toRoute<CreateContentScreenData>()

                CreateContentScreen(
                    createContentScreenData = createContentScreenData,
                    onContentAdded = { navController.navigate(Pictures) },
                    onBack = { navController.popBackStack() }
                )
            }


        }

        if (sheetState.isVisible) {
            WhatCreateBottomSheet(
                onAddContent = { whatContentCreate ->
                    coroutineScope.launch { sheetState.hide() }
                    navController.navigate(GalleryScreenData(whatContentCreate = whatContentCreate))
                },
                onDismiss = {
                    coroutineScope.launch { sheetState.hide() }
                },
                sheetState = sheetState
            )
        }
    }
}
