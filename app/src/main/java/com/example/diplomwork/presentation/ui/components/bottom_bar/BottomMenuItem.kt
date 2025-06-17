package com.example.diplomwork.presentation.ui.components.bottom_bar

import com.example.diplomwork.R
import com.example.diplomwork.presentation.ui.navigation.AddContent
import com.example.diplomwork.presentation.ui.navigation.Notification
import com.example.diplomwork.presentation.ui.navigation.OwnProfile
import com.example.diplomwork.presentation.ui.navigation.Screen
import com.example.diplomwork.presentation.ui.navigation.Category
import com.example.diplomwork.presentation.ui.navigation.Spots

sealed class BottomNavItem(
    val route: Screen,
    val icon: Int,
    val label: String,
    val isAddButton: Boolean = false,
) {
    object SpotsScreen : BottomNavItem(
        route = Spots,
        icon = R.drawable.ic_home,
        label = "Spots",
    )

    object CategoriesScreen : BottomNavItem(
        route = Category,
        icon = R.drawable.ic_search,
        label = "Category"
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
        route = OwnProfile,
        icon = R.drawable.ic_user,
        label = "Profile",
    )
}

