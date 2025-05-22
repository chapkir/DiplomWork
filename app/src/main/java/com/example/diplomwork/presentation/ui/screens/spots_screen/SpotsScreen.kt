package com.example.diplomwork.presentation.ui.screens.spots_screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.diplomwork.presentation.ui.components.LoadingSpinnerForElement
import com.example.diplomwork.presentation.ui.components.LoadingSpinnerForScreen
import com.example.diplomwork.presentation.ui.components.spot_card.SpotCard
import com.example.diplomwork.presentation.viewmodel.SpotsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotsScreen(
    spotsViewModel: SpotsViewModel = hiltViewModel(),
    onImageClick: (Long) -> Unit,
    onProfileClick: (Long, String) -> Unit
) {
    val spots = spotsViewModel.spotsPagingFlow.collectAsLazyPagingItems()
    val additionalPictures by spotsViewModel.imagesUrls.collectAsState()

    val context = LocalContext.current
    val listState = rememberLazyListState()

    val loadState = spots.loadState
    val isRefreshing = loadState.refresh is LoadState.Loading
    val stateRefresh = rememberPullToRefreshState()

    LaunchedEffect(Unit) {
        spotsViewModel.deleteStatus.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
                    Box(modifier = Modifier.fillMaxSize())
                    {
                        val e = (loadState.refresh as LoadState.Error).error
                        ErrorRetryBlock(
                            error = "Ошибка загрузки, попробуйте перезайти в аккаунт",
                            onRetry = { spots.retry() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .align(Alignment.Center)
                        )
                    }
                }

                spots.itemCount == 0 -> {
                    Text(
                        text = "Нет доступных мест",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(spots.itemCount) { index ->
                            spots[index]?.let { spot ->
                                SpotCard(
                                    firstPicture = spot.thumbnailImageUrl,
                                    additionalPictures = additionalPictures[spot.id]?.pictures
                                        ?: emptyList(),
                                    onLoadMore = { id, firstPicture ->
                                        spotsViewModel.loadMorePicturesForSpot(id, firstPicture)
                                    },
                                    picturesCount = spot.picturesCount,
                                    username = spot.username,
                                    title = spot.title,
                                    placeName = spot.namePlace ?: "",
                                    description = spot.description,
                                    userId = spot.userId,
                                    latitude = spot.latitude ?: 0.0,
                                    longitude = spot.longitude ?: 0.0,
                                    rating = spot.rating,
                                    aspectRatio = spot.aspectRatio ?: 1f,
                                    userProfileImageUrl = spot.userProfileImageUrl,
                                    id = spot.id,
                                    isCurrentUserOwner = spot.isCurrentUserOwner,
                                    onSpotClick = { onImageClick(spot.id) },
                                    onProfileClick = onProfileClick,
                                    onSpotDelete = { id -> spotsViewModel.deletePicture(id) },
                                    screenName = "Spots"
                                )
                            }
                        }
                        if (loadState.append is LoadState.Loading) {
                            item() {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    LoadingSpinnerForElement()
                                }
                            }
                        }

                        if (loadState.append is LoadState.Error) {
                            val e = (loadState.append as LoadState.Error).error
                            item() {
                                ErrorRetryBlock(
                                    error = e.message ?: "Ошибка загрузки",
                                    onRetry = { spots.retry() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorRetryBlock(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = error,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Повторить", color = Color.White)
        }
    }
}


