package com.example.diplomwork.presentation.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.presentation.ui.components.bottom_menu.BottomNavigationBar
import com.example.diplomwork.presentation.ui.components.top_bar.GetTopBars
import com.example.diplomwork.presentation.ui.screens.create_content_screens.CreateContentScreen
import com.example.diplomwork.presentation.ui.screens.create_content_screens.WhatCreateBottomSheet
import com.example.diplomwork.presentation.ui.screens.gallery_screen.GalleryScreen
import com.example.diplomwork.presentation.ui.screens.login_screen.LoginScreen
import com.example.diplomwork.presentation.ui.screens.map_screen.MapScreen
import com.example.diplomwork.presentation.ui.screens.notification_screen.NotificationScreen
import com.example.diplomwork.presentation.ui.screens.picture_detail_screen.PictureDetailScreen
import com.example.diplomwork.presentation.ui.screens.pictures_screen.PicturesScreen
import com.example.diplomwork.presentation.ui.screens.posts_screen.PostsScreen
import com.example.diplomwork.presentation.ui.screens.profile_screen.ProfileScreen
import com.example.diplomwork.presentation.ui.screens.registration_screen.RegisterScreen
import com.example.diplomwork.presentation.ui.screens.search_screen.SearchScreen
import com.example.diplomwork.presentation.ui.screens.settings_screens.EditProfileScreen
import com.example.diplomwork.presentation.ui.screens.settings_screens.SettingsScreen
import com.example.diplomwork.presentation.ui.screens.spots_screen.SpotsScreen
import com.example.diplomwork.presentation.ui.theme.BgDefault
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
            Gallery::class.simpleName,
            CreateContentScreenData::class.simpleName,
            EditProfile::class.simpleName,
        )

    val showBottomBar = currentRoute.let { route ->
        hiddenScreens.none { it != null && route.startsWith(it) }
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
                        popUpTo(route) { inclusive = false }
                    }
                },
                onAddClicked = { openSheet() }
            )
        },
        containerColor =
            when (currentRoute) {
                Login::class.simpleName -> BgDefault
                Register::class.simpleName -> BgDefault
                else -> BgDefault
            },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (sessionManager.isLoggedIn()) Spots else Login,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable<Pictures> {
                PicturesScreen(
                    onImageClick = { pictureId ->
                        navController.navigate(PictureDetailScreenData(pictureId))
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
                    onImageClick = { pictureId ->
                        navController.navigate(PictureDetailScreenData(pictureId))
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
                    onImageClick = { pictureId ->
                        navController.navigate(PictureDetailScreenData(pictureId))
                    },
                    onMapOpen = {
                        navController.navigate(Map)
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

            composable<PictureDetailScreenData> {
                PictureDetailScreen(
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
                    onImageClick = { pictureId ->
                        navController.navigate(PictureDetailScreenData(pictureId))
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

            composable<Map> {
                MapScreen()
            }

            composable<Search> {
                SearchScreen()
            }

            composable<Notification> {
                NotificationScreen(
                    onProfile = { userId, username ->
                        navController.navigate(OtherProfileScreenData(userId, username))
                    },
                    onNotificationContent = { pictureId ->
                        navController.navigate(PictureDetailScreenData(pictureId!!))
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
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable<Gallery> {
                GalleryScreen(
                    onImageSelected = { uri ->
                        navController.navigate(
                            CreateContentScreenData(
                                uri.toString()
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
                    onContentAdded = {
                        navController.popBackStack()
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }


        }

        if (whatCreateSheetState.isVisible) {
            WhatCreateBottomSheet(
                onAddContent = {
                    coroutineScope.launch { whatCreateSheetState.hide() }
                    navController.navigate(Gallery)
                },
                onDismiss = {
                    coroutineScope.launch { whatCreateSheetState.hide() }
                },
                sheetState = whatCreateSheetState
            )
        }
    }
}
