package com.example.diplomwork.ui.screens.home_screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    onImageClick: (Long, String) -> Unit,
    shouldRefresh: Boolean = false,
    onRefreshComplete: () -> Unit = {}
) {
    ContentGrid(
        modifier = Modifier.fillMaxSize(),
        onImageClick = { picture ->
            onImageClick(picture.id, picture.imageUrl)
        },
        shouldRefresh = shouldRefresh,
        onRefreshComplete = onRefreshComplete
    )
}