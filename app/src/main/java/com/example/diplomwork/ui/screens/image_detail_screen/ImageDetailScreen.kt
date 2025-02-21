package com.example.diplomwork.ui.screens.image_detail_screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.diplomwork.network.ApiClient

@Composable
fun ImageDetailScreen(
    imageUrl: String,
    initialLikesCount: Int,
    initialComments: List<String>,
    onLikeClick: () -> Unit,
    onCommentSubmit: (String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    var showCommentInput by remember { mutableStateOf(false) }
    var liked by remember { mutableStateOf(false) }
    val likeCount = remember { mutableStateOf(initialLikesCount) }
    val commentsList = remember { mutableStateOf(initialComments) }

    val finalUrl = if (imageUrl.startsWith("http")) imageUrl else ApiClient.BASE_URL + imageUrl

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        AsyncImage(
            model = finalUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "❤️ ${likeCount.value} лайков",
            style = MaterialTheme.typography.bodyLarge
        )
        Button(onClick = {
            liked = !liked
            if (liked) {
                likeCount.value += 1
            } else {
                likeCount.value = maxOf(likeCount.value - 1, 0)
            }
            onLikeClick()
        }) {
            Text(text = if (liked) "Отменить лайк" else "Лайк")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Комментарии:", style = MaterialTheme.typography.bodyMedium)
        commentsList.value.forEach { comment ->
            Text(text = comment, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { showCommentInput = !showCommentInput }) {
            Text(text = if (showCommentInput) "Скрыть комментарий" else "Добавить комментарий")
        }
        if (showCommentInput) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .zIndex(1f)
            ) {
                BasicTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.Gray)
                        .padding(8.dp),
                    textStyle = TextStyle(color = Color.Black)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            commentsList.value = commentsList.value + commentText
                            onCommentSubmit(commentText)
                            commentText = ""
                            showCommentInput = false
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(text = "Отправить")
                }
            }
        }
    }
}