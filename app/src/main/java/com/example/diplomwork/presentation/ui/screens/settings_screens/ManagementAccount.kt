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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.diplomwork.R
import com.example.diplomwork.presentation.viewmodel.SettingsViewModel

@Composable
fun ManagementAccount(
    onBack: () -> Unit,
    viewModel: SettingsViewModel
) {

    val isDeleting by viewModel.isDeleting.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.deleteResult.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            // можно также здесь сделать навигацию назад, если нужно
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        SettingsHeader(onBack = onBack, title = "Управление аккаунтом")

        Text(
            text = "Ваш аккаунт",
            color = Color.Gray,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 5.dp)
        )
        SettingItem(
            title = "Персональные данные",
            onClick = { }
        )
        SettingItem(
            title = "Адрес эдектронной почты",
            onClick = { }
        )
        SettingItem(
            title = "Изменить пароль",
            onClick = { }
        )

        HorizontalDivider(modifier = Modifier.padding(top = 10.dp))

        Text(
            text = "Деактивация и удаление",
            color = Color.Gray,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 5.dp)
        )

            SettingItem(
                title = "Отключение аккаунта",
                onClick = { }
            )
            Text(
                text = "Отключение аккаунта для временного скрытия мест и профиля",
                color = Color.Gray,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            SettingItem(
                title = "Удаление данных и аккаунта",
                onClick = { viewModel.deleteAccount() }
            )
            Text(
                text = "Безвозвратное удаление данных и всего, что связано с аккаунтом",
                color = Color.Gray,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
    }
}