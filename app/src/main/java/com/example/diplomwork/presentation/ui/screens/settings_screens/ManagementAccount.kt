package com.example.diplomwork.presentation.ui.screens.settings_screens

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
fun ManagementAccount(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
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
                text = "Управление аккаунтом",
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        HorizontalDivider()

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
                onClick = { }
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