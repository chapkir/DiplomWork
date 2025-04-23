package com.example.diplomwork.ui.screens.pictures_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableDefaults
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.diplomwork.model.PictureResponse
import com.example.diplomwork.ui.components.LoadingSpinnerForScreen
import com.example.diplomwork.ui.components.PictureCard
import com.example.diplomwork.ui.components.rememberSlowFlingBehavior
import com.example.diplomwork.ui.theme.ColorForBackground
import com.example.diplomwork.viewmodel.PicturesViewModel

@Composable
fun ContentGrid(
    picturesViewModel: PicturesViewModel,
    modifier: Modifier = Modifier,
    onImageClick: (PictureResponse) -> Unit,
    pictures: List<PictureResponse>,
    isLoading: Boolean,
    error: String?,
    refreshTrigger: Int = 0,
    isRefreshing: Boolean = false,
    searchQuery: String = ""
) {
    var searchInfoText by remember { mutableStateOf<String?>(null) }

    // Добавляем информацию о поиске для пользователя
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
        modifier = modifier.background(ColorForBackground),
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
                            // Повторить запрос
                            picturesViewModel.refreshPictures(searchQuery)
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
                            key = {_, picture -> picture.id}
                        ) { _, picture ->
                            PictureCard(
                                imageUrl = picture.imageUrl,
                                username = picture.username,
                                userProfileImageUrl = picture.userProfileImageUrl,
                                id = picture.id,
                                onClick = {
                                    onImageClick(picture)
                                },
                                screenName = "Picture"
                            )
                        }
                    }
                )
            }
        }
    }
}