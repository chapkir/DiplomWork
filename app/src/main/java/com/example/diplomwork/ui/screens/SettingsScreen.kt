package com.example.diplomwork.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
                .height(56.dp)
                .padding(horizontal = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_left),
                    contentDescription = "OnBack",
                    tint = Color.White
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Настройки",
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.width(48.dp)) // для симметрии
        }

        Divider()

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            SettingItem("Редактировать профиль", onEditProfileClick)
            SettingItem("Управление аккаунтом", onAccountManagementClick)
            SettingItem("Приватность", onPrivacyClick)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingItem("Справочный центр", onHelpCenterClick)
            SettingItem("Политика конфиденциальности", onPrivacyPolicyClick)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingItem("Выйти из аккаунта", onLogoutClick, isDestructive = true)
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        color = if (isDestructive) Color.Red else Color.Gray
    )
}
