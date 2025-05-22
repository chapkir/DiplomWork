package com.example.diplomwork.presentation.ui.screens.spot_detail_screen

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.diplomwork.R
import com.example.diplomwork.presentation.system_settings.systemInsetHeight
import com.example.diplomwork.presentation.ui.components.LoadingSpinnerForScreen
import com.example.diplomwork.presentation.ui.components.bottom_sheets.CommentItem
import com.example.diplomwork.presentation.ui.components.bottom_sheets.CommentsBottomSheet
import com.example.diplomwork.presentation.ui.components.bottom_sheets.ConfirmDeleteBottomSheet
import com.example.diplomwork.presentation.ui.components.bottom_sheets.MenuBottomSheet
import com.example.diplomwork.presentation.ui.components.spot_card.GeoText
import com.example.diplomwork.presentation.ui.components.spot_card.RatingBar
import com.example.diplomwork.presentation.ui.screens.map_screen.getResizedImageProvider
import com.example.diplomwork.presentation.ui.theme.IconPrimary
import com.example.diplomwork.presentation.viewmodel.SpotDetailScreenViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotDetailScreen(
    onNavigateBack: () -> Unit,
    onProfileClick: (Long, String) -> Unit,
    viewModel: SpotDetailScreenViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val commentSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val menuSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val confirmDeleteSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val openCommentSheet = { coroutineScope.launch { commentSheetState.show() } }
    val closeCommentSheet = { coroutineScope.launch { commentSheetState.hide() } }
    val openMenuSheet = { coroutineScope.launch { menuSheetState.show() } }
    val closeMenuSheet = { coroutineScope.launch { menuSheetState.hide() } }
    val openConfirmDeleteSheet = { coroutineScope.launch { confirmDeleteSheetState.show() } }
    val closeConfirmDeleteSheet = { coroutineScope.launch { confirmDeleteSheetState.hide() } }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (uiState.deleteStatus.isNotEmpty()) {
        Toast.makeText(context, uiState.deleteStatus, Toast.LENGTH_SHORT).show()
        if (uiState.deleteStatus == "Удаление успешно") {
            onNavigateBack()
        }
    }

    if (uiState.isLoading) {
        LoadingSpinnerForScreen()
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .imePadding()
        ) {
            item {
                ImagesPager(imageUrls = uiState.fullhdImages)
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
                ActionBar(
                    likesCount = uiState.likesCount,
                    isLiked = uiState.isLiked,
                    commentsCount = uiState.commentsCount,
                    profileImageUrl = uiState.profileImageUrl,
                    username = uiState.pictureUsername,
                    userId = uiState.pictureUserId,
                    onLikeClick = { viewModel.toggleLike() },
                    onCommentClick = { openCommentSheet() },
                    onProfileClick = onProfileClick,
                )
                Spacer(modifier = Modifier.height(15.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    color = Color.Gray.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            item {
                PlaceInfo(
                    rating = uiState.rating.toInt(),
                    title = uiState.pictureTitle,
                    placeName = uiState.placeName,
                    description = uiState.pictureDescription,
                    latitude = uiState.latitude,
                    longitude = uiState.longitude,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp, end = 15.dp)
                )
                Spacer(modifier = Modifier.height(30.dp))
            }
            item {
                MapCard()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        IconButton(
            onClick = { onNavigateBack() },
            modifier = Modifier
                .padding(
                    top = 10.dp,
                    start = 10.dp
                )
                .size(43.dp)
                .clip(CircleShape)
                .align(Alignment.TopStart)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = "back",
                tint = IconPrimary
            )
        }
        IconButton(
            onClick = { openMenuSheet() },
            modifier = Modifier
                .padding(
                    top = 10.dp,
                    end = 10.dp
                )
                .size(43.dp)
                .clip(CircleShape)
                .align(Alignment.TopEnd),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = Color.Black,
                containerColor = IconPrimary
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_menu_dots_vertical),
                contentDescription = "Menu",
                modifier = Modifier.padding(10.dp)
            )
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
            isOwnContent = uiState.isCurrentUserOwner
        )
    }

    if (confirmDeleteSheetState.isVisible) {
        ConfirmDeleteBottomSheet(
            onDismiss = { closeConfirmDeleteSheet() },
            onDelete = { viewModel.deletePicture() },
            sheetState = confirmDeleteSheetState,
        )
    }

    if (commentSheetState.isVisible) {
        CommentsBottomSheet(
            comments = uiState.comments,
            onDismiss = { closeCommentSheet() },
            onAddComment = { commentText -> viewModel.addComment(commentText) },
            sheetState = commentSheetState,
            onProfileClick = { onProfileClick(uiState.pictureUserId, uiState.pictureUsername) }
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun ImagesPager(
    imageUrls: List<String>
) {
    val pagerState = rememberPagerState()
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    Box {
        HorizontalPager(
            count = imageUrls.size,
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp)),
        ) { page ->
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
                            .data(imageUrls[page])
                            .crossfade(300)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        contentDescription = null,
                        onState = { state ->
                            isLoading = state is AsyncImagePainter.State.Loading

                            if (state is AsyncImagePainter.State.Error) {
                                isError = true
                                val exception = state.result.throwable

                                Log.e(
                                    "SpotCard",
                                    "Ошибка загрузки изображения: ${imageUrls[page]}",
                                    exception
                                )
                            }
                        },
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
        }
        if (imageUrls.size > 1) {
            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp),
                activeColor = Color.White,
                inactiveColor = Color.White.copy(alpha = 0.8f),
                indicatorWidth = 8.dp,
                spacing = 6.dp
            )
        }
    }
}

