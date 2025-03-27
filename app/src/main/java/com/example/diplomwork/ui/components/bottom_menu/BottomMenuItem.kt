package com.example.diplomwork.ui.components.bottom_menu

import com.example.diplomwork.R
import com.example.diplomwork.ui.navigation.AddContent
import com.example.diplomwork.ui.navigation.Home
import com.example.diplomwork.ui.navigation.Notification
import com.example.diplomwork.ui.navigation.Posts
import com.example.diplomwork.ui.navigation.Profile
import com.example.diplomwork.ui.navigation.Screen

sealed class BottomNavItem(
    val route: Screen,
    val icon: Int,
    val label: String,
    val isAddButton: Boolean = false
){
    object HomeScreen : BottomNavItem(
        route = Home,
        icon = R.drawable.ic_home,
        label = "Home"
    )

    object PostsScreen : BottomNavItem(
        route = Posts,
        icon = R.drawable.ic_picture,
        label = "Posts"
    )

    object AddContentScreen : BottomNavItem(
        route = AddContent,
        icon = R.drawable.ic_add,
        label = "AddContent",
        isAddButton = true
    )

    object NotificationScreen : BottomNavItem(
        route = Notification,
        icon = R.drawable.ic_bell,
        label = "Notification"
    )

    object ProfileScreen : BottomNavItem(
        route = Profile,
        icon = R.drawable.ic_user,
        label = "Profile"
    )
}

