package com.example.diplomwork.ui.main_screen.content_grid

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

@Composable
fun ContentGrid(modifier: Modifier = Modifier) {
    val images = listOf(
        R.drawable.testimg1,
        R.drawable.testimg2,
        R.drawable.testimg3,
        R.drawable.testimg4,
        R.drawable.testimg5,
        R.drawable.testimg7,
        R.drawable.testimg6
    )

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.padding(top = 80.dp, bottom = 60.dp),
        contentPadding = PaddingValues(10.dp), // отступ со всех сторон картинки
        //verticalArrangement = Arrangement.spacedBy(8.dp),
        //horizontalArrangement = Arrangement.spacedBy(8.dp) // отступы между картинками
    ) {
        items(images) { imageRes ->
            ImageCard(imageRes)
        }
    }
}