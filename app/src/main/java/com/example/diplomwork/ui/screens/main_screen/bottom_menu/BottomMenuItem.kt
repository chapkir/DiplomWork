package com.example.diplomwork.frontend.ui.screens.main_screen.bottom_menu

import com.example.diplomwork.R

sealed class BottomMenuItem(
    val route: String,
    val title: String,
    val iconId: Int
) {
    object Home: BottomMenuItem(
        route = "home",
        title =  "",
        iconId = R.drawable.ic_home
    )
    object Favs: BottomMenuItem(
        route = "favs",
        title =  "",
        iconId = R.drawable.ic_favs
    )
    object Settings: BottomMenuItem(
        route = "settings",
        title =  "",
        iconId = R.drawable.ic_settings
    )
    object User: BottomMenuItem(
        route = "user",
        title =  "",
        iconId = R.drawable.ic_user
    )
    object Add: BottomMenuItem(
        route = "add",
        title =  "",
        iconId = R.drawable.ic_add
    )
    object Info: BottomMenuItem(
        route = "info",
        title =  "",
        iconId = R.drawable.ic_info
    )
}