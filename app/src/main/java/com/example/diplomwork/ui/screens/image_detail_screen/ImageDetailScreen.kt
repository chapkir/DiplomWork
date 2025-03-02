package com.example.diplomwork.ui.screens.image_detail_screen

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.example.diplomwork.R
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.model.Comment
import com.example.diplomwork.model.CommentRequest
import com.example.diplomwork.network.ApiClient
import com.example.diplomwork.ui.theme.ColorForBottomMenu
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.fillMaxSize

@Composable
fun ImageDetailScreen(
    pinId: Long,
    imageUrl: String,
    navController: NavHostController
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
                navController.navigate("login_screen")
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

    // Get system window insets
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues()

    // Main container that extends edge-to-edge
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorForBottomMenu)
    ) {
        // Content with proper padding to avoid system bars
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorForBottomMenu)
                // Add padding only at the top to account for status bar
                .padding(top = statusBarPadding.calculateTopPadding())
                // We'll handle bottom padding separately
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
                        imageRes = if (imageUrl.startsWith("http")) imageUrl else ApiClient.baseUrl + imageUrl,
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
                                        Toast.makeText(context, "Необходима авторизация", Toast.LENGTH_SHORT).show()
                                        navController.navigate("login_screen")
                                        return@launch
                                    }

                                    // Update UI state immediately for better UX
                                    val wasLiked = isLiked
                                    isLiked = !wasLiked
                                    likesCount = if (!wasLiked) likesCount + 1 else maxOf(0, likesCount - 1)

                                    Log.d("ImageDetailScreen", "Updating like status with token: Bearer $token")

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
                                            Log.e("ImageDetailScreen", "API call failed with code: ${response.code()}")
                                            isLiked = wasLiked
                                            likesCount = if (wasLiked) likesCount + 1 else maxOf(0, likesCount - 1)
                                            Toast.makeText(context, "Ошибка при обновлении лайка (код ${response.code()})", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Log.d("ImageDetailScreen", "Like status updated successfully")
                                        }
                                    } catch (e: retrofit2.HttpException) {
                                        // Revert UI state if API call fails
                                        isLiked = wasLiked
                                        likesCount = if (wasLiked) likesCount + 1 else maxOf(0, likesCount - 1)

                                        val errorCode = e.code()
                                        val errorBody = e.response()?.errorBody()?.string()
                                        Log.e("ImageDetailScreen", "HTTP Error updating like: $errorCode, Body: $errorBody")
                                        Toast.makeText(context, "Ошибка при обновлении лайка (код $errorCode)", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        // Revert UI state if API call fails
                                        isLiked = wasLiked
                                        likesCount = if (wasLiked) likesCount + 1 else maxOf(0, likesCount - 1)

                                        Log.e("ImageDetailScreen", "Error updating like: ${e.message}", e)
                                        Toast.makeText(context, "Ошибка при обновлении лайка: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Log.e("ImageDetailScreen", "General error: ${e.message}", e)
                                    Toast.makeText(context, "Общая ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onCommentClick = {
                            showCommentsSheet = true
                        }
                    )
                }

                // Show a preview of comments (just a few)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            // Add bottom padding to account for navigation bar
                            .padding(bottom = navigationBarPadding.calculateBottomPadding())
                    ) {
                        if (comments.isNotEmpty()) {
                            Text(
                                text = "Комментарии",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Show only first 2 comments as preview
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

        // Back button - positioned with proper padding for status bar
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(
                    top = statusBarPadding.calculateTopPadding() + 16.dp,
                    start = 16.dp
                )
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .align(Alignment.TopStart)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = "Назад",
                tint = Color.White
            )
        }

        // Comments bottom sheet
        CommentsBottomSheet(
            show = showCommentsSheet,
            comments = comments,
            onDismiss = { showCommentsSheet = false },
            onAddComment = { commentText ->
                scope.launch {
                    try {
                        val token = sessionManager.getAuthToken() ?: run {
                            Toast.makeText(context, "Необходима авторизация", Toast.LENGTH_SHORT).show()
                            navController.navigate("login_screen")
                            return@launch
                        }

                        Log.d("ImageDetailScreen", "Adding comment with token: Bearer $token")

                        try {
                            val comment = CommentRequest(text = commentText)
                            val response = ApiClient.apiService.addComment(pinId, comment)
                            // Update comments list
                            comments = ApiClient.apiService.getComments(pinId)
                                //commentText = "" // Clear input field
                        } catch (e: Exception) {
                            Log.e("ImageDetailScreen", "Error adding comment: ${e.message}")
                            Toast.makeText(context, "Ошибка при добавлении комментария", Toast.LENGTH_SHORT).show()
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
        // Description
        Text(
            text = description,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Like button with count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onLikeClick() }
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isLiked) R.drawable.ic_favs_filled else R.drawable.ic_favs
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

            // Comment button with count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onCommentClick() }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_info), // Using ic_info as a comment icon
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
            .padding(vertical = 4.dp),
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
    // Only render the bottom sheet when show is true
    if (show) {
        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp.dp
        val sheetHeight = screenHeight * 0.8f

        // Use a state to track if animation has started
        var animationStarted by remember { mutableStateOf(false) }

        // Start with sheet at bottom of screen, then animate to final position
        val offsetY by animateDpAsState(
            targetValue = if (animationStarted) screenHeight * 0.2f else screenHeight,
            animationSpec = tween(
                durationMillis = 300,
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            )
        )

        // Fade in the overlay
        val overlayAlpha by animateFloatAsState(
            targetValue = if (animationStarted) 0.5f else 0f,
            animationSpec = tween(durationMillis = 300)
        )

        // Trigger animation after composition
        LaunchedEffect(key1 = true) {
            animationStarted = true
        }

        // Semi-transparent background overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = overlayAlpha))
                .clickable { onDismiss() }
        )

        // The sheet itself
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
        // Handle to indicate draggable sheet
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Комментарии",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            IconButton(onClick = onDismiss) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Закрыть",
                    tint = Color.White
                )
            }
        }

        Divider(
            color = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Comments list
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

        // Add comment section
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
