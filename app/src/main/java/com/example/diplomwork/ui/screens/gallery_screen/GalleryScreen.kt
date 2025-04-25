package com.example.diplomwork.ui.screens.gallery_screen

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.diplomwork.ui.components.checkGalleryPermission
import com.example.diplomwork.ui.components.requestGalleryPermission
import com.example.diplomwork.ui.theme.ColorForBackground
import com.example.diplomwork.viewmodel.GalleryViewModel

@Composable
fun GalleryScreen(
    onImageSelected: (Uri) -> Unit,
    onClose: () -> Unit,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val images by viewModel.images.collectAsState()
    val albums by viewModel.albums.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 - Все фото, 1 - Альбомы
    var hasPermission by remember { mutableStateOf(checkGalleryPermission(context)) }

    // Загружаем данные при входе в экран
    LaunchedEffect(Unit) {
        if (hasPermission) {
            viewModel.loadGalleryData()
        } else {
            requestGalleryPermission(context) { granted ->
                hasPermission = granted
                if (granted) viewModel.loadGalleryData()
                else onClose()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onClose() }) {
                Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Галерея", color = Color.White, fontSize = 20.sp)
        }

        // Переключение вкладок
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.Black,
            contentColor = Color.White,
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Все фото", color = Color.White, modifier = Modifier.padding(vertical = 10.dp))
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Альбомы", color = Color.White, modifier = Modifier.padding(vertical = 10.dp))
            }
        }

        // Содержимое экрана
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (hasPermission) {
                when (selectedTab) {
                    0 -> {
                        // Отображение всех фотографий
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(5.dp)
                        ) {
                            items(images) { imageUri ->
                                Image(
                                    painter = rememberAsyncImagePainter(imageUri),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .padding(3.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            onImageSelected(imageUri)
                                        },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    1 -> {
                        // Отображение альбомов
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            items(albums) { album ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.loadImagesFromAlbum(album.id)
                                            selectedTab =
                                                0 // Переключаемся на фото после выбора альбома
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(album.coverUri),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(album.name, color = Color.White, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                // Если нет разрешения
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Для выбора фото необходимо разрешение",
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            requestGalleryPermission(context) { granted ->
                                hasPermission = granted
                                if (granted) viewModel.loadGalleryData()
                                else onClose()
                            }
                        }) {
                            Text("Разрешить")
                        }
                    }
                }
            }
        }
    }
}
