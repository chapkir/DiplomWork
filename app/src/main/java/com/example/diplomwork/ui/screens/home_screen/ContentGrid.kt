package com.example.diplomwork.ui.screens.home_screen

import android.util.Log
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
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.diplomwork.model.ApiResponse
import com.example.diplomwork.model.PageResponse
import com.example.diplomwork.model.PictureResponse
import com.example.diplomwork.network.ApiClient
import com.example.diplomwork.ui.components.LoadingSpinnerForScreen
import com.example.diplomwork.ui.theme.ColorForBottomMenu
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan

@Composable
fun ContentGrid(
    modifier: Modifier = Modifier,
    onImageClick: (PictureResponse) -> Unit,
    refreshTrigger: Int = 0,
    isRefreshing: Boolean = false,
    searchQuery: String = ""
) {
    var pictures by remember { mutableStateOf<List<PictureResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val refreshScope = rememberCoroutineScope()

    suspend fun loadPictures() {
        try {
            if (!isRefreshing) {
                isLoading = true
            }

            // Выбираем между поиском и обычной загрузкой
            if (searchQuery.isNotEmpty()) {
                Log.d("ContentGrid", "Поиск пинов по запросу: $searchQuery")
                val searchResponse = ApiClient.apiService.searchPictures(searchQuery)
                val searchResults = searchResponse.data.content
                Log.d("ContentGrid", "Найдено ${searchResults.size} пинов по запросу '$searchQuery'")

                pictures = searchResults
            } else {
                Log.d("ContentGrid", "Загрузка пинов...")
                val fetchedPictures = ApiClient.apiService.getPictures()
                Log.d("ContentGrid", "Загружено ${fetchedPictures.size} пинов")
                pictures = fetchedPictures

            }

            isLoading = false
            error = null
        } catch (e: Exception) {
            // Логи
            Log.e("ContentGrid", "Ошибка загрузки данных: ${e.message}", e)

            error = "Ошибка загрузки данных: ${e.message}"
            isLoading = false
        }
    }

    // Первоначальная загрузка данных
    LaunchedEffect(Unit) {
        loadPictures()
    }

    // Загрузка при изменении триггера обновления или поискового запроса
    LaunchedEffect(refreshTrigger, searchQuery) {
        val showLoading = (pictures.isEmpty() || refreshTrigger > 0)
        if (showLoading && !isRefreshing) {
            isLoading = true
        }

        if (searchQuery.isNotEmpty()) {
            Log.d("ContentGrid", "Поиск обновлен с запросом: '$searchQuery'")
        }

        loadPictures()
    }

    // Добавляем информацию о поиске для пользователя
    var searchInfoText by remember { mutableStateOf<String?>(null) }

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
        modifier = modifier.background(ColorForBottomMenu),
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
                        onClick = {
                            error = null
                            isLoading = true
                            refreshScope.launch { loadPictures() }
                        }
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
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
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


                        itemsIndexed(pictures) { _, picture ->
                            Log.d("ContentGrid", "Отображение пина: ID=${picture.id}, URL=${picture.imageUrl}")
                            PictureCard(
                                imageUrl = picture.imageUrl,
                                id = picture.id,
                                onClick = {
                                    // логи
                                    val timestamp = System.currentTimeMillis()
                                    Log.i("NAVIGATION_DEBUG", "$timestamp - GRID: КЛИК НА ПИН С ID=${picture.id}, URL=${picture.imageUrl}")
                                    Log.i("NAVIGATION_DEBUG", "Дополнительные данные пина: Описание=${picture.description.take(20)}, " +
                                            "Автор=${picture.username}, Лайков=${picture.likesCount}")
                                    onImageClick(picture)
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}