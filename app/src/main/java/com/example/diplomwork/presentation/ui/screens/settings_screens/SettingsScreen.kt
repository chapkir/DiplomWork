package com.example.diplomwork.presentation.ui.screens.settings_screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomwork.R

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEditProfileClick: () -> Unit,
    onAccountManagementClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onHelpCenterClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(start = 22.dp)
                    .size(32.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_left),
                    contentDescription = "OnBack",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(22.dp))
            Text(
                text = "Настройки",
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        HorizontalDivider()

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            SettingItem("Редактировать профиль", onEditProfileClick)
            SettingItem("Управление аккаунтом", onAccountManagementClick)
            SettingItem("Приватность", onPrivacyClick)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingItem("Справочный центр", onHelpCenterClick)
            SettingItem("Политика конфиденциальности", onPrivacyPolicyClick)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingItem("Выйти из аккаунта", onLogoutClick, isLogoutButton = true)
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    onClick: () -> Unit,
    isLogoutButton: Boolean = false
) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(vertical = 14.dp),
        color = if (isLogoutButton) Color.Red else Color.LightGray,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
    )
}
