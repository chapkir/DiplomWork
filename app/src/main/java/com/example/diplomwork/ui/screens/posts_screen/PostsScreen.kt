package com.example.diplomwork.ui.screens.posts_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.diplomwork.R
import com.example.diplomwork.model.PostResponse
import com.example.diplomwork.ui.components.CommentsBottomSheet
import com.example.diplomwork.ui.components.LoadingSpinnerForElement
import com.example.diplomwork.ui.theme.ColorForBackground
import com.example.diplomwork.viewmodel.PostsScreenViewModel

@Composable
fun PostsScreen(
    onProfileClick: (Long?) -> Unit,
    viewModel: PostsScreenViewModel = hiltViewModel()
) {
    val posts by viewModel.posts.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val selectedPostId by viewModel.selectedPostId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showCommentsSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(8.dp)
        ) {
            items(posts) { post ->
                PostCard(
                    post = post,
                    commentsCount = comments.size,
                    onLikeClick = { viewModel.toggleLike(post.id) },
                    onCommentClick = {
                        viewModel.selectPost(post.id)
                        showCommentsSheet = true
                    },
                    onProfileClick
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (isLoading) {
            LoadingSpinnerForElement()
        }

        error?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(16.dp)
            )
        }

        CommentsBottomSheet(
            show = showCommentsSheet,
            comments = comments,
            onDismiss = { showCommentsSheet = false },
            onAddComment = { commentText ->
                selectedPostId.let { postId ->
                    viewModel.addComment(postId, commentText)
                }
            }
        )
    }
}

@Composable
fun PostCard(
    post: PostResponse,
    commentsCount: Int,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onProfileClick: (Long?) -> Unit
) {

    var isImageLoading by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = ColorForBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier
                    .clickable { onProfileClick(post.userId) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = post.userAvatar,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = post.username,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Текст поста
            if (post.text.isNotEmpty()) {
                Text(
                    text = post.text,
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = Color.White
                )
            }

            // Картинка поста
            post.imageUrl?.let { imageUrl ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Post Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize(),
                        onSuccess = { isImageLoading = false },
                        onError = { isImageLoading = false },
                        onLoading = { isImageLoading = true }
                    )

                    if (isImageLoading) {
                        LoadingSpinnerForElement()
                    }
                }
            }

            // Лайки и комментарии
            Row(
                modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLikeClick() }
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (post.isLiked) R.drawable.ic_favs_filled else R.drawable.ic_favs
                        ),
                        contentDescription = "Лайк",
                        tint = if (post.isLiked) Color.Red else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = post.likesCount.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCommentClick() }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_comments),
                        contentDescription = "Комментарии",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = commentsCount.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}
