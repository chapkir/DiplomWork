package com.example.diplomwork.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest

@Composable
fun PictureCard(
    imageUrl: String,
    id: Long,
    onClick: () -> Unit,
    contentPadding: Int = 3
) {
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(5.dp),
        modifier = Modifier
            .padding(contentPadding.dp)
            .fillMaxWidth()
            .clickable(enabled = !isLoading && !isError) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            // Градиентный фон с блюром
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF789AAB),
                                Color(0xFF09485E),
                            )
                        )
                    )
                    .blur(50.dp)
            )

            // Обёртка с aspectRatio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
            ) {
                // Само изображение
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(300)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    onState = { state ->
                        isLoading = state is AsyncImagePainter.State.Loading

                        if (state is AsyncImagePainter.State.Error) {
                            isError = true
                        }

                        if (state is AsyncImagePainter.State.Success) {
                            isError = false
                            val size = state.painter.intrinsicSize
                            if (size.width > 0 && size.height > 0) {
                                aspectRatio = size.width / size.height
                            }
                        }
                    },
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(12.dp))
                )

                // Лоадер и ошибки теперь по центру
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .matchParentSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingSpinnerForElement(Color.White, indicatorSize = 32)
                        }
                    }

                    isError -> {
                        Text(
                            text = "Ошибка загрузки",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

