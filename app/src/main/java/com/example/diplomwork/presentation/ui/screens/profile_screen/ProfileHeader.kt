package com.example.diplomwork.presentation.ui.screens.profile_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomwork.presentation.ui.components.LoadingSpinnerForElement
import com.example.diplomwork.presentation.ui.screens.profile_screen.profile_components.Avatar
import com.example.diplomwork.presentation.ui.screens.profile_screen.profile_components.StatCard
import com.example.diplomwork.presentation.ui.screens.profile_screen.profile_components.StatRow
import com.example.diplomwork.presentation.viewmodel.FollowState

@Composable
fun ProfileHeader(
    userId: Long,
    username: String,
    firstName: String,
    spotsCount: Int,
    followingCount: Int,
    followersCount: Int,
    avatarUrl: String?,
    isUploading: Boolean = false,
    onAvatarClick: () -> Unit,
    followState: FollowState,
    onSubscribe: (Long) -> Unit,
    onUnsubscribe: (Long) -> Unit,
    avatarUpdateKey: Int,
    isOwnProfile: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 7.dp, end = 7.dp, bottom = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Топ бар

        Spacer(modifier = Modifier.height(21.dp))
        // Аватарка, юзернейм
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 25.dp)
                .height(72.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isOwnProfile) Avatar(avatarUrl, isUploading, onAvatarClick, avatarUpdateKey, isOwnProfile)
            else Avatar(avatarUrl, isUploading = false, onAvatarClick = {}, avatarUpdateKey, isOwnProfile)
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
                    color = MaterialTheme.colorScheme.onSecondary,
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
            if (isOwnProfile) {
                StatCard("$followersCount", "Подписчики", Modifier.weight(1f))
                StatCard("$followingCount", "Подписки", Modifier.weight(1f))
                StatCard("$spotsCount", "Места", Modifier.weight(1f))
            }
            else{
                StatRow(
                    followersCount = "$followersCount",
                    followingCount = "$followingCount",
                    spotsCount = "$spotsCount")
            }
        }
        Spacer(modifier = Modifier.height(9.dp))

        //Кнопка подписаться
        if (!isOwnProfile) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            when (followState) {
                is FollowState.Loading -> {
                    Button(
                        modifier = Modifier.height(30.dp),
                        onClick = { },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonColors(
                            containerColor = Color.Gray,
                            contentColor = Color.White,
                            disabledContainerColor = Color.Gray,
                            disabledContentColor = Color.White
                        )
                    ) {
                        LoadingSpinnerForElement()
                    }
                }
                is FollowState.Success -> {
                    val isSubscribed = followState.isSubscribed

                    Button(
                        modifier = Modifier.height(30.dp),
                        onClick = { if(isSubscribed) onUnsubscribe(userId) else onSubscribe(userId) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonColors(
                            containerColor =
                                if (isSubscribed) Color.Gray else Color.Red.copy(alpha = 0.9f),
                            contentColor = Color.White,
                            disabledContainerColor = Color.Gray,
                            disabledContentColor = Color.White
                        )
                    ) {
                        Text(
                            text = if (isSubscribed) "Отписаться" else "Подписаться",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                is FollowState.Error -> {
                    Text(
                        text = "Ошибка, попробуйте позже",
                        color = Color.Red
                    )
                }

                FollowState.Idle -> {
                    Button(
                        modifier = Modifier.height(30.dp),
                        onClick = { onSubscribe(userId) },
                        shape = RoundedCornerShape(12.dp),
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
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            }
            Spacer(modifier = Modifier.height(9.dp))
        }
    }
}
