package com.example.diplomwork.ui.screens.posts_screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.diplomwork.R
import com.example.diplomwork.model.PostResponse
import com.example.diplomwork.ui.components.CommentsBottomSheet
import com.example.diplomwork.ui.components.LoadingSpinnerForElement
import com.example.diplomwork.ui.components.LoadingSpinnerForScreen
import com.example.diplomwork.ui.components.rememberSlowFlingBehavior
import com.example.diplomwork.viewmodel.PostsScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostsScreen(
    onProfileClick: (Long?, String) -> Unit,
    viewModel: PostsScreenViewModel = hiltViewModel()
) {
    val posts by viewModel.posts.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val selectedPostId by viewModel.selectedPostId.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showCommentsSheet by remember { mutableStateOf(false) }

    val stateRefresh = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    var isHeaderVisible by remember { mutableStateOf(true) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                isHeaderVisible = (index == 0 && offset == 0)
            }
    }

    val headerHeight by animateDpAsState(
        targetValue = if (isHeaderVisible) 60.dp else 0.dp,
        animationSpec = tween(durationMillis = 600)
    )

    Column(modifier = Modifier.fillMaxWidth()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .background(Color.Black),
            contentAlignment = Alignment.CenterStart
        ) {
            if (isHeaderVisible) {
                Text(
                    text = "Spotsy",
                    fontWeight = FontWeight.Bold,
                    fontSize = 35.sp,
                    color = Color.White
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = {
                    isRefreshing = true
                    viewModel.refreshPosts()
                },
                state = stateRefresh,
                indicator = {
                    Indicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = isLoading,
                        containerColor = Color.Gray,
                        color = Color.White,
                        state = stateRefresh
                    )
                }
            ) {
                LazyColumn(
                    state = listState,
                    flingBehavior = rememberSlowFlingBehavior(),
                ) {
                    items(posts) { post ->
                        PostCard(
                            post = post,
                            commentsCount = comments[post.id]?.size ?: 0,
                            onLikeClick = { viewModel.toggleLike(post.id) },
                            onCommentClick = {
                                viewModel.selectPost(post.id)
                                viewModel.loadCommentsForPost(post.id)
                                showCommentsSheet = true
                            },
                            onProfileClick,
                            onDeletePost = { postId -> viewModel.deletePost(postId) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            if (isLoading) {
                LoadingSpinnerForScreen()
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
                comments = comments[selectedPostId] ?: emptyList(),
                onDismiss = { showCommentsSheet = false },
                onAddComment = { commentText ->
                    selectedPostId.let { postId ->
                        viewModel.addComment(postId, commentText)
                    }
                })
        }
    }
}

@Composable
fun PostCard(
    post: PostResponse,
    commentsCount: Int,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onProfileClick: (Long?, String) -> Unit,
    onDeletePost: (Long) -> Unit,
) {

    var isImageLoading by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            // Аватарка, юзернейм, настройки
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        onProfileClick(post.userId, post.username)
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = post.userAvatar ?: R.drawable.default_avatar,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = post.username, fontWeight = FontWeight.Bold, color = Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .size(25.dp)
                        .padding(end = 10.dp, top = 2.dp, bottom = 2.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            if (post.isOwnPost) onDeletePost(post.id)
                            else return@clickable
                        }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_menu_dots_vertical),
                        contentDescription = "Menu",
                        tint = Color.White
                    )

                }
            }

            // Текст поста
            if (post.text.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                    var expanded by remember { mutableStateOf(false) }

                    val annotatedText = if (expanded) post.text else post.text.take(100)

                    Text(
                        text = annotatedText,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clickable { expanded = !expanded },
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 16.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                        overflow = TextOverflow.Ellipsis
                    )


                    if (!expanded && post.text.length > 50) {
                        Text(
                            text = "Читать далее",
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .clickable { expanded = true },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray, fontWeight = FontWeight.Medium, fontSize = 14.sp
                            )
                        )
                    }
                }
            } else Spacer(modifier = Modifier.height(10.dp))

            // Картинка поста
            post.imageUrl?.let { imageUrl ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    // 1. Градиентный фон с блюром
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF789AAB),
                                        Color(0xFF09485E),
                                    )
                                )
                            )
                            .blur(50.dp)
                    )
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(300)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .build(),
                        contentDescription = "Post Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        onSuccess = { isImageLoading = false },
                        onError = { isImageLoading = false },
                        onLoading = { isImageLoading = true })

                    if (isImageLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                        ) {
                            LoadingSpinnerForElement(indicatorSize = 50)
                        }
                    }
                }
            }
        }

        // Лайки и комментарии
        Row(
            modifier = Modifier.padding(top = 10.dp, start = 10.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) { onLikeClick() }) {
                Icon(
                    painter = painterResource(
                        id = if (post.isLikedByCurrentUser) R.drawable.ic_favs_filled else R.drawable.ic_favs
                    ),
                    contentDescription = "Лайк",
                    tint = if (post.isLikedByCurrentUser) Color.Red else Color.White,
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
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onCommentClick() }) {
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