package com.example.diplomwork.presentation.ui.components.spot_card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.diplomwork.presentation.ui.components.bottom_sheets.ConfirmDeleteBottomSheet
import com.example.diplomwork.presentation.ui.components.bottom_sheets.MenuBottomSheet
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotCard(
    firstPicture: String,
    additionalPictures: List<String>,
    onLoadMore: (Long, String) -> Unit,
    picturesCount: Int,
    username: String,
    title: String,
    placeName: String,
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
        elevation = CardDefaults.cardElevation(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clickable { onSpotClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                if (screenName == "Spots") {
                    SpotCardHeader(
                        onProfileClick = { onProfileClick(userId, username) },
                        userProfileImageUrl = userProfileImageUrl,
                        username = username,
                        openMenuSheet = openMenuSheet
                    )
                } else Spacer(modifier = Modifier.height(13.dp))


                Row(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    ImagesPager(
                        firstPicture = firstPicture,
                        additionalPictures = additionalPictures,
                        picturesCount = picturesCount,
                        onLoadMore = { onLoadMore(id, firstPicture) },
                        modifier = Modifier
                            .padding(start = 13.dp)
                            .weight(0.58f)
                    )

                    PlaceInfo(
                        rating = rating.toInt(),
                        title = title,
                        placeName = placeName,
                        description = description,
                        latitude = latitude,
                        longitude = longitude,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 15.dp, end = 10.dp)
                            .weight(0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(13.dp))
            }
        }
    }


    if (menuSheetState.isVisible) {
        MenuBottomSheet(
            onDismiss = { closeMenuSheet() },
            onDelete = {
                openConfirmDeleteSheet()
                closeMenuSheet()
            },
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
            message = "Вы уверены, что хотите удалить место?"
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ImagesPager(
    firstPicture: String,
    additionalPictures: List<String>,
    picturesCount: Int,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState()
    var isLoadMoreTriggered by remember { mutableStateOf(false) }

    val images = remember(firstPicture, additionalPictures) {
        buildList {
            add(firstPicture)
            addAll(additionalPictures)
        }
    }

    val displayImages = remember(images, picturesCount) {
        List(picturesCount) { index -> images.getOrNull(index) }
    }

    Box(modifier = modifier) {
        HorizontalPager(
            count = picturesCount,
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(clip = false)
                .clip(RoundedCornerShape(12.dp)),
        ) { page ->

            val imageUrl = displayImages[page]
            if (imageUrl == null && !isLoadMoreTriggered) {
                isLoadMoreTriggered = true
                onLoadMore()
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.75f)
                ) {

                    if (imageUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(300)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
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
                    }
                }
            }
        }

        if (picturesCount > 1) {
            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp),
                activeColor = Color.White,
                inactiveColor = Color.White.copy(alpha = 0.7f),
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
    placeName: String,
    description: String,
    latitude: Double,
    longitude: Double,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = if (title == "") placeName else title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        RatingBar(rating = rating)

        Spacer(modifier = Modifier.height(10.dp))

        Box {
            GeoText(latitude, longitude)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (description == "") "Без описания" else description,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}