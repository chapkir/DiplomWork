package com.example.diplomwork.presentation.ui.components.top_bar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomwork.presentation.ui.navigation.AddContent
import com.example.diplomwork.presentation.ui.navigation.Notification
import com.example.diplomwork.presentation.ui.navigation.Pictures
import com.example.diplomwork.presentation.ui.navigation.Spots

@Composable
fun GetTopBars(
    currentRoute: String?,
) {
    when (currentRoute) {
        Spots::class.simpleName -> CustomTopBar(title = "Места в городе Санкт-Петербург")
        Pictures::class.simpleName -> CustomTopBar(title = "Лента картинок")
        AddContent::class.simpleName -> CustomTopBar(title = "Добавить")
        Notification::class.simpleName -> CustomTopBar(title = "Уведомления")
        else -> {}
    }
}


@Composable
fun CustomTopBar(
    title: String
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = statusBarHeight),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 17.5.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp, bottom = 12.dp)
        )
    }
}

