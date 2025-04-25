package com.example.diplomwork.ui.screens.pictures_screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.diplomwork.model.PictureResponse
import com.example.diplomwork.ui.components.LoadingSpinnerForScreen
import com.example.diplomwork.ui.components.PictureCard
import com.example.diplomwork.ui.components.rememberSlowFlingBehavior
import com.example.diplomwork.viewmodel.PicturesViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PicturesScreen(
    picturesViewModel: PicturesViewModel = hiltViewModel(),
    onImageClick: (Long, String) -> Unit,
    searchQuery: String = ""
) {

    val pictures by picturesViewModel.pictures.collectAsState()
    val isLoading by picturesViewModel.isLoading.collectAsState()
    val error by picturesViewModel.error.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        ContentGrid(
            modifier = Modifier.fillMaxSize(),
            onImageClick = { pictureResponse ->
                onImageClick(pictureResponse.id, pictureResponse.imageUrl)
            },
            pictures = pictures,
            isLoading = isLoading,
            onRefresh = { picturesViewModel.refreshPictures() },
            error = error,
            searchQuery = searchQuery
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentGrid(
    modifier: Modifier = Modifier,
    onImageClick: (PictureResponse) -> Unit,
    pictures: List<PictureResponse>,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    error: String?,
    searchQuery: String = ""
) {
    var searchInfoText by remember { mutableStateOf<String?>(null) }
    val stateRefresh = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery, pictures.size) {
        searchInfoText = if (searchQuery.isNotEmpty()) {
            if (pictures.isEmpty()) {
                "По запросу \"$searchQuery\" ничего не найдено"
            } else {
                "Найдено ${pictures.size} результатов по запросу \"$searchQuery\""
            }
        } else null
    }

    Box(
        modifier = modifier.background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading && !isRefreshing -> {
                LoadingSpinnerForScreen()
            }

            error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = error ?: "",
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onRefresh() }
                    ) {
                        Text("Повторить", color = Color.White)
                    }
                }
            }

            pictures.isEmpty() -> {
                Text(
                    text = if (searchQuery.isEmpty()) "Нет доступных пинов"
                    else "По запросу \"$searchQuery\" ничего не найдено",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }

            else -> {
                PullToRefreshBox(
                    isRefreshing = isLoading,
                    onRefresh = {
                        isRefreshing = true
                        onRefresh()
                    },
                    state = stateRefresh,
                    indicator = {
                        Indicator(
                            modifier = Modifier.align(Alignment.TopCenter),
                            isRefreshing = isLoading,
                            containerColor = Color.Gray,
                            color = Color.White,
                            state = stateRefresh
                        )
                    }
                ) {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        flingBehavior = rememberSlowFlingBehavior(),
                        modifier = Modifier.fillMaxSize(),
                        content = {
                            if (searchInfoText != null) {
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    Text(
                                        text = searchInfoText!!,
                                        color = Color.White,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            itemsIndexed(
                                items = pictures,
                                key = { _, picture -> picture.id }
                            ) { _, picture ->
                                PictureCard(
                                    imageUrl = picture.imageUrl,
                                    username = picture.username,
                                    aspectRatio = picture.aspectRatio ?: 1f,
                                    userProfileImageUrl = picture.userProfileImageUrl,
                                    id = picture.id,
                                    onClick = { onImageClick(picture) },
                                    screenName = "Picture"
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
