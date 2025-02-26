package com.example.diplomwork.ui.screens.profile_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.example.diplomwork.R
import com.example.diplomwork.ui.theme.ColorForBottomMenu


@Composable
fun ProfileScreen(navController: NavHostController, username: String) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Публикации", "Лайки")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorForBottomMenu)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Spacer(Modifier.weight(1f))
            Text(
                text = username,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.weight(1f))
            IconButton(
                onClick = { navController.navigate("login_screen") },
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_login),
                    contentDescription = "Войти",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.weight(1f))
        }
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.background(ColorForBottomMenu),
            contentColor = Color.White,
            containerColor = ColorForBottomMenu
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(text = title, color = Color.White) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> ImageGrid(images = getPublishedImages())
            1 -> ImageGrid(images = getLikedImages())
        }
    }
}

@Composable
fun ImageGrid(images: List<String>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.padding(8.dp),
        contentPadding = PaddingValues(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(images) { imageUrl ->
            ImageItem(imageUrl = imageUrl)
        }
    }
}

@Composable
fun ImageItem(imageUrl: String) {
    AsyncImage(
        model = imageUrl,
        contentDescription = "Изображение",
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.DarkGray),
        contentScale = ContentScale.Crop
    )
}

// Заглушки данных
fun getPublishedImages(): List<String> = List(15) { "https://via.placeholder.com/150?text=Pub$it" }
fun getLikedImages(): List<String> = List(15) { "https://via.placeholder.com/150?text=Like$it" }

