package com.example.diplomwork.presentation.ui.screens.settings_screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomwork.R

@Composable
fun SettingItem(
    title: String,
    onClick: () -> Unit,
    isLogoutButton: Boolean = false,
    actionIcon: Int = R.drawable.ic_arrow_right
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 15.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = if (isLogoutButton) Color.Red else Color.LightGray,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .padding(end = 14.dp)
        )
        Icon(
            painterResource(id = actionIcon),
            contentDescription = "action icon",
            tint = if (isLogoutButton) Color.Red else Color.LightGray,
            modifier = Modifier
                .size(
                    if (actionIcon == R.drawable.ic_arrow_up_right) 20.dp
                    else 30.dp
                )
                .padding( end =
                    if (actionIcon == R.drawable.ic_arrow_up_right) 6.dp
                    else 0.dp
                )
        )
    }
}