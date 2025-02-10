package com.example.diplomwork.ui.main_screen.bottom_menu

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.diplomwork.ui.theme.ColorForBottomMenu

@Composable
fun BottomMenu() {

    val items = listOf(
        BottomMenuItem.Home,
        BottomMenuItem.Favs,
        BottomMenuItem.Settings
    )

    val selectedItem = remember { mutableStateOf("Home") }

    val navBarHeight = SystemNavBarHeight()

    NavigationBar(
        containerColor = ColorForBottomMenu,
        contentColor = Color.White,
        modifier = Modifier.height(50.dp + navBarHeight.value)
    )
    {
        items.forEach { item ->

            NavigationBarItem(
                selected = selectedItem.value == item.route,
                onClick = {
                    selectedItem.value = item.route
                },

                icon = {
                    Icon(
                        painter = painterResource(id = item.iconId),
                        contentDescription = null,
                        modifier = Modifier.size(25.dp)
                    )
                },

                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White, // Цвет активного значка
                    unselectedIconColor = Color.Gray, // Цвет неактивного значка
                    selectedTextColor = Color.Blue, // Цвет активного текста
                    unselectedTextColor = Color.Gray, // Цвет неактивного текста
                    indicatorColor = Color.Transparent // Отключает серый овал
                )
            )
        }
    }
}

@Composable
fun SystemNavBarHeight(): MutableState<Dp>
{
    val navBarHeight = remember { mutableStateOf(0.dp) }

    // Используем LocalContext и LocalDensity для получения отступов
    val context = LocalContext.current
    val view = LocalView.current
    val density = LocalDensity.current

    // Используем LaunchedEffect для того, чтобы высота навигационной панели обновлялась
    LaunchedEffect(context) {
        // Получаем rootWindowInsets для системных панелей
        val insets = ViewCompat.getRootWindowInsets(view)
        navBarHeight.value = with(density) {
            // Получаем нижний отступ, который равен высоте навигационной панели
            insets?.getInsets(WindowInsetsCompat.Type.systemBars())?.bottom?.toDp() ?: 0.dp
        }
    }
    return navBarHeight
}





