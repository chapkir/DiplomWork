package com.example.diplomwork.ui.screens.home_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.diplomwork.model.PictureResponse
import com.example.diplomwork.network.ApiClient
import com.example.diplomwork.ui.theme.ColorForBottomMenu

@Composable
fun ContentGrid(
    modifier: Modifier = Modifier,
    onImageClick: (PictureResponse) -> Unit,
    shouldRefresh: Boolean = false,
    onRefreshComplete: () -> Unit = {}
) {
    var pictures by remember { mutableStateOf<List<PictureResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    suspend fun loadPictures() {
        try {
            isLoading = true
            pictures = ApiClient.apiService.getPictures()
            isLoading = false
            error = null
        } catch (e: Exception) {
            error = "Ошибка загрузки данных: ${e.message}"
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadPictures()
    }

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            loadPictures()
            onRefreshComplete()
        }
    }

    Box(
        modifier = modifier.background(ColorForBottomMenu),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = Color.White
                )
            }
            error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = error ?: "",
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
            pictures.isEmpty() -> {
                Text(
                    text = "Нет доступных пинов",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    //contentPadding = PaddingValues(0.dp, 4.dp, 4.dp, 4.dp)
                ) {
                    itemsIndexed(pictures) { _, picture ->
                        PictureCard(
                            imageUrl = picture.imageUrl,
                            onClick = { onImageClick(picture) }
                        )
                    }
                }
            }
        }
    }
}