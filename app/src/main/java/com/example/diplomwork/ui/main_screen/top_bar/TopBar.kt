package com.example.diplomwork.ui.main_screen.top_bar

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.diplomwork.R
import com.example.diplomwork.ui.main_screen.bottom_menu.BottomMenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {

    val items = listOf(
        TopBarItem.Search,
        TopBarItem.FilterList,
    )

    TopAppBar(
        title = { Text("Моя Галерея", fontWeight = FontWeight.Bold) },
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