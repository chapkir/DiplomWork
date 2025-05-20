package com.example.diplomwork.presentation.ui.screens.settings_screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomwork.presentation.ui.components.bottom_sheets.ConfirmDeleteBottomSheet
import com.example.diplomwork.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagementAccount(
    onBack: () -> Unit,
    isAccountDeleted: () -> Unit,
    onPasswordChange: () -> Unit,
    viewModel: SettingsViewModel
) {

    val isDeleting by viewModel.isDeleting.collectAsState()
    val isDeleted by viewModel.isDeleted.collectAsState()
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    val confirmDeleteSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val openConfirmDeleteSheet = { coroutineScope.launch { confirmDeleteSheetState.show() } }
    val closeConfirmDeleteSheet = { coroutineScope.launch { confirmDeleteSheetState.hide() } }

    LaunchedEffect(Unit) {
        viewModel.deleteResult.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            if (isDeleted) {
                viewModel.logout()
                isAccountDeleted()
            }
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
                onClick = { openConfirmDeleteSheet() }
            )
            Text(
                text = "Безвозвратное удаление данных и всего, что связано с аккаунтом",
                color = Color.Gray,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
    }

    if (confirmDeleteSheetState.isVisible) {
        ConfirmDeleteBottomSheet(
            onDismiss = { closeConfirmDeleteSheet() },
            onDelete = { viewModel.deleteAccount() },
            sheetState = confirmDeleteSheetState,
            message = "Вы дейстительно готовы удалить аккаунт?",
            isDeleteAccount = true
        )
    }
}