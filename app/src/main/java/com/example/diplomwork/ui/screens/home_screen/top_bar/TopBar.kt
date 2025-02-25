package com.example.diplomwork.ui.screens.home_screen.top_bar

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.diplomwork.R
import com.example.diplomwork.ui.theme.ColorForBottomMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {

    val items = listOf(
        TopBarItem.Search,
        TopBarItem.FilterList,
    )

    TopAppBar(
        title = { Text("Картинки", fontWeight = FontWeight.Bold) },

        modifier = Modifier
            .fillMaxWidth(),
            //.height(TopBarHeight),

        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ColorForBottomMenu, // Фон AppBar
            titleContentColor = Color.White), // Цвет текста

        actions = {

            IconButton(
                onClick = { /* Поиск */ }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = null,
                    modifier = Modifier.size(25.dp)
                )
            }

            IconButton(
                onClick = { /* Фильтры */ }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_filter_list),
                    contentDescription = null,
                    modifier = Modifier.size(25.dp)
                )
            }
        }
    )
}