package com.example.diplomwork.ui.screens.profile_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomwork.R
import com.example.diplomwork.ui.screens.profile_screen.profile_components.Avatar
import com.example.diplomwork.ui.screens.profile_screen.profile_components.StatCard

@Composable
fun ProfileHeader(
    username: String,
    picturesCount: Int,
    avatarUrl: String?,
    isUploading: Boolean = false,
    onAvatarClick: () -> Unit,
    onLogout: () -> Unit,
    avatarUpdateKey: Int,
    isOwnProfile: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Топ бар
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Профиль",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 21.sp,
                modifier = Modifier.padding(start = 20.dp)
            )
            IconButton(
                onClick = {
                    if (isOwnProfile) onLogout()
                    else return@IconButton
                },
                modifier = Modifier
                    .size(41.dp)
                    .padding(end = 20.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }

        // Аватарка, юзернейм
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(start = 25.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isOwnProfile) Avatar(avatarUrl, isUploading, onAvatarClick, avatarUpdateKey)
            else Avatar(avatarUrl, isUploading = false, onAvatarClick = {}, avatarUpdateKey)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 23.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Kiryha",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        ) {
                            append("@")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        ) {
                            append(username)
                        }
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }

        // Достижения
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 20.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatCard("Достижения", "", Modifier.weight(1f))
        }

        // Подписчики, подписки
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatCard("4", "Подписчики", Modifier.weight(1f))
            StatCard("15", "Подписки", Modifier.weight(1f))
            StatCard("$picturesCount", "Картинки", Modifier.weight(1f))
        }

        //Кнопка подписаться
        if(!isOwnProfile) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, bottom = 11.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(0.5f),
                    onClick = {
                    },
                    colors = ButtonColors(
                        containerColor = Color.Red.copy(alpha = 0.9f),
                        contentColor = Color.White,
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Подписаться",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
