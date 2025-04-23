package com.example.diplomwork.ui.screens.picture_detail_screen

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.diplomwork.R
import com.example.diplomwork.system_settings.systemInsetHeight
import com.example.diplomwork.ui.components.CommentItem
import com.example.diplomwork.ui.components.CommentsBottomSheet
import com.example.diplomwork.ui.components.LoadingSpinnerForScreen
import com.example.diplomwork.ui.navigation.PictureDetailScreenData
import com.example.diplomwork.ui.theme.ColorForArrowBack
import com.example.diplomwork.ui.theme.ColorForBackground
import com.example.diplomwork.util.AppConstants
import com.example.diplomwork.viewmodel.PictureDetailScreenViewModel

@Composable
fun PictureDetailScreen(
    pictureDetailScreenData: PictureDetailScreenData,
    onNavigateBack: () -> Unit,
    onProfileClick: (Long?, String) -> Unit,
    viewModel: PictureDetailScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showCommentsSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (uiState.deleteStatus.isNotEmpty()) {
        Toast.makeText(context, uiState.deleteStatus, Toast.LENGTH_SHORT).show()
        if (uiState.deleteStatus == "Ну тут вроде удаляется, но нужно обновлять страницу") {
            onNavigateBack()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorForBackground)
            .padding(top = systemInsetHeight(WindowInsetsCompat.Type.statusBars()).value)
            .imePadding()
    ) {
        if (uiState.isLoading) {
            item {
                LoadingSpinnerForScreen()
            }
        } else {
            item {
                ImageView(
                    imageRes =
                        if (pictureDetailScreenData.imageUrl.startsWith("http")) pictureDetailScreenData.imageUrl
                        else AppConstants.BASE_URL + pictureDetailScreenData.imageUrl,
                    aspectRatio = 1f
                )
            }

            item {
                ActionBar(
                    description = uiState.pictureDescription,
                    likesCount = uiState.likesCount,
                    isLiked = uiState.isLiked,
                    commentsCount = uiState.comments.size,
                    profileImageUrl = uiState.profileImageUrl,
                    username = uiState.pictureUsername,
                    userId = uiState.pictureUserId,
                    onLikeClick = { viewModel.toggleLike() },
                    onCommentClick = { showCommentsSheet = true },
                    onProfileClick,
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
                    if (uiState.comments.isNotEmpty()) {
                        Text(
                            text = "Комментарии",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontSize = 17.sp,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        uiState.comments.take(2).forEach { comment ->
                            CommentItem(comment = comment)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (uiState.comments.size > 2) {
                            TextButton(
                                onClick = { showCommentsSheet = true },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    text = "Показать все комментарии (${uiState.comments.size})",
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
            contentDescription = "back",
            tint = ColorForArrowBack
        )
    }

    if(uiState.isCurrentUserOwner) {
        IconButton(
            onClick = { viewModel.deletePicture() },
            modifier = Modifier
                .padding(
                    top = systemInsetHeight(WindowInsetsCompat.Type.statusBars()).value + 18.dp,
                    start = 70.dp
                )
                .size(43.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_trash),
                contentDescription = "trash",
                tint = ColorForArrowBack
            )
        }
    }

    CommentsBottomSheet(
        show = showCommentsSheet,
        comments = uiState.comments,
        onDismiss = { showCommentsSheet = false },
        onAddComment = { commentText -> viewModel.addComment(commentText) }
    )
}