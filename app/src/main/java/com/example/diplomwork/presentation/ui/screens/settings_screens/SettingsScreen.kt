package com.example.diplomwork.presentation.ui.screens.settings_screens

import android.widget.Toast
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.diplomwork.R
import com.example.diplomwork.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEditProfileClick: () -> Unit,
    onAccountManagementClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onHelpCenterClick: () -> Unit,
    onLicensesClick: () -> Unit,
    viewModel: SettingsViewModel
) {
    val logoutState by viewModel.isLogout.collectAsState()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    LaunchedEffect(logoutState) {
        logoutState?.let {
            it.onSuccess {
                onLogoutClick()
            }.onFailure { e ->
                Toast.makeText(context, "Ошибка выхода из аккаунта: $e", Toast.LENGTH_LONG).show()
            }
        }
    }

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

        Column {
            SettingItem("Редактировать профиль", onEditProfileClick)
            SettingItem("Управление аккаунтом", onAccountManagementClick)
            SettingItem("Приватность", onPrivacyClick)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingItem(
                title = "Справочный центр",
                onClick = onHelpCenterClick,
                actionIcon = R.drawable.ic_arrow_up_right
            )

            SettingItem(
                title = "Политика конфиденциальности",
                onClick = { uriHandler.openUri("http://chapkir.ru/privacy.html") },
                actionIcon = R.drawable.ic_arrow_up_right
            )

            SettingItem(
                title = "Пользовательское соглашение",
                onClick = { uriHandler.openUri("http://chapkir.ru/privacy.html") },
                actionIcon = R.drawable.ic_arrow_up_right
            )

            SettingItem(
                title = "Сведения",
                onClick = { onLicensesClick() }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingItem("Выйти из аккаунта", { viewModel.logout() }, isLogoutButton = true)
        }
    }
}
