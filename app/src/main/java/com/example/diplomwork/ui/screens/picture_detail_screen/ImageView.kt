package com.example.diplomwork.ui.screens.picture_detail_screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.example.diplomwork.ui.components.LoadingSpinnerForElement
import com.example.diplomwork.ui.components.LoadingSpinnerForScreen

@Composable
fun ImageView(imageRes: String, aspectRatio: Float) {
    var currentAspectRatio by remember { mutableStateOf(aspectRatio) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    var retryCount by remember { mutableIntStateOf(0) }

    val context = LocalContext.current

    var displayUrl = imageRes

    Card(
        shape = RoundedCornerShape(30.dp),
        elevation = CardDefaults.cardElevation(15.dp),
        modifier = Modifier.padding(start = 7.dp, end = 7.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
        ) {
            // 1. Градиентный фон с блюром
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFB3E5FC),
                                Color(0xFF0A526B),
                            )
                        )
                    )
                    .blur(50.dp)
            )
            AsyncImage(
                model = ImageRequest.Builder(context).data(displayUrl).crossfade(true).build(),
                contentDescription = null,
                onState = { state ->
                    isLoading = state is AsyncImagePainter.State.Loading

                    if (state is AsyncImagePainter.State.Error) {
                        isError = true
                        val exception = state.result.throwable

                        // Логируем ошибку
                        Log.e(
                            "ImageView",
                            "Ошибка загрузки изображения: $displayUrl",
                            exception
                        )

                        // Пробуем восстановиться
                        if (retryCount < 2) {
                            retryCount++
                            displayUrl = "${displayUrl}?cache_bust=${System.currentTimeMillis()}"
                        }
                    }

                    if (state is AsyncImagePainter.State.Success) {
                        isError = false
                        val size = state.painter.intrinsicSize
                        if (size.width > 0 && size.height > 0) {
                            currentAspectRatio = size.width / size.height
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(currentAspectRatio)
                    .clip(RoundedCornerShape(12.dp))
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.matchParentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingSpinnerForElement(indicatorSize = 30)
                    }
                }

                isError -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .align(Alignment.Center)
                    ) {
                        Text(
                            text = "Ошибка загрузки изображения",
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}