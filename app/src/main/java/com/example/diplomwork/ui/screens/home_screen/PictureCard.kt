package com.example.diplomwork.ui.screens.home_screen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.example.diplomwork.network.ApiClient
import com.example.diplomwork.ui.components.LoadingSpinnerForScreen
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun PictureCard(
    imageUrl: String,
    onClick: () -> Unit
) {
    var aspectRatio by remember { mutableFloatStateOf(1f) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    var retryCount by rememberSaveable { mutableIntStateOf(0) }
    var currentUrl by rememberSaveable { mutableStateOf(processImageUrl(imageUrl)) }
    val maxRetryCount = 2

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(5.dp),
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .clickable(enabled = !isLoading && !isError) { onClick() }
    ) {
        Box(
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(currentUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onState = { state ->
                    isLoading = state is AsyncImagePainter.State.Loading

                    if (state is AsyncImagePainter.State.Error) {
                        isError = true
                        val exception = state.result.throwable
                        Log.e("PictureCard", "Ошибка загрузки изображения: $currentUrl", exception)

                        if (retryCount < maxRetryCount) {
                            retryCount++
                            val errorCode = when {
                                exception.message?.contains("410") == true -> 410
                                exception.message?.contains("404") == true -> 404
                                exception.message?.contains("400") == true -> 400
                                exception.message?.contains("500") == true -> 500
                                else -> -1
                            }

                            currentUrl = when (errorCode) {
                                410, 404 -> {
                                    Log.w("PictureCard", "Ссылка недействительна ($errorCode), пробуем через прокси")
                                    forceProxyImageUrl(imageUrl)
                                }
                                in 400..499 -> {
                                    Log.w("PictureCard", "Клиентская ошибка ($errorCode), пробуем другой URL")
                                    tryAlternativeUrl(imageUrl, retryCount)
                                }
                                else -> {
                                    Log.w("PictureCard", "Неизвестная ошибка, пробуем через прокси")
                                    forceProxyImageUrl(imageUrl)
                                }
                            }
                        } else {
                            Log.e("PictureCard", "Все попытки загрузки исчерпаны для: $imageUrl")
                        }
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
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio)
                    .clip(RoundedCornerShape(12.dp))
            )

            when {
                isLoading -> {
                    LoadingSpinnerForScreen(
                        Color.White,
                        25
                    )
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

private fun processImageUrl(imageUrl: String): String {
    val url = if (imageUrl.startsWith("@")) imageUrl.substring(1) else imageUrl

    return when {
        url.startsWith("http://") || url.startsWith("https://") -> url
        else -> "${ApiClient.getBaseUrl()}$url"
    }
}

private fun forceProxyImageUrl(imageUrl: String): String {
    val url = if (imageUrl.startsWith("@")) imageUrl.substring(1) else imageUrl

    val baseUrl = if (url.startsWith("http")) {
        url
    } else {
        "${ApiClient.getBaseUrl()}$url"
    }

    return "$baseUrl${if (url.contains("?")) "&" else "?"}cache_bust=${System.currentTimeMillis()}"
}

private fun tryAlternativeUrl(imageUrl: String, attemptNumber: Int): String {
    val url = if (imageUrl.startsWith("@")) imageUrl.substring(1) else imageUrl

    return when (attemptNumber) {
        1 -> forceProxyImageUrl(url)
        else -> {
            if (url.startsWith("http")) {
                url
            } else {
                "${ApiClient.getBaseUrl()}$url"
            }
        }
    }
}

