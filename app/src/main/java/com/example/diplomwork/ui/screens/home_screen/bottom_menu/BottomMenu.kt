package com.example.diplomwork.ui.screens.home_screen.bottom_menu

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
import com.example.diplomwork.R
import com.example.diplomwork.system_settings.SystemInsetHeight
import com.example.diplomwork.ui.theme.ColorForBottomMenu
import com.example.diplomwork.ui.theme.Dimens.BottomMenuHeight

@Composable
fun BottomMenu(navController: NavHostController) {
    NavigationBar(
        containerColor = ColorForBottomMenu,
        contentColor = Color.White,
        modifier = Modifier
            .height(
                BottomMenuHeight +
                        SystemInsetHeight(WindowInsetsCompat.Type.navigationBars()).value
            )
    ) {
        // Главная
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("home_screen") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = "Главная",
                    modifier = Modifier.size(25.dp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )

        // Информация
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("info_screen") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_info),
                    contentDescription = "Информация",
                    modifier = Modifier.size(25.dp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )

        // Добавить (добавление нового поста, например)
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("add_screen") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Добавить",
                    modifier = Modifier.size(25.dp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )

        // Избранное
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("favs_screen") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_favs),
                    contentDescription = "Избранное",
                    modifier = Modifier.size(25.dp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )

        // Профиль (переход на экран авторизации)
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("login_screen") },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_user),
                    contentDescription = "Профиль",
                    modifier = Modifier.size(25.dp)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
    }
}








