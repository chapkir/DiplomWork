package com.example.diplomwork.ui.screens.profile_screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.diplomwork.model.PictureResponse
import com.example.diplomwork.model.PostResponse
import com.example.diplomwork.ui.components.CustomTabPager
import com.example.diplomwork.ui.components.LoadingSpinnerForScreen
import com.example.diplomwork.ui.components.PictureCard
import com.example.diplomwork.ui.theme.BgProfile
import com.example.diplomwork.viewmodel.ProfileViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit,
    onBack: () -> Unit,
    onImageClick: (Long, String) -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val profileData by profileViewModel.profileData.collectAsState()
    val likedPictures by profileViewModel.likedPictures.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val isUploading by profileViewModel.isUploading.collectAsState()
    val error by profileViewModel.error.collectAsState()
    val avatarUpdateCounter by profileViewModel.avatarUpdateCounter.collectAsState()
    val isOwnProfile by profileViewModel.isOwnProfile.collectAsState()

    val selectedTabIndex by remember { mutableIntStateOf(0) }
    var onTabSelected by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Посты", "Картинки", "Избранное")

    val ownPictures = profileData?.pins ?: emptyList()
    val ownPosts = profileData?.posts ?: emptyList()

    val pagerState = rememberPagerState(initialPage = selectedTabIndex)

    LaunchedEffect(pagerState.currentPage) {
        onTabSelected = pagerState.currentPage
    }

    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 1) profileViewModel.loadLikedPictures()
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { profileViewModel.uploadAvatarToServer(it, context) } }

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
                    onSubscribe = {},
                    onUnsubscribe = {},
                    onBack = onBack,
                    avatarUpdateKey = avatarUpdateCounter,
                    isOwnProfile = isOwnProfile
                )

                CustomTabPager(
                    tabTitles = tabTitles,
                    pagerState = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    lineOffset = 2.37
                ) { page ->
                    when (page) {
                        0 -> PostsGrid(ownPosts)
                        1 -> PicturesGrid(ownPictures, onImageClick)
                        2 -> PicturesGrid(likedPictures, onImageClick)
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
private fun PicturesGrid(pictures: List<PictureResponse>, onPictureClick: (Long, String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(pictures, key = { it.id }) { picture ->
            PictureCard(
                imageUrl = picture.imageUrl,
                username = picture.username,
                userProfileImageUrl = picture.userProfileImageUrl,
                id = picture.id,
                onPictureClick = { onPictureClick(picture.id, picture.imageUrl) },
                contentPadding = 3,
                screenName = "Profile"
            )
        }
    }
}

@Composable
private fun PostsGrid(posts: List<PostResponse>) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
    ) {
        itemsIndexed(items = posts, key = { _, post -> post.id }) { _, post ->
            PictureCard(
                imageUrl = post.imageUrl!!,
                username = post.username,
                userProfileImageUrl = post.userAvatar,
                id = post.id,
                onPictureClick = { },
                contentPadding = 3,
                screenName = "Profile"
            )
        }
    }
}