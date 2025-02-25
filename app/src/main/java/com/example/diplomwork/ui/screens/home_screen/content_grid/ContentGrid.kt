package com.example.diplomwork.ui.screens.home_screen.content_grid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.diplomwork.model.Pin
import com.example.diplomwork.network.ApiClient
import com.example.diplomwork.ui.theme.ColorForBottomMenu

@Composable
fun ContentGrid(modifier: Modifier = Modifier, onImageClick: (String) -> Unit) {
    var pins by remember { mutableStateOf<List<Pin>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            pins = ApiClient.apiService.getPins()
            println("Получено пинов: ${pins.size}")
        } catch (e: Exception) {
            e.printStackTrace()
            pins = emptyList()
        }
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = modifier
            .background(ColorForBottomMenu),
        contentPadding = PaddingValues(8.dp)
    ) {
        itemsIndexed(pins) { index, pin ->
            ImageCard(
                imageUrl = pin.imageUrl,
                onClick = { onImageClick(pin.imageUrl) }
            )
        }
    }
}