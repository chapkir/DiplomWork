package com.example.diplomwork.ui.screens.home_screen

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    onImageClick: (Long, String) -> Unit,
    shouldRefresh: Boolean = false,
    onRefreshComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val refreshScope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // Функция для обновления данных
    fun refreshData() {
        refreshing = true
        refreshScope.launch {
            refreshTrigger++
            Log.d("HomeScreen", "Выполняется обновление данных (триггер: $refreshTrigger)")
            delay(500)
            refreshing = false
            onRefreshComplete()
        }
    }

    // Создаем состояние для PullRefresh
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = { refreshData() }
    )

    // При изменении shouldRefresh из внешнего источника
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            refreshData()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        ContentGrid(
            modifier = Modifier.fillMaxSize(),
            onImageClick = { picture ->
                Log.d("HomeScreen", "Клик на изображение: ID=${picture.id}, URL=${picture.imageUrl}")
                onImageClick(picture.id, picture.imageUrl)
            },
            refreshTrigger = refreshTrigger,
            isRefreshing = refreshing
        )

        // Индикатор загрузки
        PullRefreshIndicator(
            refreshing = refreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = androidx.compose.ui.graphics.Color.White,
            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            scale = true
        )
    }
}
