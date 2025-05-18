package com.example.diplomwork.presentation.ui.screens.spots_screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.diplomwork.presentation.ui.components.LoadingSpinnerForScreen
import com.example.diplomwork.presentation.ui.components.spot_card.SpotsCard
import com.example.diplomwork.presentation.ui.screens.pictures_screen.ErrorRetryBlock
import com.example.diplomwork.presentation.viewmodel.SpotsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotsScreen(
    spotsViewModel: SpotsViewModel = hiltViewModel(),
    onImageClick: (Long) -> Unit,
    onProfileClick: (Long, String) -> Unit
) {
    val spots = spotsViewModel.picturesPagingFlow.collectAsLazyPagingItems()
    val spotLocations by spotsViewModel.spotLocations.collectAsState()

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
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(spots.itemCount) { index ->
                        spots[index]?.let { spot ->
                            val location = spotLocations[spot.id]

                            LaunchedEffect(spot.id) {
                                spotsViewModel.loadLocationsForVisibleSpots(setOf(spot.id))
                            }

                            SpotsCard(
                                imageUrl = spot.thumbnailImageUrl,
                                username = spot.username,
                                title = spot.title,
                                description = spot.description,
                                userId = spot.userId,
                                latitude = location?.latitude ?: 0.0,
                                longitude = location?.longitude ?: 0.0,
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


