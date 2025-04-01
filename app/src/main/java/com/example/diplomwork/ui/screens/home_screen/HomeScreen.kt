package com.example.diplomwork.ui.screens.home_screen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.diplomwork.model.PictureResponse
import com.example.diplomwork.viewmodel.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    onImageClick: (Long, String) -> Unit = { _, _ -> },
    shouldRefresh: Boolean = false,
    onRefreshComplete: () -> Unit = {},
    searchQuery: String = ""
) {

    val pictures by homeViewModel.pictures.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val error by homeViewModel.error.collectAsState()

    val refreshing by remember { mutableStateOf(false) }
    val refreshTrigger by remember { mutableIntStateOf(0) }

    // Отслеживаем изменения флага shouldRefresh
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            homeViewModel.refreshPictures(searchQuery)
            onRefreshComplete()
        }
    }

    // Логика обновления при Pull-to-Refresh
    val refreshScope = rememberCoroutineScope()

    fun refresh() = refreshScope.launch {
        homeViewModel.refreshPictures(searchQuery)
    }

    val pullRefreshState = rememberPullRefreshState(refreshing, ::refresh)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        ContentGrid(
            homeViewModel,
            modifier = Modifier.fillMaxSize(),
            onImageClick = { pictureResponse ->
                onImageClick(pictureResponse.id, pictureResponse.imageUrl)
            },
            pictures = pictures,
            isLoading = isLoading,
            error = error,
            refreshTrigger = refreshTrigger,
            isRefreshing = refreshing,
            searchQuery = searchQuery
        )

        PullRefreshIndicator(
            refreshing = refreshing,
            state = pullRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .size(40.dp)
        )
    }
}
