package com.example.diplomwork.presentation.ui.screens.spot_detail_screen

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.diplomwork.R
import com.example.diplomwork.presentation.ui.components.GeoText
import com.example.diplomwork.presentation.ui.components.LoadingSpinnerForScreen
import com.example.diplomwork.presentation.ui.components.RatingBar
import com.example.diplomwork.presentation.ui.components.bottom_sheets.CommentsBottomSheet
import com.example.diplomwork.presentation.ui.components.bottom_sheets.ConfirmDeleteBottomSheet
import com.example.diplomwork.presentation.ui.components.bottom_sheets.MenuBottomSheet
import com.example.diplomwork.presentation.ui.screens.map_screen.getResizedImageProvider
import com.example.diplomwork.presentation.ui.screens.picture_detail_screen.ImageView
import com.example.diplomwork.presentation.ui.theme.IconPrimary
import com.example.diplomwork.presentation.viewmodel.PictureDetailScreenViewModel
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
    viewModel: PictureDetailScreenViewModel = hiltViewModel()
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
                .background(Color.Black)
                .imePadding()
        ) {
            item {
                ImageView(
                    imageRes = uiState.picture?.thumbnailImageUrl ?: "",
                    aspectRatio = 0.75f, //uiState.aspectRatio
                )
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
                    rating = 4, //uiState.rating.toInt(),
                    title = "Елагин остров",//uiState.pictureTitle,
                    description = "Очень зеленое и свежее место, люблю гулять там с детьми, кормить белок и нюхать траву. А еще люблю чешские трдельники там!!!",//uiState.pictureDescription,
                    geo = "нет",
                    latitude = 43.534534,
                    longitude = 32.534534,
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
                    start = 16.dp
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
                    end = 16.dp
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
            sheetState = commentSheetState
        )
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
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onPrimary
        )
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