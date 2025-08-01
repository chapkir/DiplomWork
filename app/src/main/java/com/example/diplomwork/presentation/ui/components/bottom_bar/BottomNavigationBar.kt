package com.example.diplomwork.presentation.ui.components.bottom_bar

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import com.example.diplomwork.presentation.system_settings.systemInsetHeight
import com.example.diplomwork.presentation.ui.navigation.Screen
import com.example.diplomwork.presentation.ui.theme.Dimens.BottomMenuHeight

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (Screen) -> Unit,
    onAddClicked: () -> Unit
) {

    val items = listOf(
        BottomNavItem.SpotsScreen,
        BottomNavItem.CategoriesScreen,
        BottomNavItem.AddContentScreen,
        BottomNavItem.NotificationScreen,
        BottomNavItem.ProfileScreen
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier
                .height(
                    BottomMenuHeight + systemInsetHeight(WindowInsetsCompat.Type.navigationBars()).value
                )
                .padding(top = 1.dp, start = 15.dp, end = 15.dp)
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route::class.simpleName

                val size = animateDpAsState(
                    targetValue = if (isSelected) 26.dp else 24.dp,
                    animationSpec = tween(durationMillis = 300)
                ).value

                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (!isSelected) {
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
                        selectedIconColor = MaterialTheme.colorScheme.onBackground,
                        unselectedIconColor =
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        selectedTextColor = Color.Blue,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    ),
                    alwaysShowLabel = false
                )
            }
        }
    }
}