@Composable
private fun ActionBar(
    likesCount: Int,
    isLiked: Boolean,
    commentsCount: Int,
    profileImageUrl: String?,
    username: String,
    userId: Long,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onProfileClick: (Long, String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onProfileClick(userId, username)
                },
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = profileImageUrl ?: R.drawable.default_avatar,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = username,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
        }

        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onLikeClick() }
            ) {
                Icon(
                    painter = painterResource(
                        id =
                            if (isLiked) R.drawable.ic_favs_filled
                            else R.drawable.ic_favs
                    ),
                    contentDescription = "Лайк",
                    tint = if (isLiked) Color.Red else Color.White,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = likesCount.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onCommentClick() }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_comments),
                    contentDescription = "Комментарии",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = commentsCount.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun PlaceInfo(
    rating: Int,
    title: String,
    placeName: String,
    description: String,
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
    ) {
        Text(
            text = if (title == "") placeName else title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(10.dp))

        RatingBar(rating = rating, starSize = 20, screenName = "Detail")

        Spacer(modifier = Modifier.height(10.dp))

        GeoText(latitude, longitude)

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (description.isBlank()) "Без описания" else description,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            maxLines = if (isExpanded) Int.MAX_VALUE else 4,
            overflow = if (isExpanded) TextOverflow.Clip else TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.clickable { isExpanded = !isExpanded }
        )

        if (description.isNotBlank()) {
            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = if (isExpanded) "Скрыть" else "Показать полностью",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray,
                modifier = Modifier
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 2.dp)
            )
        }
    }
}

@Suppress("DEPRECATION")
@Composable
fun MapCard(
    latitude: Double = 59.940068,
    longitude: Double = 30.328952,
    placeName: String = "спас на крови"
) {
    val context = LocalContext.current
    val mapHeight = 150.dp
    val location = remember { Point(latitude, longitude) }

    val mapView = remember { mutableStateOf<MapView?>(null) }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(horizontal = 7.dp)
            .fillMaxWidth()
            .height(mapHeight),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            AndroidView(
                factory = { ctx ->
                    MapKitFactory.initialize(ctx)
                    MapView(ctx).apply {
                        onWindowFocusChanged(true)
                        requestFocus()


                        map.isZoomGesturesEnabled = false
                        map.isScrollGesturesEnabled = false
                        map.isRotateGesturesEnabled = false
                        map.isTiltGesturesEnabled = false

                        map.move(CameraPosition(location, 10f, 2f, 2f))
                        val icon = getResizedImageProvider(ctx, R.drawable.ic_marker_png, 64, 64)
                        map.mapObjects.addPlacemark(location).setIcon(icon)

                        mapView.value = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        val baseUri =
                            "yandexmaps://maps.yandex.ru/?pt=$longitude,$latitude&z=16&l=map"
                        val encodedPlace = Uri.encode(placeName)
                        val uri = if (placeName.isNotBlank()) {
                            "$baseUri&text=$encodedPlace"
                        } else {
                            baseUri
                        }.toUri()

                        val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                            setPackage("ru.yandex.yandexmaps")
                        }
                        try {
                            context.startActivity(mapIntent)
                        } catch (e: ActivityNotFoundException) {
                            Toast
                                .makeText(
                                    context,
                                    "Приложение Яндекс.Карты не установлено",
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                    }
            )
        }
    }
}