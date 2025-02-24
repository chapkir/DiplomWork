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
import coil.request.ImageRequest

@Composable
fun ImageCard(
    imageUrl: String,
    onClick: () -> Unit

) {

    var aspectRatio by remember { mutableStateOf(1f) }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(5.dp),
        modifier = Modifier
            .padding(6.dp)
            .fillMaxWidth()
            .clickable { onClick() } // Ну и тут тоже переход получается
    ) {
        Box(
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(if (imageUrl.startsWith("http")) imageUrl else ApiClient.baseUrl + imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                onState = { state ->
                    if (state is AsyncImagePainter.State.Success) {
                        val size = state.painter.intrinsicSize
                        if (size.width > 0 && size.height > 0) {
                            aspectRatio =
                                size.width / size.height
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .clip(RoundedCornerShape(12.dp))
            )

            if (aspectRatio == 0f) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

