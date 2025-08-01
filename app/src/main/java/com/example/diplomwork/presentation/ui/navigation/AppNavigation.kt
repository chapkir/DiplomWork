package com.example.diplomwork.presentation.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.presentation.ui.components.CustomSnackbarHost
import com.example.diplomwork.presentation.ui.components.bottom_bar.BottomNavigationBar
import com.example.diplomwork.presentation.ui.components.top_bar.GetTopBars
import com.example.diplomwork.presentation.ui.screens.create_content_screens.CreateSpotScreen
import com.example.diplomwork.presentation.ui.screens.create_content_screens.WhatCreateBottomSheet
import com.example.diplomwork.presentation.ui.screens.gallery_screen.GalleryScreen
import com.example.diplomwork.presentation.ui.screens.login_screen.LoginScreen
import com.example.diplomwork.presentation.ui.screens.map_screen.MapScreen
import com.example.diplomwork.presentation.ui.screens.notification_screen.NotificationScreen
import com.example.diplomwork.presentation.ui.screens.pictures_screen.PicturesScreen
import com.example.diplomwork.presentation.ui.screens.posts_screen.PostsScreen
import com.example.diplomwork.presentation.ui.screens.profile_screen.ProfileScreen
import com.example.diplomwork.presentation.ui.screens.registration_screen.RegisterScreen
import com.example.diplomwork.presentation.ui.screens.search_screen.CategoryScreen
import com.example.diplomwork.presentation.ui.screens.search_screen.SearchResultsScreen
import com.example.diplomwork.presentation.ui.screens.search_screen.SearchScreen
import com.example.diplomwork.presentation.ui.screens.settings_screens.ChangePasswordScreen
import com.example.diplomwork.presentation.ui.screens.settings_screens.EditProfileScreen
import com.example.diplomwork.presentation.ui.screens.settings_screens.FeedbackScreen
import com.example.diplomwork.presentation.ui.screens.settings_screens.LicensesScreen
import com.example.diplomwork.presentation.ui.screens.settings_screens.ManagementAccount
import com.example.diplomwork.presentation.ui.screens.settings_screens.SettingsScreen
import com.example.diplomwork.presentation.ui.screens.spot_detail_screen.SpotDetailScreen
import com.example.diplomwork.presentation.ui.screens.spots_screen.SpotsScreen
import com.example.diplomwork.presentation.ui.theme.BgDefault
import com.example.diplomwork.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(navController: NavHostController) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        ?.substringAfterLast('.') ?: ""

    val visibleScreens =
        listOf(
            Spots::class.simpleName,
            Settings::class.simpleName,
            Category::class.simpleName,
            OwnProfile::class.simpleName,
            OtherProfileScreenData::class.simpleName,
            Notification::class.simpleName,
            "search"
        )

    val showBottomBar = currentRoute.let { route ->
        visibleScreens.any { it != null && route.startsWith(it) }
    }

    val coroutineScope = rememberCoroutineScope()

    val whatCreateSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val openSheet = { coroutineScope.launch { whatCreateSheetState.show() } }

    Scaffold(
        topBar = { GetTopBars(currentRoute = currentRoute) },
        bottomBar = {
            if (showBottomBar) BottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(route) { saveState = true }
                    }
                },
                onAddClicked = { openSheet() }
            )
        },
        snackbarHost = { CustomSnackbarHost(snackbarHostState) },
        containerColor =
            when (currentRoute) {
                Login::class.simpleName -> BgDefault
                Register::class.simpleName -> BgDefault
                else -> MaterialTheme.colorScheme.background
            },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (sessionManager.isLoggedIn()) Spots else Login,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable<Pictures> {
                PicturesScreen(
                    onSpotClick = { pictureId ->
                        navController.navigate(SpotDetailScreenData(pictureId))
                    },
                    onProfileClick = { userId, username ->
                        navController.navigate(
                            OtherProfileScreenData(userId, username)
                        )
                    }
                )
            }
            composable<Login> {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Spots) {
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
                    onSpotClick = { pictureId ->
                        navController.navigate(SpotDetailScreenData(pictureId))
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
                    onSpotClick = { pictureId ->
                        navController.navigate(SpotDetailScreenData(pictureId))
                    }
                )
            }

            composable<SpotDetailScreenData> {
                SpotDetailScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onProfileClick = { userId, username ->
                        navController.navigate(
                            OtherProfileScreenData(userId, username)
                        )
                    }
                )
            }

            composable<Spots> {
                SpotsScreen(
                    onSpotClick = { spotId ->
                        navController.navigate(SpotDetailScreenData(spotId))
                    },
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
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            searchNavGraph(navController)

            composable<Notification> {
                NotificationScreen(
                    onProfile = { userId, username ->
                        navController.navigate(OtherProfileScreenData(userId, username))
                    },
                    onNotificationContent = { pictureId ->
                        navController.navigate(SpotDetailScreenData(pictureId))
                    }
                )
            }

            settingsNavGraph(navController)

            composable<Register> {
                RegisterScreen(
                    onCompleteRegistration = {
                        navController.navigate(Spots) {
                            popUpTo(Register) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<Map> {
                MapScreen(
                    onLocationSelected = { spotName, latitude, longitude ->
                        val data = GalleryScreenData(
                            spotName = spotName,
                            latitude = latitude,
                            longitude = longitude
                        )
                        navController.navigate(data)
                    },
                    onBack = { navController.popBackStack() },
                )
            }

            composable<GalleryScreenData> { backStackEntry ->
                val galleryData = backStackEntry.toRoute<GalleryScreenData>()

                GalleryScreen(
                    onImageSelected = { selectedImages ->
                        val createSpotData = CreateSpotScreenData(
                            spotName = galleryData.spotName,
                            latitude = galleryData.latitude,
                            longitude = galleryData.longitude,
                            imageUrls = selectedImages
                        )
                        navController.navigate(createSpotData)
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable<CreateSpotScreenData> { backStackEntry ->
                val createSpotScreenData = backStackEntry.toRoute<CreateSpotScreenData>()
                CreateSpotScreen(
                    createSpotScreenData = createSpotScreenData,
                    onContentAdded = {
                        navController.popBackStack()
                        navController.popBackStack()
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() },
                )
            }
        }

        if (whatCreateSheetState.isVisible) {
            WhatCreateBottomSheet(
                onAddContent = {
                    coroutineScope.launch { whatCreateSheetState.hide() }
                    navController.navigate(Map)
                },
                onDismiss = {
                    coroutineScope.launch { whatCreateSheetState.hide() }
                },
                sheetState = whatCreateSheetState
            )
        }
    }
}

fun NavGraphBuilder.settingsNavGraph(navController: NavController) {
    navigation(
        startDestination = "settings",
        route = "settings_root"
    ) {
        composable<Settings> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("settings_root")
            }
            val viewModel: SettingsViewModel = hiltViewModel(parentEntry)

            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEditProfileClick = {
                    navController.navigate(EditProfile) {
                        popUpTo(EditProfile)
                    }
                },
                onAccountManagementClick = {
                    navController.navigate(ManagementAccount) {
                        popUpTo(ManagementAccount) { inclusive = false }
                    }
                },
                onPrivacyClick = { }, // TODO
                onLogoutClick = {
                    navController.navigate(Login) {
                        popUpTo(Login) { inclusive = true }
                    }
                },
                onFeedbackClick = {
                    navController.navigate("feedback") {
                        popUpTo(Licenses) { inclusive = false }
                    }
                },
                onHelpCenterClick = { }, // TODO
                onLicensesClick = {
                    navController.navigate(Licenses) {
                        popUpTo(Licenses) { inclusive = false }
                    }
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

        composable<Licenses> {
            LicensesScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable("changePassword") {
            ChangePasswordScreen(
                onBack = { navController.popBackStack() },
                isPasswordChanged = {
                    navController.navigate(OwnProfile) {
                        popUpTo(OwnProfile) { inclusive = false }
                    }
                }
            )
        }

        composable("feedback") { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("settings_root")
            }
            val viewModel: SettingsViewModel = hiltViewModel(parentEntry)

            FeedbackScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable<ManagementAccount> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("settings_root")
            }
            val viewModel: SettingsViewModel = hiltViewModel(parentEntry)

            ManagementAccount(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onPasswordChange = {
                    navController.navigate("changePassword") {
                        popUpTo("changePassword") { inclusive = false }
                    }
                },
                isAccountDeleted = {
                    navController.navigate(Login) {
                        popUpTo(Login) { inclusive = true }
                    }
                }
            )
        }
    }
}

fun NavGraphBuilder.searchNavGraph(navController: NavController) {
    navigation(
        startDestination = "search",
        route = "search_root"
    ) {
        composable<Category> {
            CategoryScreen(
                onSearchBarClick = {
                    navController.navigate("search") {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("search") {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onSpotClick = { spotId ->
                    navController.navigate(SpotDetailScreenData(spotId))
                }
            )
        }

        composable(
            route = "search_results/{query}",
            arguments = listOf(navArgument("query") { type = NavType.StringType })
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query").orEmpty()
            SearchResultsScreen(
                query = query,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
