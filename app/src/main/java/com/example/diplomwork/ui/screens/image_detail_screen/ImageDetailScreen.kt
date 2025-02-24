package com.example.diplomwork.ui.screens.image_detail_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.example.diplomwork.R
import com.example.diplomwork.model.Comment
import com.example.diplomwork.network.ApiClient
import com.example.diplomwork.ui.theme.ColorForBottomMenu
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.Scaffold

@Composable
fun ImageDetailScreen(
    imageUrl: String,
    initialLikesCount: Int,
    initialComments: List<Comment>,
    onLikeClick: () -> Unit,
    onCommentSubmit: (String) -> Unit
) {
    var pinDescription by remember { mutableStateOf("") }
    val likesCountState = remember { mutableStateOf(initialLikesCount) }
    var comments by remember { mutableStateOf(initialComments) }
    var isLoading by remember { mutableStateOf(true) }
    var commentText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(imageUrl) {
        try {
            val pins = ApiClient.apiService.getPins()
            val pin = pins.find { it.imageUrl == imageUrl }
            pin?.let {
                pinDescription = it.description
                likesCountState.value = it.likesCount
                comments = it.comments
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val finalUrl =
            if (imageUrl.startsWith("http")) imageUrl else ApiClient.baseUrl + imageUrl


        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorForBottomMenu)
        ) {
            item {
                ImageView(imageRes = finalUrl, aspectRatio = 1f)
            }
            item {
                LikeSection(
                    description = pinDescription,
                    likesCount = likesCountState,
                    onLikeClick = onLikeClick
                )
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Комментарии",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = ColorForBottomMenu.copy(alpha = 0.8f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .verticalScroll(rememberScrollState())
                                .padding(10.dp)
                        ) {
                            comments.forEach { comment ->
                                CommentItem(username = comment.username, comment = comment.text)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Добавить комментарий", color = Color.Gray) },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    onCommentSubmit(commentText)
                                    comments = comments + Comment(
                                        id = System.currentTimeMillis(),
                                        text = commentText,
                                        username = "CurrentUser"
                                    )
                                    commentText = ""
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                                .padding(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_uparrow),
                                contentDescription = "Отправить комментарий",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun ImageView(imageRes: String, aspectRatio: Float) {
    var currentAspectRatio by remember { mutableStateOf(aspectRatio) }

    Card(
        shape = RoundedCornerShape(30.dp),
        elevation = CardDefaults.cardElevation(15.dp),
        modifier = Modifier
            .padding(top = 37.dp, start = 7.dp, end = 7.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageRes)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                onState = { state ->
                    if (state is AsyncImagePainter.State.Success) {
                        val size = state.painter.intrinsicSize
                        if (size.width > 0 && size.height > 0) {
                            currentAspectRatio = size.width / size.height
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(currentAspectRatio)
                    .clip(RoundedCornerShape(12.dp))
            )

            if (currentAspectRatio == 0f) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun LikeSection(description: String, likesCount: MutableState<Int>, onLikeClick: () -> Unit) {
    var isLiked by remember { mutableStateOf(false) }
    val toggleLike = {
        isLiked = !isLiked
        if (isLiked) {
            likesCount.value++
        } else {
            likesCount.value = maxOf(likesCount.value - 1, 0)
        }
        onLikeClick()
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = description,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = toggleLike) {
                    Icon(
                        painter = if (isLiked)
                            painterResource(id = R.drawable.ic_favs_filled)
                        else painterResource(id = R.drawable.ic_favs),
                        contentDescription = "Лайк",
                        tint = if (isLiked) Color.Red else Color.Gray,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    text = "${likesCount.value}",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 13.sp,
                    color = Color.White,
                    modifier = Modifier.padding(start = 2.dp, end = 7.dp)
                )
            }
        }
    }
}



@Composable
fun CommentItem(username: String, comment: String) {
    Column(modifier = Modifier.padding(5.dp)) {
        Text(
            text = username,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
        Text(
            text = comment,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}

@Composable
fun AddCommentField() {
    var commentText by remember { mutableStateOf(TextFieldValue("")) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = commentText,
            onValueChange = { commentText = it },
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
                .height(40.dp)
                .background(Color.LightGray, shape = CircleShape),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.padding(8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (commentText.text.isEmpty()) {
                        Text(text = "    Добавить комментарий...")
                    }
                    innerTextField()
                }
            }
        )
        IconButton(
            onClick = { },
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray)
                .padding(4.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_uparrow),
                contentDescription = "Отправить комментарий",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun LocalTextStyleCompat(): TextStyle {
    return MaterialTheme.typography.bodyMedium
}
