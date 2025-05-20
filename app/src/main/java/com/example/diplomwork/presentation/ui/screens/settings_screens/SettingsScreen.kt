package com.example.diplomwork.presentation.ui.screens.settings_screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
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
    onFeedbackClick: () -> Unit,
    onLicensesClick: () -> Unit,
    viewModel: SettingsViewModel
) {
    val logoutState by viewModel.isLogout.collectAsState()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

//    LaunchedEffect(logoutState) {
//        logoutState?.let {
//            it.onSuccess {
//                onLogoutClick()
//            }.onFailure { e ->
//                Toast.makeText(context, "Ошибка выхода из аккаунта: $e", Toast.LENGTH_LONG).show()
//            }
//        }
//    }

    Column(modifier = Modifier.fillMaxSize()) {

        SettingsHeader(onBack = onBack, title = "Настройки")

        Column {
            SettingItem("Редактировать профиль", onEditProfileClick)
            SettingItem("Управление аккаунтом", onAccountManagementClick)
//            SettingItem("Приватность", onPrivacyClick)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingItem(
                title = "Оставьте отзыв",
                onClick = onFeedbackClick,
            )

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

            SettingItem(
                title = "Выйти из аккаунта",
                onClick = {
                    viewModel.logout()
                    onLogoutClick()
                },
                isLogoutButton = true
            )
        }
    }
}
