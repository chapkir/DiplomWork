package com.example.diplomwork.presentation.ui.screens.spots_screen

import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.diplomwork.R
import com.example.diplomwork.presentation.ui.components.LoadingSpinnerForScreen
import com.example.diplomwork.presentation.ui.components.bottom_sheets.ConfirmDeleteBottomSheet
import com.example.diplomwork.presentation.ui.components.bottom_sheets.MenuBottomSheet
import com.example.diplomwork.presentation.ui.screens.pictures_screen.ErrorRetryBlock
import com.example.diplomwork.presentation.ui.theme.BgElevated
import com.example.diplomwork.presentation.ui.theme.ButtonPrimary
import com.example.diplomwork.presentation.ui.theme.DividerDark
import com.example.diplomwork.presentation.viewmodel.PicturesViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val aspectRatio = 0.75f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotsScreen(
    picturesViewModel: PicturesViewModel = hiltViewModel(),
    onImageClick: (Long) -> Unit,
    onProfileClick: (Long, String) -> Unit
) {
    val spots = picturesViewModel.picturesPagingFlow.collectAsLazyPagingItems()

    val context = LocalContext.current

    val loadState = spots.loadState
    val isRefreshing = loadState.refresh is LoadState.Loading
    val stateRefresh = rememberPullToRefreshState()

    LaunchedEffect(Unit) {
        picturesViewModel.deleteStatus.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { spots.refresh() },
        state = stateRefresh,
        indicator = {
            Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = isRefreshing,
                containerColor = Color.Gray,
                color = Color.White,
                state = stateRefresh
            )
        }
    ) {
        when {
            loadState.refresh is LoadState.Loading -> {
                LoadingSpinnerForScreen()
            }

            loadState.refresh is LoadState.Error -> {
                val e = (loadState.refresh as LoadState.Error).error
                ErrorRetryBlock(error = e.message ?: "Ошибка", onRetry = { spots.retry() })
            }

            spots.itemCount == 0 -> {
                Text(
                    text = "Нет доступных мест",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(spots.itemCount) { index ->
                        spots[index]?.let { spot ->
                            SpotsCard(
                                imageUrl = spot.fullhdImageUrl,
                                username = spot.username,
                                title = spot.title,
                                description = spot.description,
                                userId = spot.userId,
                                aspectRatio = spot.aspectRatio ?: 1f,
                                userProfileImageUrl = spot.userProfileImageUrl,
                                id = spot.id,
                                isCurrentUserOwner = spot.isCurrentUserOwner,
                                onSpotClick = { onImageClick(spot.id) },
                                onProfileClick = onProfileClick,
                                onSpotDelete = { id -> picturesViewModel.deletePicture(id) },
                            )
                        }
                    }
                    if (loadState.append is LoadState.Loading) {
                        item() {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier
                                    .padding(16.dp)
                            )
                        }
                    }

                    if (loadState.append is LoadState.Error) {
                        val e = (loadState.append as LoadState.Error).error
                        item() {
                            ErrorRetryBlock(
                                error = e.message ?: "Ошибка загрузки",
                                onRetry = { spots.retry() })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotsCard(
    imageUrl: String,
    username: String,
    title: String,
    description: String,
    userId: Long,
    aspectRatio: Float,
    userProfileImageUrl: String?,
    id: Long,
    isCurrentUserOwner: Boolean = false,
    onSpotClick: () -> Unit,
    onProfileClick: (Long, String) -> Unit,
    onSpotDelete: (Long) -> Unit,
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
                .background(BgElevated)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
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
                            color = Color.White,
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
                            tint = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))
                HorizontalDivider(thickness = 2.dp, color = DividerDark)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    ImagesPager(
                        imageUrls = imageUrl,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .weight(0.55f)
                    )
                    PlaceInfo(
                        rating = Random.nextInt(1, 6),
                        title = title,
                        description = description,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(start = 10.dp, end = 15.dp)
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
            onReportSpot = {  }, // TODO
            onDownloadPicture = {  }, // TODO
            onHideSpot = {  }, // TODO
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

    Column(
        modifier = modifier
    ) {
        HorizontalPager(
            count = 5,
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
                        .aspectRatio(aspectRatio)
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
                            .data(imageUrls) // TODO Когда несколько картинок imageUrls[page]
                            .crossfade(300)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
//                            onState = { state ->
//                                isLoading = state is AsyncImagePainter.State.Loading
//                                if (state is AsyncImagePainter.State.Error) {
//                                    isError = true
//                                }
//                            },
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
fun PlaceInfo(
    rating: Int,
    title: String,
    description: String,
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
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Рейтинг
        RatingBar(rating = rating)

        Spacer(modifier = Modifier.height(10.dp))

        // Геотег
        Text(
            text = "Локация не указана",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Описание
        Text(
            text = if (description == "") "Без описания" else description,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
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
