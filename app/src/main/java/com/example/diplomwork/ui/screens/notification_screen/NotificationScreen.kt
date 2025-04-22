package com.example.diplomwork.ui.screens.notification_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.diplomwork.R
import com.example.diplomwork.model.NotificationResponse
import com.example.diplomwork.ui.components.formatDate
import com.example.diplomwork.viewmodel.NotificationViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Composable
fun NotificationScreen(
    onProfile: (Long) -> Unit = {},
    onNotificationContent: (Long) -> Unit = {},
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black)
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
            uiState.error != null -> {
                Text(
                    text = uiState.error!!,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.notifications.isEmpty() -> {
                Text(
                    text = "Нет уведомлений",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                val grouped = groupNotificationsByDate(uiState.notifications)

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    grouped.forEach { (label, group) ->
                        item {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .padding(start = 16.dp, top = 16.dp, bottom = 5.dp)
                            )
                        }
                        items(
                            items = group,
                            key = { it.id }
                        ) { notification ->
                            NotificationItem(
                                notification,
                                onUserClick = { userId -> onProfile(userId) },
                                onNotificationClick = { contentId -> onNotificationContent(contentId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: NotificationResponse,
    onUserClick: (Long) -> Unit,
    onNotificationClick: (Long) -> Unit
) {
    val annotatedText = buildAnnotatedString {
        pushStringAnnotation(tag = "USERNAME", annotation = notification.senderId.toString())
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(notification.senderUsername)
        }
        pop()
        append(" ${notification.message}")
    }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNotificationClick(notification.pinId) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = R.drawable.default_avatar,
            contentDescription = null,
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .clickable { onUserClick(notification.senderId) },
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = annotatedText,
                style = MaterialTheme.typography.bodyMedium,
                onTextLayout = { textLayoutResult = it },
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures { offset ->
                        textLayoutResult?.let { layoutResult ->
                            val position = layoutResult.getOffsetForPosition(offset)
                            annotatedText.getStringAnnotations("USERNAME", position, position)
                                .firstOrNull()?.let {
                                    onUserClick(it.item.toLong())
                                }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = formatDate(notification.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        AsyncImage(
            model = notification.pinImageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(8.dp))
        )
    }
}

fun groupNotificationsByDate(notifications: List<NotificationResponse>): Map<String, List<NotificationResponse>> {
    val now = LocalDate.now()
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    return notifications.groupBy { notif ->
        val dateTime = LocalDateTime.parse(notif.createdAt, formatter)
        val date = dateTime.toLocalDate()
        when {
            date == now -> "Сегодня"
            date == now.minusDays(1) -> "Вчера"
            else -> "Ранее"
        }
    }
}