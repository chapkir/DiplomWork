package com.example.diplomwork.presentation.ui.components

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.diplomwork.R
import com.example.diplomwork.presentation.ui.components.bottom_sheets.ConfirmDeleteBottomSheet
import com.example.diplomwork.presentation.ui.components.bottom_sheets.MenuBottomSheet
import com.example.diplomwork.presentation.ui.theme.BgElevated
import com.example.diplomwork.presentation.ui.theme.ButtonPrimary
import com.example.diplomwork.presentation.ui.theme.DividerDark
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import coil.compose.AsyncImagePainter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotsCard(
    imageUrl: String,
    username: String,
    title: String,
    description: String,
    userId: Long,
    latitude: Double,
    longitude: Double,
    rating: Double,
    aspectRatio: Float,
    userProfileImageUrl: String?,
    id: Long,
    isCurrentUserOwner: Boolean = false,
    onSpotClick: () -> Unit,
    onProfileClick: (Long, String) -> Unit = { _, _ -> },
    onSpotDelete: (Long) -> Unit = { _ -> },
    screenName: String
) {

    val coroutineScope = rememberCoroutineScope()
    val menuSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val confirmDeleteSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val openMenuSheet = { coroutineScope.launch { menuSheetState.show() } }
    val closeMenuSheet = { coroutineScope.launch { menuSheetState.hide() } }
    val openConfirmDeleteSheet = { coroutineScope.launch { confirmDeleteSheetState.show() } }
    val closeConfirmDeleteSheet = { coroutineScope.launch { confirmDeleteSheetState.hide() } }

    Card(
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
            .clickable { onSpotClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { clip = true }
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (screenName == "Spots") {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { onProfileClick(userId, username) },
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = userProfileImageUrl ?: R.drawable.default_avatar,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { onProfileClick(userId, username) },
                        ) {
                            Text(
                                text = username,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(15.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { openMenuSheet() }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_menu_dots_vertical),
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    HorizontalDivider(thickness = 2.dp, color = DividerDark)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                else Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    ImagesPager(
                        imageUrls = imageUrl,
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .weight(0.55f)
                    )
                    PlaceInfo(
                        rating = rating.toInt(),
                        title = title,
                        description = description,
                        geo = "$latitude,$longitude",
                        latitude = latitude,
                        longitude = longitude,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(start = 15.dp, end = 10.dp)
                            .weight(0.8f)
                    )
                }
            }
        }
    }

    if (menuSheetState.isVisible) {
        MenuBottomSheet(
            onDismiss = { closeMenuSheet() },
            onDelete = { openConfirmDeleteSheet() },
            onReportSpot = { }, // TODO
            onDownloadPicture = { }, // TODO
            onHideSpot = { }, // TODO
            sheetState = menuSheetState,
            isOwnContent = isCurrentUserOwner
        )
    }

    if (confirmDeleteSheetState.isVisible) {
        ConfirmDeleteBottomSheet(
            onDismiss = { closeConfirmDeleteSheet() },
            onDelete = {
                onSpotDelete(id)
                closeMenuSheet()
            },
            sheetState = confirmDeleteSheetState,
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ImagesPager(
    imageUrls: String,
    modifier: Modifier
) {
    val pagerState = rememberPagerState()
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    var retryCount by remember { mutableIntStateOf(0) }

    var displayUrl = imageUrls

    // Формируем список изображений
//    val images = remember(firstPicture, additionalPictures) {
//        buildList {
//            firstPicture?.let { add(it) }
//            addAll(additionalPictures)
//        }
//    }

    // Добавим "заглушку", если доп. картинки ещё не загружены
//    val displayImages = remember(imageUrls) {
//        if (imageUrls.size == 1 && additionalPictures.isEmpty()) {
//            images + listOf<String?>(null) // заглушка
//        } else {
//            images
//        }
//    }

    Column(
        modifier = modifier
    ) {
        HorizontalPager(
            count = 5, //displayImages.size,
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp)),
        ) { page ->

            // Если пользователь долистал до заглушки и не загружали — вызываем загрузку
//            if (displayImages[page] == null && additionalPictures.isEmpty()) {
//                onLoadMore()
//            }

            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(5.dp),
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.75f)
                        .graphicsLayer { clip = true }
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFFA292),
                                        Color(0xFFD5523B),
                                    )
                                ),
                            )
                            .blur(50.dp),
                    )

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(displayUrl) // TODO Когда несколько картинок imageUrls[page]
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
                                val exception = state.result.throwable

                                Log.e(
                                    "SpotCard",
                                    "Ошибка загрузки изображения: $displayUrl",
                                    exception
                                )

                                if (retryCount < 2) {
                                    retryCount++
                                    displayUrl = "${imageUrls}?cache_bust=${System.currentTimeMillis()}"
                                }
                            }
                        },
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 10.dp, top = 10.dp)
        ) {
            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier.align(Alignment.BottomCenter),
                activeColor = ButtonPrimary,
                inactiveColor = Color.LightGray,
                indicatorWidth = 6.dp,
                spacing = 4.dp
            )
        }
    }
}

@Composable
private fun PlaceInfo(
    rating: Int,
    title: String,
    description: String,
    geo: String,
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = if (title == "") "Без названия" else title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Рейтинг
        RatingBar(rating = rating)

        Spacer(modifier = Modifier.height(10.dp))

        GeoText(latitude, longitude)

        Spacer(modifier = Modifier.height(10.dp))

        // Описание
        Text(
            text = if (description == "") "Без описания" else description,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
fun RatingBar(rating: Int) {
    Row {
        repeat(5) { index ->
            val starColor = if (index < rating) Color.Yellow else Color.Gray
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Рейтинг",
                tint = starColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun GeoText(latitude: Double, longitude: Double, placeName: String? = "Елагин парк") {
    val context = LocalContext.current
    val geo = "$latitude,$longitude"

    Text(
        text = if (placeName.isNullOrBlank()) geo else "$placeName ($geo)",
        fontSize = 12.sp,
        color = Color.Gray,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier.clickable {
            try {
                val encodedPlaceName = placeName?.replace(" ", "+") ?: ""

                val mapUri = if (encodedPlaceName.isNotBlank()) {
                    "https://yandex.ru/maps/?pt=$longitude,$latitude,pm2blm&z=16&l=map&text=$encodedPlaceName"
                } else {
                    "https://yandex.ru/maps/?pt=$longitude,$latitude,pm2blm&z=16&l=map"
                }

                val intent = Intent(Intent.ACTION_VIEW, mapUri.toUri())

                val chooser = Intent.createChooser(intent, "Выберите приложение для открытия карты")

                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(chooser)
                } else {
                    Log.d("GeoText", "No app found to handle URI")
                }
            } catch (e: Exception) {
                Log.e("GeoText", "Error creating intent: ${e.message}")
            }
        }
    )
}