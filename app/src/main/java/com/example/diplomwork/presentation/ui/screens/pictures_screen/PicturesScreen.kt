package com.example.diplomwork.presentation.ui.screens.pictures_screen

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.diplomwork.data.model.SpotResponse
import com.example.diplomwork.presentation.ui.components.LoadingSpinnerForScreen
import com.example.diplomwork.presentation.ui.components.PictureCard
import com.example.diplomwork.presentation.ui.screens.spots_screen.ErrorRetryBlock
import com.example.diplomwork.presentation.viewmodel.SpotsViewModel

@Composable
fun PicturesScreen(
    spotsViewModel: SpotsViewModel = hiltViewModel(),
    onSpotClick: (Long) -> Unit,
    onProfileClick: (Long, String) -> Unit
) {
    val pagingPictures = spotsViewModel.spotsPagingFlow.collectAsLazyPagingItems()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        spotsViewModel.deleteStatus.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PagingContentGrid(
            modifier = Modifier.fillMaxSize(),
            onImageClick = { picture -> onSpotClick(picture.id) },
            onProfileClick = onProfileClick,
            onPictureDelete = { id -> spotsViewModel.deletePicture(id) },
            pictures = pagingPictures
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagingContentGrid(
    modifier: Modifier = Modifier,
    onImageClick: (SpotResponse) -> Unit,
    onProfileClick: (Long, String) -> Unit,
    onPictureDelete: (Long) -> Unit,
    pictures: LazyPagingItems<SpotResponse>
) {
    val loadState = pictures.loadState
    val isRefreshing = loadState.refresh is LoadState.Loading
    val stateRefresh = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { pictures.refresh() },
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
                ErrorRetryBlock(
                    error = e.message ?: "Ошибка",
                    onRetry = { pictures.retry() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.Center)
                )
            }

            pictures.itemCount == 0 -> {
                Text(
                    text = "Нет доступных пинов",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }

            else -> {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = modifier,
                    content = {
                        items(pictures.itemCount) { index ->
                            pictures[index]?.let { picture ->
                                PictureCard(
                                    imageUrl = picture.thumbnailImageUrl,
                                    username = picture.username,
                                    userId = picture.userId,
                                    aspectRatio = picture.aspectRatio ?: 1f,
                                    userProfileImageUrl = picture.userProfileImageUrl,
                                    id = picture.id,
                                    isCurrentUserOwner = picture.isCurrentUserOwner,
                                    onPictureClick = { onImageClick(picture) },
                                    onProfileClick = onProfileClick,
                                    onPictureDelete = onPictureDelete,
                                    screenName = "Picture"
                                )
                            }
                        }

                        if (loadState.append is LoadState.Loading) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier
                                        .padding(16.dp)
                                )
                            }
                        }

                        if (loadState.append is LoadState.Error) {
                            val e = (loadState.append as LoadState.Error).error
                            item(span = StaggeredGridItemSpan.FullLine) {
                                ErrorRetryBlock(
                                    error = e.message ?: "Ошибка загрузки",
                                    onRetry = { pictures.retry() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}
