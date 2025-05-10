package com.example.diplomwork.presentation.ui.screens.profile_screen

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
import com.example.diplomwork.presentation.ui.screens.profile_screen.profile_components.Avatar
import com.example.diplomwork.presentation.ui.screens.profile_screen.profile_components.StatCard

@Composable
fun ProfileHeader(
    username: String,
    firstName: String,
    picturesCount: Int,
    followingCount: Int,
    followersCount: Int,
    avatarUrl: String?,
    isUploading: Boolean = false,
    onAvatarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSubscribe: () -> Unit,
    onUnsubscribe: () -> Unit,
    onBack: () -> Unit,
    avatarUpdateKey: Int,
    isOwnProfile: Boolean = false,
    onMapOpen: () -> Unit
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
                .padding(top = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isOwnProfile) {
                IconButton(
                    onClick = { onMapOpen() },
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .size(23.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_stats),
                        contentDescription = "Stats",
                        tint = Color.White
                    )
                }
            } else {
                IconButton(
                    onClick = { onBack() },
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .size(35.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_left),
                        contentDescription = "OnBack",
                        tint = Color.White
                    )
                }
            }
            IconButton(
                onClick = {
                    if (isOwnProfile) onSettingsClick()
                    else return@IconButton
                },
                modifier = Modifier
                    .padding(end = 15.dp)
                    .size(23.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(21.dp))
        // Аватарка, юзернейм
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 25.dp)
                .height(72.dp),
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
                    text = firstName,
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
        Spacer(modifier = Modifier.height(12.dp))

//        // Достижения
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(start = 10.dp, end = 10.dp, top = 20.dp, bottom = 10.dp),
//            horizontalArrangement = Arrangement.spacedBy(10.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            StatCard("Достижения", "", Modifier.weight(1f))
//        }

        // Подписчики, подписки
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatCard("$followersCount", "Подписчики", Modifier.weight(1f))
            StatCard("$followingCount", "Подписки", Modifier.weight(1f))
            StatCard("$picturesCount", "Места", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(9.dp))

        //Кнопка подписаться
        if (!isOwnProfile) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, bottom = 11.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(0.5f),
                    onClick = { onSubscribe() },
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
            Spacer(modifier = Modifier.height(9.dp))
        }
    }
}
