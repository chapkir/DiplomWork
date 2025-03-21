package com.example.diplomwork.ui.navigation

enum class Screen {
    SPLASH, // TODO Экран загрузки
    LOGIN,
    REGISTER,
    HOME, // or PICTURES
    POSTS, // TODO Posts screen
    ADD_CONTENT,
    CREATE_PICTURE,
    CREATE_POST,
    NOTIFICATION,
    PROFILE,
    VIEW_PICTURE,
    VIEW_POST
}

sealed class NavigationItem(val route: String) {
    object Splash : NavigationItem(Screen.SPLASH.name)
    object Login : NavigationItem(Screen.LOGIN.name)
    object Register : NavigationItem(Screen.REGISTER.name)
    object Home : NavigationItem(Screen.HOME.name)
    object Posts : NavigationItem(Screen.POSTS.name)
    object AddContent : NavigationItem(Screen.ADD_CONTENT.name)
    object CreatePicture : NavigationItem(Screen.CREATE_PICTURE.name)
    object CreatePost : NavigationItem(Screen.CREATE_POST.name)
    object Notification : NavigationItem(Screen.NOTIFICATION.name)
    object Profile : NavigationItem(Screen.PROFILE.name)
    object ViewPicture : NavigationItem(Screen.VIEW_PICTURE.name)
    object ViewPost : NavigationItem(Screen.VIEW_POST.name)
}