package com.example.diplomwork.ui.screens.main_screen.content_grid

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import androidx.compose.ui.platform.LocalContext
import com.example.diplomwork.network.ApiClient
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color

@Composable
fun ImageCard(
    imageUrl: String,
    onClick: () -> Unit,
    templateType: Int = 0
) {
    when (templateType) {
        0 -> {
            // Шаблон А: по умолчанию (прямоугольная карточка 1:1)
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(5.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .padding(6.dp)
                    .fillMaxWidth()
                    .clickable { onClick() }
            ) {
                Box(modifier = Modifier.clip(RoundedCornerShape(12.dp))) {
                    AsyncImage(
                        model = if (imageUrl.startsWith("http")) imageUrl else ApiClient.BASE_URL + imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
        }
        1 -> {
            // Шаблон B: альтернативный, прямоугольная карточка с соотношением сторон 2:1
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .padding(6.dp)
                    .fillMaxWidth()
                    .clickable { onClick() }
            ) {
                AsyncImage(
                    model = if (imageUrl.startsWith("http")) imageUrl else ApiClient.BASE_URL + imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
        else -> {
            ImageCard(imageUrl = imageUrl, onClick = onClick, templateType = 0)
        }
    }
}

