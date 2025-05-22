package com.example.diplomwork.presentation.ui.components.spot_card

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RatingBar(
    rating: Int,
    starSize: Int = 18,
    screenName: String = ""
) {
    Row {
        if (screenName != "")
        {
            Text(
                text = "Рейтинг -",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
        repeat(5) { index ->
            val starColor = if (index < rating) Color.Yellow else Color.Gray
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Рейтинг",
                tint = starColor,
                modifier = Modifier.size(starSize.dp)
            )
        }
    }
}