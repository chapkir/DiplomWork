package com.example.diplomwork.presentation.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LoadingSpinnerForScreen(
    indicatorColor: Color = Color.Gray,
    indicatorSize: Int = 55
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        CircularProgressIndicator(
            color = indicatorColor,
            strokeWidth = 7.dp,
            modifier = Modifier.size(indicatorSize.dp)
        )
    }
}

@Composable
fun LoadingSpinnerForElement(
    indicatorColor: Color = Color.White,
    indicatorSize: Int = 20
) {
    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.Center)
    ) {
        CircularProgressIndicator(
            color = indicatorColor,
            strokeWidth = 6.dp,
            modifier = Modifier.size(indicatorSize.dp)
        )
    }
}
