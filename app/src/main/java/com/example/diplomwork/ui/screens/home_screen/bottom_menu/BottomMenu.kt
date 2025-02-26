package com.example.diplomwork.ui.screens.home_screen.bottom_menu

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.diplomwork.R
import com.example.diplomwork.system_settings.SystemInsetHeight
import com.example.diplomwork.ui.theme.ColorForBottomMenu
import com.example.diplomwork.ui.theme.Dimens.BottomMenuHeight

@Composable
fun BottomMenu(navController: NavHostController) {

    val items = listOf(
        BottomNavItem("home_screen", R.drawable.ic_home, "Главная"),
        BottomNavItem("info_screen", R.drawable.ic_info, "Информация"),
        BottomNavItem("add_screen", R.drawable.ic_add, "Добавить"),
        BottomNavItem("favs_screen", R.drawable.ic_favs, "Избранное"),
        BottomNavItem("profile_screen", R.drawable.ic_user, "Профиль")
    )

    NavigationBar(
        containerColor = ColorForBottomMenu,
        contentColor = Color.White,
        modifier = Modifier.height(
            BottomMenuHeight + SystemInsetHeight(WindowInsetsCompat.Type.navigationBars()).value
        )
    ) {
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

        items.forEach { item ->
            val isSelected = currentRoute == item.route

            val size = animateDpAsState(
                targetValue = if (isSelected) 27.dp else 25.dp,
                animationSpec = tween(durationMillis = 300)
            ).value

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) navController.navigate(item.route)
                },
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.label,
                        modifier = Modifier.size(size)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White, // Цвет активного значка
                    unselectedIconColor = Color.Gray, // Цвет неактивного значка
                    selectedTextColor = Color.Blue, // Цвет активного текста
                    unselectedTextColor = Color.Gray, // Цвет неактивного текста
                    indicatorColor = Color.Transparent // Отключает серый овал
                ),
                alwaysShowLabel = false
            )
        }
    }
}

data class BottomNavItem(val route: String, val icon: Int, val label: String)











