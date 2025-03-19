package com.example.diplomwork.ui.screens.home_screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import androidx.compose.runtime.saveable.rememberSaveable
import android.util.Log
import java.io.IOException

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

                    // Обработка ошибок
                    if (state is AsyncImagePainter.State.Error) {
                        isError = true
                        val exception = state.result.throwable

                        // Подробное логирование ошибки
                        Log.e("PictureCard", "Ошибка загрузки изображения: $currentUrl", exception)

                        // Проверяем тип ошибки и пробуем обработать
                        if (retryCount < maxRetryCount) {
                            retryCount++

                            // Определяем тип ошибки по сообщению или классу исключения
                            val errorCode = when {
                                exception.message?.contains("410") == true -> 410
                                exception.message?.contains("404") == true -> 404
                                exception.message?.contains("400") == true -> 400
                                exception.message?.contains("500") == true -> 500
                                else -> -1
                            }

                            when (errorCode) {
                                410, 404 -> {
                                    // Если ссылка устарела (410 Gone) или не найдена (404)
                                    Log.w("PictureCard", "Ссылка недействительна (${errorCode}), пробуем через прокси")
                                    currentUrl = forceProxyImageUrl(imageUrl)
                                }
                                in 400..499 -> {
                                    // Другие клиентские ошибки
                                    Log.w("PictureCard", "Клиентская ошибка (${errorCode}), пробуем другой URL")
                                    currentUrl = tryAlternativeUrl(imageUrl, retryCount)
                                }
                                else -> {
                                    // Серверные ошибки или неизвестные
                                    Log.w("PictureCard", "Неизвестная ошибка, пробуем через прокси")
                                    currentUrl = forceProxyImageUrl(imageUrl)
                                }
                            }
                        } else {
                            // Выводим сообщение, что все попытки исчерпаны
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
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center),
                        color = Color.White
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

/**
 * Обрабатывает URL изображения, при необходимости добавляя к нему базовый URL или
 * используя прокси для Яндекс.Диска
 */
private fun processImageUrl(imageUrl: String): String {
    return when {
        // Если URL уже содержит протокол, проверяем, это Яндекс.Диск или нет
        imageUrl.startsWith("http://") || imageUrl.startsWith("https://") -> {
            if (isYandexDiskUrl(imageUrl)) {
                // Используем прокси для Яндекс.Диска
                "${ApiClient.getBaseUrl()}api/pins/proxy-image?url=${android.net.Uri.encode(imageUrl)}"
            } else {
                imageUrl
            }
        }
        // Если это относительный путь, добавляем базовый URL
        else -> "${ApiClient.getBaseUrl()}$imageUrl"
    }
}

/**
 * Принудительно проксирует URL через сервер
 */
private fun forceProxyImageUrl(imageUrl: String): String {
    val baseUrl = if (imageUrl.startsWith("http")) {
        imageUrl
    } else {
        "${ApiClient.getBaseUrl()}$imageUrl"
    }
    return "${ApiClient.getBaseUrl()}api/pins/proxy-image?url=${android.net.Uri.encode(baseUrl)}&cache_bust=${System.currentTimeMillis()}"
}

/**
 * Пробует альтернативный URL в зависимости от номера попытки
 */
private fun tryAlternativeUrl(imageUrl: String, attemptNumber: Int): String {
    return when (attemptNumber) {
        1 -> forceProxyImageUrl(imageUrl) // Первая попытка - через прокси
        else -> {
            // Вторая попытка - прямая ссылка, если это не Яндекс Диск
            if (!isYandexDiskUrl(imageUrl) && imageUrl.startsWith("http")) {
                imageUrl
            } else {
                // Иначе снова через прокси, но с другим параметром cache_bust
                "${ApiClient.getBaseUrl()}api/pins/proxy-image?url=${android.net.Uri.encode(imageUrl)}&cache_bust=${System.currentTimeMillis() + attemptNumber}"
            }
        }
    }
}

/**
 * Проверяет, является ли URL ссылкой на Яндекс.Диск
 */
private fun isYandexDiskUrl(url: String): Boolean {
    return url.contains("yandex") ||
            url.contains("disk.") ||
            url.contains("downloader.") ||
            url.contains("preview.") ||
            url.contains("yadi.sk")
}

