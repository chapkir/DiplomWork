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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    onImageClick: (String, String) -> Unit = { _, _ -> },
    shouldRefresh: Boolean = false,
    onRefreshComplete: () -> Unit = {},
    searchQuery: String = ""
) {
    val context = LocalContext.current
    var refreshing by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // Отслеживаем изменения флага shouldRefresh
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            Log.d("HomeScreen", "Запрос обновления получен, запускаем обновление...")
            refreshData(
                onRefreshStart = { refreshing = true },
                onRefreshComplete = {
                    refreshing = false
                    onRefreshComplete()
                    Log.d("HomeScreen", "Обновление завершено")
                },
                triggerRefresh = {
                    refreshTrigger++
                    Unit
                }
            )
        }
    }

    val refreshScope = rememberCoroutineScope()

    fun refresh() = refreshScope.launch {
        Log.d("HomeScreen", "Пользователь запустил Pull-to-Refresh")
        refreshData(
            onRefreshStart = { refreshing = true },
            onRefreshComplete = {
                refreshing = false
                Log.d("HomeScreen", "Pull-to-Refresh завершен")
            },
            triggerRefresh = {
                refreshTrigger++
                Unit
            }
        )
    }

    val pullRefreshState = rememberPullRefreshState(refreshing, ::refresh)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        ContentGrid(
            modifier = Modifier.fillMaxSize(),
            onImageClick = { pictureResponse ->
                Log.d("HomeScreen", "Нажатие на изображение с id: ${pictureResponse.id}")
                onImageClick(pictureResponse.id.toString(), pictureResponse.imageUrl)
            },
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

// логика обновления
private suspend fun refreshData(
    onRefreshStart: () -> Unit,
    onRefreshComplete: () -> Unit,
    triggerRefresh: () -> Unit
) {
    onRefreshStart()

    triggerRefresh()

    delay(800)

    // Завершаем обновление
    onRefreshComplete()
}
