package com.example.diplomwork.ui.components.top_bar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomwork.R
import com.example.diplomwork.ui.navigation.AddContent
import com.example.diplomwork.ui.navigation.Home
import com.example.diplomwork.ui.navigation.Login
import com.example.diplomwork.ui.navigation.Notification
import com.example.diplomwork.ui.navigation.Posts
import com.example.diplomwork.ui.navigation.Profile
import com.example.diplomwork.ui.navigation.Screen
import com.example.diplomwork.ui.theme.ColorForBottomMenu

@Composable
fun GetTopBars(currentRoute: String?) {
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(currentRoute) {
        isSearching = false
        searchQuery = ""
    }

    if (isSearching) {
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onCloseSearch = {
                isSearching = false
                searchQuery = ""
            }
        )
    } else {
        when (currentRoute) {
            Home::class.simpleName -> CustomTopBar(
                title = "Лента картинок",
                icon = R.drawable.ic_search,
                contentDescription = "Search",
                onIconClick = { isSearching = true }
            )

            Posts::class.simpleName -> CustomTopBar(title = "Посты")
            AddContent::class.simpleName -> CustomTopBar(title = "Добавить")
            Notification::class.simpleName -> CustomTopBar(title = "Уведомления")
            Profile::class.simpleName -> CustomTopBar(title = "Профиль")
            Login::class.simpleName -> CustomTopBar(title = "Авторизация")
            else -> {}
        }
    }
}

@Composable
fun CustomTopBar(
    title: String,
    icon: Int = 0,
    contentDescription: String = "",
    onIconClick: () -> Unit = {}
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorForBottomMenu)
            .padding(top = statusBarHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != 0) Spacer(Modifier.weight(2f))
        else Spacer(Modifier.weight(1f))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 11.dp, bottom = 11.dp)
        )
        Spacer(Modifier.weight(1f))
        if (icon != 0) {
            IconButton(onClick = { onIconClick() })
            {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = contentDescription,
                    modifier = Modifier.size(17.dp),
                    tint = Color.White
                )
            }
        } else return
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorForBottomMenu)
            .padding(top = statusBarHeight + 8.dp, bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onCloseSearch) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_left),
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(29.dp)
            )
        }
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.height(51.dp).width(251.dp),
            placeholder = { Text("Поиск", color = Color.Gray, fontSize = 15.sp) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Gray,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
        IconButton(onClick = {}) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = "Search",
                tint = Color.White,
                modifier = Modifier.size(17.dp),
            )
        }
    }
}

