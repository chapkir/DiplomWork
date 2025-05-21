package com.example.diplomwork.presentation.ui.screens.profile_screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.diplomwork.R
import com.example.diplomwork.data.model.SpotPicturesResponse
import com.example.diplomwork.data.model.SpotResponse
import com.example.diplomwork.presentation.ui.components.CustomTabPager
import com.example.diplomwork.presentation.ui.components.LoadingSpinnerForScreen
import com.example.diplomwork.presentation.ui.components.spot_card.SpotsCard
import com.example.diplomwork.presentation.viewmodel.ProfileViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSettingsClick: () -> Unit,
    onBack: () -> Unit,
    onImageClick: (Long) -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val profileData by profileViewModel.profileData.collectAsState()
    val additionalPictures by profileViewModel.imagesUrls.collectAsState()
    val followersCount by profileViewModel.followersCount.collectAsState()
    val followState by profileViewModel.followState.collectAsState()
    val profilePictures by profileViewModel.profilePictures.collectAsState()
    val likedPictures by profileViewModel.likedPictures.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()

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
                0 -> isLoadingPictures
                1 -> isLoadingLiked
                else -> isLoading
            },
        onRefresh = {
            isRefreshing = true
            when (pagerState.currentPage) {
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, start = 7.dp, end = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isOwnProfile) {
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .padding(start = 15.dp)
                            .size(23.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_stats),
                            contentDescription = "Stats",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                } else {
                    IconButton(
                        onClick = { onBack() },
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(35.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_left),
                            contentDescription = "OnBack",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                IconButton(
                    onClick = {
                        if (isOwnProfile) onSettingsClick()
                        else return@IconButton
                    },
                    modifier = Modifier
                        .padding(end = 15.dp)
                        .size(23.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            when {
                isLoading -> LoadingSpinnerForScreen()

                error.errorLoadProfile != null ->
                    ErrorScreen(error.errorLoadProfile) {
                        profileViewModel.loadLikedPictures()
                    }

                profileData != null -> {
                    ProfileHeader(
                        userId = profileData?.id ?: 0L,
                        username = profileData?.username ?: "Неизвестный",
                        firstName = profileData?.firstName ?: "Неизвестный",
                        picturesCount = profileData?.pinsCount ?: 0,
                        followingCount = profileData?.followingCount ?: 0,
                        followersCount = followersCount,
                        avatarUrl = profileData?.profileImageUrl,
                        isUploading = isUploading,
                        onAvatarClick = { pickImageLauncher.launch("image/*") },
                        followState = followState,
                        onSubscribe = { userId -> profileViewModel.subscribe(userId) },
                        onUnsubscribe = { userId -> profileViewModel.unsubscribe(userId) },
                        avatarUpdateKey = avatarUpdateCounter,
                        isOwnProfile = isOwnProfile,
                    )

                    CustomTabPager(
                        tabTitles = tabTitles,
                        pagerState = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        lineOffset = 2.25
                    ) { page ->
                        when (page) {
                            0 -> SpotsGrid(
                                spots = profilePictures,
                                additionalPictures = additionalPictures,
                                onLoadMore = { id, firstPicture ->
                                    profileViewModel.loadMorePicturesForSpot(id, firstPicture)
                                },
                                onPictureClick = onImageClick,
                                isLoading = isLoadingPictures,
                                emptyMessage = "Нет добавленных мест",
                                isError = error.errorLoadSpots
                            )

                            1 -> SpotsGrid(
                                spots = likedPictures,
                                additionalPictures = additionalPictures,
                                onLoadMore = { id, firstPicture ->
                                    profileViewModel.loadMorePicturesForSpot(id, firstPicture)
                                },
                                onPictureClick = onImageClick,
                                isLoading = isLoadingLiked,
                                emptyMessage = "Нет лайкнутых мест",
                                isError = error.errorLoadLikes
                            )
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
                text = error ?: "Ошибка загрузки",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("Повторить") }
        }
    }
}

@Composable
private fun SpotsGrid(
    spots: List<SpotResponse>,
    additionalPictures: Map<Long, SpotPicturesResponse>,
    onLoadMore: (Long, String) -> Unit,
    onPictureClick: (Long) -> Unit,
    isLoading: Boolean,
    emptyMessage: String,
    isError: String? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            (spots.isEmpty() && !isLoading) -> {
                Text(
                    text = emptyMessage,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            (isError != null) -> {
                Text(
                    text = isError,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 5.dp)
                ) {
                    items(spots.size) { index ->
                        spots[index].let { spot ->
                            SpotsCard(
                                firstPicture = spot.thumbnailImageUrl,
                                additionalPictures = additionalPictures[spot.id]?.pictures ?: emptyList(),
                                onLoadMore = { id, firstPicture -> onLoadMore(id, firstPicture) },
                                picturesCount = spot.picturesCount,
                                username = spot.username,
                                title = spot.title,
                                placeName = spot.namePlace,
                                description = spot.description,
                                userId = spot.userId,
                                latitude = spot.latitude,
                                longitude = spot.longitude,
                                rating = spot.rating,
                                aspectRatio = spot.aspectRatio ?: 1f,
                                userProfileImageUrl = spot.userProfileImageUrl,
                                id = spot.id,
                                isCurrentUserOwner = spot.isCurrentUserOwner,
                                onSpotClick = { onPictureClick(spot.id) },
                                screenName = "Profile"
                            )
                        }
                    }
                }
            }
        }
    }
}