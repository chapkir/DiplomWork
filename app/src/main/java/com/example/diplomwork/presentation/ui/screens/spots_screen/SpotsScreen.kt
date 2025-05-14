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
import com.example.diplomwork.presentation.ui.components.SpotsCard
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
    //val location

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


