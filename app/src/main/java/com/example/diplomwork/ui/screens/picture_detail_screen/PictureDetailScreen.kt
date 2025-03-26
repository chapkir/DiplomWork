package com.example.diplomwork.ui.screens.picture_detail_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import com.example.diplomwork.R
import com.example.diplomwork.network.ApiClient
import com.example.diplomwork.system_settings.systemInsetHeight
import com.example.diplomwork.ui.components.LoadingSpinnerForScreen
import com.example.diplomwork.ui.navigation.ViewPictureDetailScreenData
import com.example.diplomwork.ui.theme.ColorForArrowBack
import com.example.diplomwork.ui.theme.ColorForBottomMenu
import com.example.diplomwork.viewmodel.PictureDetailScreenViewModel

@Composable
fun PictureDetailScreen(
    viewPictureDetailScreenData: ViewPictureDetailScreenData,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: PictureDetailScreenViewModel = remember {
        PictureDetailScreenViewModel(viewPictureDetailScreenData.pictureId)
    }
) {
    val pictureDescription by viewModel.pictureDescription.collectAsState()
    val likesCount by viewModel.likesCount.collectAsState()
    val isLiked by viewModel.isLiked.collectAsState()
    val pictureUsername by viewModel.pictureUsername.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showCommentsSheet by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorForBottomMenu)
            .padding(top = systemInsetHeight(WindowInsetsCompat.Type.statusBars()).value)
            .imePadding()
    ) {
        if (isLoading) {
            item {
                LoadingSpinnerForScreen()
            }
        } else {
            item {
                ImageView(
                    imageRes =
                    if (viewPictureDetailScreenData.imageUrl.startsWith("http")) viewPictureDetailScreenData.imageUrl
                    else ApiClient.getBaseUrl() + viewPictureDetailScreenData.imageUrl,
                    aspectRatio = 1f
                )
            }

            item {
                ActionBar(
                    description = pictureDescription,
                    likesCount = likesCount,
                    isLiked = isLiked,
                    commentsCount = comments.size,
                    avatarUrl = "",
                    username = pictureUsername,
                    onLikeClick = { viewModel.toggleLike() },
                    onCommentClick = { showCommentsSheet = true },
                    onProfileClick = { }
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            bottom =
                            systemInsetHeight(WindowInsetsCompat.Type.navigationBars()).value
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
                top = systemInsetHeight(WindowInsetsCompat.Type.statusBars()).value + 18.dp,
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
        onAddComment = { commentText -> viewModel.addComment(commentText) }
    )
}