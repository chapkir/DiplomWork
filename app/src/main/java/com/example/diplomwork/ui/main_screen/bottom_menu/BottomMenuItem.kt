package com.example.diplomwork.ui.main_screen.bottom_menu

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
}