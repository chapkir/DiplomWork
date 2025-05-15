package com.example.diplomwork.presentation.ui.screens.profile_screen.profile_components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomwork.presentation.ui.theme.BgDefault
import com.example.diplomwork.presentation.ui.theme.BgElevated

@Composable
fun StatCard(
    statValue: String,
    statLabel: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.then(
            Modifier
                .height(60.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(10.dp)
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = statValue,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = statLabel,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}