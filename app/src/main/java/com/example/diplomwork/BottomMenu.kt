package com.example.diplomwork

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.diplomwork.ui.theme.ColorForBottomMenu

@Composable
fun BottomMenu() {

    val items = listOf(
        BottomMenuItem.Home,
        BottomMenuItem.Favs,
        BottomMenuItem.Settings
    )

    val selectedItem = remember { mutableStateOf("Home") }

    NavigationBar(
        containerColor = ColorForBottomMenu,
        contentColor = Color.White,
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





