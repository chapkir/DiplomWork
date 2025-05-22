package com.example.diplomwork.presentation.ui.components.spot_card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.diplomwork.R
import com.example.diplomwork.presentation.ui.theme.DividerDark
import kotlinx.coroutines.Job

@Composable
fun SpotCardHeader(
    onProfileClick: () -> Unit,
    userProfileImageUrl: String?,
    username: String,
    openMenuSheet: () -> Job
) {
    Spacer(modifier = Modifier.height(6.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(50))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = userProfileImageUrl ?: R.drawable.default_avatar,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
        Box(
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onProfileClick() },
        ) {
            Text(
                text = username,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
        }
        Box(
            modifier = Modifier
                .size(15.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { openMenuSheet() }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_menu_dots_vertical),
                contentDescription = "Menu",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
    Spacer(modifier = Modifier.height(5.dp))
    HorizontalDivider(thickness = 2.dp, color = DividerDark)
    Spacer(modifier = Modifier.height(12.dp))
}