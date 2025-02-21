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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.example.diplomwork.R
import com.example.diplomwork.network.ApiClient
import com.example.diplomwork.ui.theme.ColorForBottomMenu
import com.google.accompanist.systemuicontroller.rememberSystemUiController

//@Composable
//fun ImageDetailScreen1(
//    imageUrl: String,
//    initialLikesCount: Int,
//    initialComments: List<String>,
//    onLikeClick: () -> Unit,
//    onCommentSubmit: (String) -> Unit
//) {
//    var commentText by remember { mutableStateOf("") }
//    var showCommentInput by remember { mutableStateOf(false) }
//    var liked by remember { mutableStateOf(false) }
//    val likeCount = remember { mutableStateOf(initialLikesCount) }
//    val commentsList = remember { mutableStateOf(initialComments) }
//
//    val finalUrl = if (imageUrl.startsWith("http")) imageUrl else ApiClient.BASE_URL + imageUrl
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        AsyncImage(
//            model = finalUrl,
//            contentDescription = null,
//            contentScale = ContentScale.Crop,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(300.dp)
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(
//            text = "❤️ ${likeCount.value} лайков",
//            style = MaterialTheme.typography.bodyLarge
//        )
//        Button(onClick = {
//            liked = !liked
//            if (liked) {
//                likeCount.value += 1
//            } else {
//                likeCount.value = maxOf(likeCount.value - 1, 0)
//            }
//            onLikeClick()
//        }) {
//            Text(text = if (liked) "Отменить лайк" else "Лайк")
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(text = "Комментарии:", style = MaterialTheme.typography.bodyMedium)
//        commentsList.value.forEach { comment ->
//            Text(text = comment, style = MaterialTheme.typography.bodySmall)
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//        Button(onClick = { showCommentInput = !showCommentInput }) {
//            Text(text = if (showCommentInput) "Скрыть комментарий" else "Добавить комментарий")
//        }
//        if (showCommentInput) {
//            Box(modifier = Modifier
//                .fillMaxWidth()
//                .padding(top = 8.dp)
//                .zIndex(1f)
//            ) {
//                BasicTextField(
//                    value = commentText,
//                    onValueChange = { commentText = it },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .border(1.dp, Color.Gray)
//                        .padding(8.dp),
//                    textStyle = TextStyle(color = Color.Black)
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                Button(
//                    onClick = {
//                        if (commentText.isNotBlank()) {
//                            commentsList.value = commentsList.value + commentText
//                            onCommentSubmit(commentText)
//                            commentText = ""
//                            showCommentInput = false
//                        }
//                    },
//                    modifier = Modifier.padding(top = 8.dp)
//                ) {
//                    Text(text = "Отправить")
//                }
//            }
//        }
//    }
//}



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
    val systemUiController = rememberSystemUiController()
    systemUiController.setStatusBarColor(ColorForBottomMenu)

    var aspectRatio by remember { mutableStateOf(1f) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorForBottomMenu)
    ) {

        item {
            ImageView(imageRes = finalUrl, aspectRatio = aspectRatio)
        }

        item {
            LikeSection(likeCount)
        }

        item {
            CommentSection()
            Spacer(modifier = Modifier.size(30.dp))
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
fun LikeSection(likesCount: MutableState<Int>) {

    var isLiked by remember { mutableStateOf(false) }
    val toggleLike = { isLiked = !isLiked }
    val likedIcon = if (isLiked) painterResource(id = R.drawable.ic_favs_filled)
    else painterResource(id = R.drawable.ic_favs)

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
                text = "*Название изображения*",
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
                        painter = likedIcon,
                        contentDescription = "Лайк",
                        tint = if (isLiked) Color.Red else Color.Gray,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Text(
                    text = "$likesCount",
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
fun CommentSection() {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp, 0.dp)
                .height(150.dp)
                .background(Color.Black)
        ) {
            item {
                CommentItem(username = "Пользователь 1", comment = "Это отличный снимок!")
                CommentItem(username = "Пользователь 1", comment = "Это отличный снимок!")
                CommentItem(username = "Пользователь 1", comment = "Это отличный снимок!")
                CommentItem(username = "Пользователь 1", comment = "Это отличный снимок!")
                CommentItem(username = "Пользователь 2", comment = "Очень красиво!")
                CommentItem(username = "Пользователь 2", comment = "Очень красиво!")
                CommentItem(username = "Пользователь 2", comment = "Очень красиво!")
            }
        }

        AddCommentField()
    }
}

@Composable
fun CommentItem(username: String, comment: String) {
    Column(modifier = Modifier.padding(5.dp)) {
        Text(
            text = username, style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
        Text(
            text = comment, style = MaterialTheme.typography.bodyMedium,
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
