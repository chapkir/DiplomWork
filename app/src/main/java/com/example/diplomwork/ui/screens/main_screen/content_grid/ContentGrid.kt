package com.example.diplomwork.ui.screens.main_screen.content_grid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import com.example.diplomwork.model.Pin
import com.example.diplomwork.network.ApiClient
import com.example.diplomwork.system_settings.SystemInsetHeight
import com.example.diplomwork.ui.theme.ColorForBottomMenu
import com.example.diplomwork.ui.theme.Dimens.BottomMenuHeight
import com.example.diplomwork.ui.theme.Dimens.TopBarHeight
import kotlinx.coroutines.launch

@Composable
fun ContentGrid(modifier: Modifier = Modifier, onImageClick: (String) -> Unit) {
    // Состояние для списка пинов, получаемых из API
    var pins by remember { mutableStateOf<List<Pin>>(emptyList()) }

    // Запускаем корутину для получения данных
    LaunchedEffect(Unit) {
        try {
            pins = ApiClient.apiService.getPins()
            println("Получено пинов: ${pins.size}")
        } catch (e: Exception) {
            e.printStackTrace()
            pins = emptyList()
        }
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = modifier
            .padding(
                top = TopBarHeight + SystemInsetHeight(WindowInsetsCompat.Type.statusBars()).value,
                bottom = BottomMenuHeight + SystemInsetHeight(WindowInsetsCompat.Type.navigationBars()).value
            )
            .background(ColorForBottomMenu),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(pins) { pin ->
            ImageCard(
                imageUrl = pin.imageUrl,
                onClick = { onImageClick(pin.imageUrl) }
            )
        }
    }
}