package com.example.diplomwork.presentation.ui.screens.profile_screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.diplomwork.data.model.PictureResponse
import com.example.diplomwork.data.model.PostResponse
import com.example.diplomwork.presentation.ui.components.CustomTabPager
import com.example.diplomwork.presentation.ui.components.LoadingSpinnerForElement
import com.example.diplomwork.presentation.ui.components.LoadingSpinnerForScreen
import com.example.diplomwork.presentation.ui.components.PictureCard
import com.example.diplomwork.presentation.viewmodel.ProfileViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit,
    onBack: () -> Unit,
    onImageClick: (Long) -> Unit,
    onMapOpen: () -> Unit = {},
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val profileData by profileViewModel.profileData.collectAsState()
    val profilePictures by profileViewModel.profilePictures.collectAsState()
    val profilePosts by profileViewModel.profilePosts.collectAsState()
    val likedPictures by profileViewModel.likedPictures.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()

    val isLoadingPosts by profileViewModel.isLoadingPosts.collectAsState()
    val isLoadingPictures by profileViewModel.isLoadingPictures.collectAsState()
    val isLoadingLiked by profileViewModel.isLoadingLiked.collectAsState()

    val isUploading by profileViewModel.isUploading.collectAsState()
    val error by profileViewModel.error.collectAsState()
    val avatarUpdateCounter by profileViewModel.avatarUpdateCounter.collectAsState()
    val isOwnProfile by profileViewModel.isOwnProfile.collectAsState()

    val selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Места", "Избранное")

    val pagerState = rememberPagerState(initialPage = selectedTabIndex)

    val stateRefresh = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        when (pagerState.currentPage) {
            //0 -> profileViewModel.loadProfilePosts()
            0 -> profileViewModel.loadProfilePictures()
            1 -> profileViewModel.loadLikedPictures()
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { profileViewModel.uploadAvatarToServer(it, context) } }

    PullToRefreshBox(
        isRefreshing =
            when (pagerState.currentPage) {
                //0 -> isLoadingPosts
                0 -> isLoadingPictures
                1 -> isLoadingLiked
                else -> isLoading
            },
        onRefresh = {
            isRefreshing = true
            when (pagerState.currentPage) {
                //0 -> profileViewModel.refreshPosts()
                0 -> profileViewModel.refreshPictures()
                1 -> profileViewModel.refreshLikesPictures()
            }
        },
        state = stateRefresh,
        indicator = {
            Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing =
                    when (pagerState.currentPage) {
                    //0 -> isLoadingPosts
                    0 -> isLoadingPictures
                    1 -> isLoadingLiked
                    else -> isLoading
                },
                containerColor = Color.Gray,
                color = Color.White,
                state = stateRefresh
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when {
                isLoading -> LoadingSpinnerForScreen()
                error != null -> ErrorScreen(error) { profileViewModel.loadLikedPictures() }
                profileData != null -> {
                    ProfileHeader(
                        username = profileData?.username ?: "Неизвестный",
                        firstName = profileData?.firstName ?: "Неизвестный",
                        picturesCount = profileData?.pinsCount ?: 0,
                        followingCount = profileData?.followingCount ?: 0,
                        followersCount = profileData?.followersCount ?: 0,
                        avatarUrl = profileData?.profileImageUrl,
                        isUploading = isUploading,
                        onAvatarClick = { pickImageLauncher.launch("image/*") },
                        onSettingsClick = onSettingsClick,
                        onSubscribe = {  },
                        onUnsubscribe = {},
                        onBack = onBack,
                        avatarUpdateKey = avatarUpdateCounter,
                        isOwnProfile = isOwnProfile,
                        onMapOpen = onMapOpen
                    )

                    CustomTabPager(
                        tabTitles = tabTitles,
                        pagerState = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        lineOffset = 2.37
                    ) { page ->
                        when (page) {
                            //0 -> PostsGrid(profilePosts, isLoadingPosts)
                            0 -> PicturesGrid(profilePictures, onImageClick, isLoadingPictures)
                            1 -> PicturesGrid(likedPictures, onImageClick, isLoadingLiked)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorScreen(error: String?, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Ошибка загрузки: $error",
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("Повторить") }
        }
    }
}

@Composable
private fun PicturesGrid(
    pictures: List<PictureResponse>,
    onPictureClick: (Long) -> Unit,
    isLoading: Boolean
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(pictures, key = { it.id }) { picture ->
            PictureCard(
                imageUrl = picture.imageUrl,
                username = picture.username,
                userProfileImageUrl = picture.userProfileImageUrl,
                id = picture.id,
                aspectRatio = picture.aspectRatio ?: 1f,
                onPictureClick = { onPictureClick(picture.id) },
                contentPadding = 3,
                screenName = "Profile"
            )
        }
    }
}

@Composable
private fun PostsGrid(
    posts: List<PostResponse>,
    isLoading: Boolean
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
    ) {
        itemsIndexed(items = posts, key = { _, post -> post.id }) { _, post ->
            if (isLoading) LoadingSpinnerForElement()
            else
                PictureCard(
                    imageUrl = post.imageUrl!!,
                    username = post.username,
                    userProfileImageUrl = post.userAvatar,
                    id = post.id,
                    aspectRatio = 1f,
                    onPictureClick = { },
                    contentPadding = 3,
                    screenName = "Profile"
                )
        }
    }
}