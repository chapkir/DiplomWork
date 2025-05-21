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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.diplomwork.R
import kotlinx.coroutines.Job

@Composable
fun SpotCardFooter(
    onProfileClick: () -> Unit,
    userProfileImageUrl: String?,
    username: String,
    openMenuSheet: () -> Job,
    modifier: Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
//        Box(
//            modifier = Modifier
//                .clickable(
//                    indication = null,
//                    interactionSource = remember { MutableInteractionSource() }
//                ) { onProfileClick() },
//        ) {
//            Text(
//                text = username,
//                fontSize = 11.sp,
//                fontWeight = FontWeight.Medium,
//                color = MaterialTheme.colorScheme.onPrimary,
//                textAlign = TextAlign.Center
//            )
//        }
        Box(
            modifier = Modifier
                .size(16.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { openMenuSheet() }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_menu_dots),
                contentDescription = "Menu",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}