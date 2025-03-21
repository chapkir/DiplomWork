package com.example.diplomwork.ui.components.top_bar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.diplomwork.ui.theme.ColorForBottomMenu
import com.example.diplomwork.ui.theme.Dimens.TopBarHeight

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
            "home_screen" -> CustomTopBar(
                title = "Лента картинок",
                icon = R.drawable.ic_search,
                contentDescription = "Search",
                onIconClick = { isSearching = true }
            )

            "posts_screen" -> CustomTopBar(title = "Посты")
            "add_screen" -> CustomTopBar(title = "Добавить")
            "notice_screen" -> CustomTopBar(title = "Уведомления")
            "profile_screen" -> CustomTopBar(title = "Профиль")
            "login_screen" -> CustomTopBar(title = "Авторизация")
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorForBottomMenu)
            //.padding(top = SystemInsetHeight(WindowInsetsCompat.Type.statusBars()).value)
            .height(TopBarHeight),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != 0) Spacer(Modifier.weight(2f))
        else Spacer(Modifier.weight(1f))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 11.dp)
        )
        Spacer(Modifier.weight(1f))
        if (icon != 0) {
            IconButton(onClick = { onIconClick() })
            {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = contentDescription,
                    modifier = Modifier.size(15.dp),
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ColorForBottomMenu)
            .padding(10.dp)
            .height(TopBarHeight + 10.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onCloseSearch) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.height(50.dp),
            placeholder = { Text("Поиск", color = Color.Gray, fontSize = 15.sp) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Gray
            )
        )
    }
}

