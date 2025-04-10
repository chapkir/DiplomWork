package com.example.diplomwork.ui.screens.profile_screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.diplomwork.R
import com.example.diplomwork.model.PictureResponse
import com.example.diplomwork.ui.components.LoadingSpinnerForScreen
import com.example.diplomwork.ui.components.PictureCard
import com.example.diplomwork.ui.theme.ColorForBackgroundProfile
import com.example.diplomwork.ui.theme.ColorForFocusButton
import com.example.diplomwork.viewmodel.ProfileViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onImageClick: (Long, String) -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val profileData by profileViewModel.profileData.collectAsState()
    val profileImageUrl by profileViewModel.profileImageUrl.collectAsState()
    val likedPictures by profileViewModel.likedPictures.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val isUploading by profileViewModel.isUploading.collectAsState()
    val error by profileViewModel.error.collectAsState()
    val avatarUpdateCounter by profileViewModel.avatarUpdateCounter.collectAsState()
    val isOwnProfile by profileViewModel.isOwnProfile.collectAsState()

    // Управление вкладками
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Публикации", "Лайки")

    // Загружаем лайкнутые пины, когда выбран второй таб
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 1) profileViewModel.loadLikedPictures()
    }

    // Лаунчер для выбора аватарки
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { profileViewModel.uploadAvatarToServer(it, context) } }

    // Основной UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorForBackgroundProfile)
    ) {
        when {
            isLoading -> LoadingSpinnerForScreen()
            error != null -> ErrorScreen(error) { profileViewModel.loadLikedPictures() }
            profileData != null -> {
                // Здесь выбираем соответствующий ProfileHeader
                if (isOwnProfile) {
                    OwnProfileHeader(
                        username = profileData?.username ?: "Неизвестный",
                        avatarUrl = profileImageUrl,
                        isUploading = isUploading,
                        onAvatarClick = {
                            pickImageLauncher.launch("image/*")
                        },
                        onLogout = onLogout,
                        avatarUpdateKey = avatarUpdateCounter
                    )
                } else {
                    OtherProfileHeader(
                        username = profileData?.username ?: "Неизвестный",
                        avatarUrl = profileImageUrl,
                        avatarUpdateKey = avatarUpdateCounter
                    )
                }

                SwipeableTabs(
                    tabTitles = tabTitles,
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = { selectedTabIndex = it },
                    profileData = profileData?.pins ?: emptyList(),
                    likedPictures = likedPictures,
                    onImageClick = onImageClick
                )
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
fun Avatar(
    avatarUrl: String?,
    isUploading: Boolean,
    onAvatarClick: () -> Unit,
    avatarUpdateKey: Int
) {
    Box(
        modifier = Modifier
            .size(140.dp)
            .clip(RoundedCornerShape(50))
            .clickable { onAvatarClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isUploading) {
            LoadingSpinnerForScreen()
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("$avatarUrl?v=$avatarUpdateKey")
                    .crossfade(true)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .border(3.dp, color = ColorForFocusButton, shape = CircleShape)
            )

            if (avatarUrl.isNullOrEmpty()) {
                Text(
                    text = "Добавить аватар",
                    color = Color.Gray ,
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun LogoutButton(onLogout: () -> Unit) {
    Box(modifier = Modifier.size(40.dp)) {
        IconButton(onClick = onLogout) {
            Icon(
                painter = painterResource(id = R.drawable.ic_login),
                contentDescription = "Exit",
                tint = Color.White,
                modifier = Modifier.size(25.dp)
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun SwipeableTabs(
    tabTitles: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    profileData: List<PictureResponse>,
    likedPictures: List<PictureResponse>,
    onImageClick: (Long, String) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = selectedTabIndex)

    LaunchedEffect(pagerState.currentPage) {
        onTabSelected(pagerState.currentPage)
    }

    TabRow(
        selectedTabIndex = pagerState.currentPage,
        modifier = Modifier.padding(horizontal = 30.dp),
        contentColor = Color.White,
        containerColor = ColorForBackgroundProfile
    ) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                selected = pagerState.currentPage == index,
                onClick = { onTabSelected(index) }
            ) {
                Text(
                    text = title,
                    modifier = Modifier.padding(bottom = 7.dp),
                    color = if (pagerState.currentPage == index) Color.White else Color.Gray,
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                )
            }
        }
    }

    HorizontalPager(
        count = tabTitles.size,
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = true
    ) { page ->
        when (page) {
            0 -> PicturesGrid(profileData, onImageClick)
            1 -> PicturesGrid(likedPictures, onImageClick)
        }
    }
}

@Composable
private fun PicturesGrid(pictures: List<PictureResponse>, onPictureClick: (Long, String) -> Unit) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(2.dp)
    ) {
        itemsIndexed(items = pictures, key = { _, picture -> picture.id }) { _, picture ->
            PictureCard(
                imageUrl = picture.imageUrl,
                id = picture.id,
                onClick = { onPictureClick(picture.id, picture.imageUrl) },
                contentPadding = 3
            )
        }
    }
}