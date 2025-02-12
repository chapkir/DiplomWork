package com.example.diplomwork.ui.screens.main_screen.content_grid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridPrefetchStrategy
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.diplomwork.R
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowInsetsCompat
import com.example.diplomwork.system_settings.SystemInsetHeight
import com.example.diplomwork.ui.theme.ColorForBottomMenu
import com.example.diplomwork.ui.theme.Dimens.BottomMenuHeight
import com.example.diplomwork.ui.theme.Dimens.TopBarHeight

@Composable
fun ContentGrid(modifier: Modifier = Modifier, onImageClick: (Int) -> Unit) {

    val images = ImageRepository.images

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier
            .padding(
                top = TopBarHeight +
                        SystemInsetHeight(WindowInsetsCompat.Type.statusBars()).value,
                bottom = BottomMenuHeight +
                        SystemInsetHeight(WindowInsetsCompat.Type.navigationBars()).value
            )
            .background(ColorForBottomMenu),
        //contentPadding = PaddingValues(10.dp), // отступ со всех сторон картинки
        //verticalArrangement = Arrangement.spacedBy(8.dp),
        //horizontalArrangement = Arrangement.spacedBy(8.dp) // отступы между картинками
    ) {
        items(images) { imageRes ->
            ImageCard(
                imageRes = imageRes,
                onClick = { onImageClick(imageRes) } )// Передаем обработчик клика
        }
    }
}