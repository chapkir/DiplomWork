package com.example.diplomwork.ui.components.bottom_menu

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
import com.example.diplomwork.R
import com.example.diplomwork.system_settings.systemInsetHeight
import com.example.diplomwork.ui.navigation.Screen
import com.example.diplomwork.ui.theme.ColorForBottomMenu
import com.example.diplomwork.ui.theme.Dimens.BottomMenuHeight

data class BottomNavItem(
    val route: String,
    val icon: Int,
    val label: String,
    val isAddButton: Boolean = false
)

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onAddClicked: () -> Unit
) {

    val items = listOf(
        BottomNavItem(Screen.HOME.name, R.drawable.ic_home, "Home"),
        BottomNavItem(Screen.POSTS.name, R.drawable.ic_picture, "Posts"),
        BottomNavItem(Screen.ADD_CONTENT.name, R.drawable.ic_add, "Add", isAddButton = true),
        BottomNavItem(Screen.NOTIFICATION.name, R.drawable.ic_bell, "Notice"),
        BottomNavItem(Screen.PROFILE.name, R.drawable.ic_user, "Profile")
    )

    NavigationBar(
        containerColor = ColorForBottomMenu,
        contentColor = Color.White,
        modifier = Modifier.height(
            BottomMenuHeight + systemInsetHeight(WindowInsetsCompat.Type.navigationBars()).value
        )
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route

            val size = animateDpAsState(
                targetValue = if (isSelected) 27.dp else 25.dp,
                animationSpec = tween(durationMillis = 300)
            ).value

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected){
                        if (item.isAddButton) {
                            onAddClicked()
                        } else {
                            onNavigate(item.route)
                        }
                    }
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













