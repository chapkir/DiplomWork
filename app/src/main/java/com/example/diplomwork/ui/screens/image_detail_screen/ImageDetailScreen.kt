package com.example.diplomwork.ui.screens.image_detail_screen

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.example.diplomwork.R
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.model.Comment
import com.example.diplomwork.model.CommentRequest
import com.example.diplomwork.network.ApiClient
import com.example.diplomwork.system_settings.SystemInsetHeight
import com.example.diplomwork.ui.theme.ColorForArrowBack
import com.example.diplomwork.ui.theme.ColorForBottomMenu
import kotlinx.coroutines.launch

@Composable
fun ImageDetailScreen(
    pinId: Long,
    imageUrl: String,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    var pinDescription by remember { mutableStateOf("") }
    var likesCount by remember { mutableStateOf(0) }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var commentText by remember { mutableStateOf("") }
    var isLiked by remember { mutableStateOf(false) }
    var showCommentsSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val token = sessionManager.getAuthToken() ?: run {
                onNavigateToLogin()
                return@LaunchedEffect
            }

            try {
                val pin = ApiClient.apiService.getPin(pinId)
                pinDescription = pin.description
                likesCount = pin.likesCount
                isLiked = pin.isLikedByCurrentUser
            } catch (e: Exception) {
                Log.e("ImageDetailScreen", "Error loading pin: ${e.message}")
                Toast.makeText(context, "Ошибка загрузки пина", Toast.LENGTH_SHORT).show()
            }

            try {
                comments = ApiClient.apiService.getComments(pinId)
            } catch (e: Exception) {
                Log.e("ImageDetailScreen", "Error loading comments: ${e.message}")
                Toast.makeText(context, "Ошибка загрузки комментариев", Toast.LENGTH_SHORT).show()
            }

            isLoading = false
        } catch (e: Exception) {
            Log.e("ImageDetailScreen", "General error: ${e.message}")
            Toast.makeText(context, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
        }
    }



    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorForBottomMenu)
            .padding(top = SystemInsetHeight(WindowInsetsCompat.Type.statusBars()).value)
            .imePadding()
    ) {
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        } else {
            item {
                ImageView(
                    imageRes =
                    if (imageUrl.startsWith("http")) imageUrl
                    else ApiClient.baseUrl + imageUrl,
                    aspectRatio = 1f
                )
            }

            item {
                ActionBar(
                    description = pinDescription,
                    likesCount = likesCount,
                    isLiked = isLiked,
                    commentsCount = comments.size,
                    onLikeClick = {
                        scope.launch {
                            try {
                                val token = sessionManager.getAuthToken() ?: run {
                                    Toast.makeText(
                                        context,
                                        "Необходима авторизация",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onNavigateToLogin()
                                    return@launch
                                }

                                // Update UI state immediately for better UX
                                val wasLiked = isLiked
                                isLiked = !wasLiked
                                likesCount =
                                    if (!wasLiked) likesCount + 1 else maxOf(0, likesCount - 1)

                                Log.d(
                                    "ImageDetailScreen",
                                    "Updating like status with token: Bearer $token"
                                )

                                try {
                                    // Make API call
                                    val response = if (wasLiked) {
                                        Log.d("ImageDetailScreen", "Unliking pin $pinId")
                                        ApiClient.apiService.unlikePin(pinId)
                                    } else {
                                        Log.d("ImageDetailScreen", "Liking pin $pinId")
                                        ApiClient.apiService.likePin(pinId)
                                    }

                                    if (!response.isSuccessful) {
                                        // Revert UI state if API call fails
                                        Log.e(
                                            "ImageDetailScreen",
                                            "API call failed with code: ${response.code()}"
                                        )
                                        isLiked = wasLiked
                                        likesCount = if (wasLiked) likesCount + 1 else maxOf(
                                            0,
                                            likesCount - 1
                                        )
                                        Toast.makeText(
                                            context,
                                            "Ошибка при обновлении лайка (код ${response.code()})",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Log.d(
                                            "ImageDetailScreen",
                                            "Like status updated successfully"
                                        )
                                    }
                                } catch (e: retrofit2.HttpException) {
                                    // Revert UI state if API call fails
                                    isLiked = wasLiked
                                    likesCount =
                                        if (wasLiked) likesCount + 1 else maxOf(0, likesCount - 1)

                                    val errorCode = e.code()
                                    val errorBody = e.response()?.errorBody()?.string()
                                    Log.e(
                                        "ImageDetailScreen",
                                        "HTTP Error updating like: $errorCode, Body: $errorBody"
                                    )
                                    Toast.makeText(
                                        context,
                                        "Ошибка при обновлении лайка (код $errorCode)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } catch (e: Exception) {
                                    // Revert UI state if API call fails
                                    isLiked = wasLiked
                                    likesCount =
                                        if (wasLiked) likesCount + 1 else maxOf(0, likesCount - 1)

                                    Log.e(
                                        "ImageDetailScreen",
                                        "Error updating like: ${e.message}",
                                        e
                                    )
                                    Toast.makeText(
                                        context,
                                        "Ошибка при обновлении лайка: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Log.e("ImageDetailScreen", "General error: ${e.message}", e)
                                Toast.makeText(
                                    context,
                                    "Общая ошибка: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    onCommentClick = {
                        showCommentsSheet = true
                    }
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom =
                            SystemInsetHeight(WindowInsetsCompat.Type.navigationBars()).value
                        )
                ) {
                    if (comments.isNotEmpty()) {
                        Text(
                            text = "Комментарии",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontSize = 17.sp,
                            modifier = Modifier.padding(start = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        comments.take(2).forEach { comment ->
                            CommentItem(comment = comment)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (comments.size > 2) {
                            TextButton(
                                onClick = { showCommentsSheet = true },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    text = "Показать все комментарии (${comments.size})",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    IconButton(
        onClick = { onNavigateBack() },
        modifier = Modifier
            .padding(
                top = SystemInsetHeight(WindowInsetsCompat.Type.statusBars()).value + 18.dp,
                start = 16.dp
            )
            .size(43.dp)
            .clip(CircleShape)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_back),
            contentDescription = "Назад",
            tint = ColorForArrowBack
        )
    }

    CommentsBottomSheet(
        show = showCommentsSheet,
        comments = comments,
        onDismiss = { showCommentsSheet = false },
        onAddComment = { commentText ->
            scope.launch {
                try {
                    val token = sessionManager.getAuthToken() ?: run {
                        Toast.makeText(context, "Необходима авторизация", Toast.LENGTH_SHORT).show()
                        onNavigateToLogin()
                        return@launch
                    }

                    Log.d("ImageDetailScreen", "Adding comment with token: Bearer $token")

                    try {
                        val comment = CommentRequest(text = commentText)
                        val response = ApiClient.apiService.addComment(pinId, comment)
                        comments = ApiClient.apiService.getComments(pinId)
                    } catch (e: Exception) {
                        Log.e("ImageDetailScreen", "Error adding comment: ${e.message}")
                        Toast.makeText(
                            context,
                            "Ошибка при добавлении комментария",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e("ImageDetailScreen", "General error: ${e.message}", e)
                    Toast.makeText(
                        context,
                        "Общая ошибка: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    )
}


@Composable
fun ImageView(imageRes: String, aspectRatio: Float) {
    var currentAspectRatio by remember { mutableStateOf(aspectRatio) }

    Card(
        shape = RoundedCornerShape(30.dp),
        elevation = CardDefaults.cardElevation(15.dp),
        modifier = Modifier
            .padding(top = 10.dp, start = 7.dp, end = 7.dp)
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
fun ActionBar(
    description: String,
    likesCount: Int,
    isLiked: Boolean,
    commentsCount: Int,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = description,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            fontSize = 25.sp
        )

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onLikeClick() }
            ) {
                Icon(
                    painter = painterResource(
                        id =
                        if (isLiked) R.drawable.ic_favs_filled
                        else R.drawable.ic_favs
                    ),
                    contentDescription = "Лайк",
                    tint = if (isLiked) Color.Red else Color.White,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = likesCount.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onCommentClick() }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_info),
                    contentDescription = "Комментарии",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = commentsCount.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = comment.username,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun CommentsBottomSheet(
    show: Boolean,
    comments: List<Comment>,
    onDismiss: () -> Unit,
    onAddComment: (String) -> Unit
) {
    if (show) {
        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp.dp
        val sheetHeight = screenHeight * 0.8f

        var animationStarted by remember { mutableStateOf(false) }

        val offsetY by animateDpAsState(
            targetValue = if (animationStarted) screenHeight * 0.2f else screenHeight,
            animationSpec = tween(
                durationMillis = 300,
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            )
        )

        val overlayAlpha by animateFloatAsState(
            targetValue = if (animationStarted) 0.5f else 0f,
            animationSpec = tween(durationMillis = 300)
        )

        LaunchedEffect(key1 = true) {
            animationStarted = true
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = overlayAlpha))
                .clickable { onDismiss() }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(sheetHeight)
                .offset(y = offsetY)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(ColorForBottomMenu)
        ) {
            CommentsContent(
                comments = comments,
                onDismiss = onDismiss,
                onAddComment = onAddComment
            )
        }
    }
}

@Composable
fun CommentsContent(
    comments: List<Comment>,
    onDismiss: () -> Unit,
    onAddComment: (String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Комментарии",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontSize = 20.sp
            )

            IconButton(onClick = onDismiss) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Закрыть",
                    tint = Color.White
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color.White.copy(alpha = 0.2f)
        )

        if (comments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Нет комментариев",
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(comments) { comment ->
                    CommentItem(comment = comment)
                    Spacer(modifier = Modifier.height(8.dp))
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
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (commentText.isNotBlank()) {
                        onAddComment(commentText)
                        commentText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_up),
                    contentDescription = "Отправить",
                    tint = ColorForBottomMenu
                )
            }
        }
    }
}
