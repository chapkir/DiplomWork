package com.example.diplomwork.ui.screens.home_screen.content_grid

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
import com.example.diplomwork.model.PinResponse
import com.example.diplomwork.network.ApiClient
import com.example.diplomwork.ui.theme.ColorForBottomMenu
import kotlinx.coroutines.delay

@Composable
fun ContentGrid(
    modifier: Modifier = Modifier,
    onImageClick: (PinResponse) -> Unit,
    shouldRefresh: Boolean = false,
    onRefreshComplete: () -> Unit = {}
) {
    var pins by remember { mutableStateOf<List<PinResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    suspend fun loadPins() {
        try {
            isLoading = true
            pins = ApiClient.apiService.getPins()
            isLoading = false
            error = null
        } catch (e: Exception) {
            error = "Ошибка загрузки данных: ${e.message}"
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadPins()
    }

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            loadPins()
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
            pins.isEmpty() -> {
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
                    contentPadding = PaddingValues(4.dp)
                ) {
                    itemsIndexed(pins) { _, pin ->
                        ImageCard(
                            imageUrl = pin.imageUrl,
                            onClick = { onImageClick(pin) }
                        )
                    }
                }
            }
        }
    }
}