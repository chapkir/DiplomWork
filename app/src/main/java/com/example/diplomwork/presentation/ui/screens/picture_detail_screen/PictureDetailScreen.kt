package com.example.diplomwork.presentation.ui.screens.picture_detail_screen

import android.widget.Toast
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.diplomwork.presentation.system_settings.systemInsetHeight
import com.example.diplomwork.presentation.ui.components.CommentItem
import com.example.diplomwork.presentation.ui.components.CommentsBottomSheet
import com.example.diplomwork.presentation.ui.components.LoadingSpinnerForScreen
import com.example.diplomwork.presentation.ui.navigation.PictureDetailScreenData
import com.example.diplomwork.presentation.ui.theme.IconPrimary
import com.example.diplomwork.util.AppConstants
import com.example.diplomwork.presentation.viewmodel.PictureDetailScreenViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PictureDetailScreen(
    pictureDetailScreenData: PictureDetailScreenData,
    onNavigateBack: () -> Unit,
    onProfileClick: (Long?, String) -> Unit,
    viewModel: PictureDetailScreenViewModel = hiltViewModel()
) {

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val openSheet = { coroutineScope.launch { sheetState.show() } }

    val uiState by viewModel.uiState.collectAsState()
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
                    aspectRatio = uiState.aspectRatio
                )
            }

            item {
                ActionBar(
                    description = uiState.pictureDescription,
                    likesCount = uiState.likesCount,
                    isLiked = uiState.isLiked,
                    commentsCount = uiState.commentsCount,
                    profileImageUrl = uiState.profileImageUrl,
                    username = uiState.pictureUsername,
                    userId = uiState.pictureUserId,
                    onLikeClick = { viewModel.toggleLike() },
                    onCommentClick = { openSheet() },
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
                                onClick = { openSheet() },
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
                top = 10.dp,
                start = 16.dp
            )
            .size(43.dp)
            .clip(CircleShape)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_back),
            contentDescription = "back",
            tint = IconPrimary
        )
    }

    if (uiState.isCurrentUserOwner) {
        IconButton(
            onClick = { viewModel.deletePicture() },
            modifier = Modifier
                .padding(
                    top = 16.dp,
                    start = 70.dp
                )
                .size(43.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_trash),
                contentDescription = "trash",
                tint = IconPrimary
            )
        }
    }

    if (sheetState.isVisible) {
        CommentsBottomSheet(
            comments = uiState.comments,
            onDismiss = { },
            onAddComment = { commentText -> viewModel.addComment(commentText) },
            sheetState = sheetState
        )
    }
}