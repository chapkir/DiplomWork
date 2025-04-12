package com.example.diplomwork.ui.screens.profile_screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.diplomwork.model.PictureResponse
import com.example.diplomwork.ui.components.LoadingSpinnerForScreen
import com.example.diplomwork.ui.components.PictureCard
import com.example.diplomwork.ui.theme.ColorForBackgroundProfile
import com.example.diplomwork.viewmodel.ProfileViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
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

    // Управление вкладками
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Публикации", "Избранное")

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
                ProfileHeader(
                    username = profileData?.username ?: "Неизвестный",
                    picturesCount = profileData?.pinsCount ?: 0,
                    avatarUrl = profileData?.profileImageUrl,
                    isUploading = isUploading,
                    onAvatarClick = {
                        pickImageLauncher.launch("image/*")
                    },
                    onLogout = onLogout,
                    onBack = onBack,
                    avatarUpdateKey = avatarUpdateCounter,
                    isOwnProfile = isOwnProfile
                )

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

    CustomTabPager(
        tabTitles = tabTitles,
        pagerState = pagerState,
        modifier = Modifier.fillMaxSize()
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

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CustomTabPager(
    tabTitles: List<String>,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    tabContent: @Composable (page: Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val tabWidths = remember { mutableStateListOf<Float>() }

    Column(modifier = modifier) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { coordinates ->
                                if (tabWidths.size < tabTitles.size) {
                                    tabWidths.add(coordinates.size.width.toFloat())
                                }
                            }
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(
                                        page = index,
                                        animationSpec = tween(
                                            durationMillis = 400,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                                }
                            }
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (pagerState.currentPage == index) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
            }

            // Индикатор под вкладками
            if (tabWidths.size == tabTitles.size) {
                val tabWidth = tabWidths.getOrNull(0) ?: 0f
                val offset by remember {
                    derivedStateOf {
                        val pageOffset = pagerState.currentPage + pagerState.currentPageOffset
                        (tabWidth * pageOffset).coerceIn(0f, tabWidth * (tabTitles.size - 1))
                    }
                }

                Box(
                    Modifier
                        .offset { IntOffset(offset.roundToInt() + 20.dp.roundToPx(), 0) }
                        .padding(top = 36.dp)
                        .width(100.dp)
                        .height(3.dp)
                        .background(Color.White, RoundedCornerShape(1.dp)),
                )
            }
        }

        HorizontalPager(
            count = tabTitles.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            tabContent(page)
        }
    }
}