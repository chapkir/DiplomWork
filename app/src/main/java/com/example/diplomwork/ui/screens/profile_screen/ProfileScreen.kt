package com.example.diplomwork.ui.screens.profile_screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.example.diplomwork.ui.theme.ColorForBackground
import com.example.diplomwork.ui.theme.ColorForFocusButton
import com.example.diplomwork.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onImageClick: (Long, String) -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Состояние из ViewModel
    val profileData by profileViewModel.profileData.collectAsState()
    val likedPictures by profileViewModel.likedPictures.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val isUploading by profileViewModel.isUploading.collectAsState()
    val error by profileViewModel.error.collectAsState()
    val avatarUpdateCounter by profileViewModel.avatarUpdateCounter.collectAsState()

    // Управление вкладками
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Публикации", "Лайки")

    // Загружаем лайкнутые пины, когда выбран второй таб
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 1) {
            profileViewModel.loadLikedPictures()
        }
    }

    // Лаунчер для выбора изображения
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            profileViewModel.uploadAvatarToServer(it, context)
        }
    }

    // Основной UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorForBackground)
    ) {
        when {
            isLoading -> {
                LoadingSpinnerForScreen()
            }

            error != null -> {
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
                        Button(onClick = {
                            profileViewModel.loadLikedPictures() // Повторная попытка загрузки
                        }) {
                            Text("Повторить")
                        }
                    }
                }
            }

            profileData != null -> {
                ProfileHeader(
                    username = profileData?.username ?: "Неизвестный",
                    avatarUrl = profileData?.profileImageUrl,
                    isUploading = isUploading,
                    onAvatarClick = { pickImageLauncher.launch("image/*") },
                    onLogout = onLogout,
                    avatarUpdateKey = avatarUpdateCounter
                )

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    contentColor = Color.White,
                    containerColor = ColorForBackground
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index }) {
                            Text(text = title, color = Color.White)
                        }
                    }
                }

                // Используем Crossfade для плавного переключения между контентом
                Crossfade(targetState = selectedTabIndex) { currentTabIndex ->
                    when (currentTabIndex) {
                        0 -> {
                            if (profileData?.pictures?.isEmpty() == true) {
                                EmptyStateMessage(message = "У вас пока нет пинов")
                            } else {
                                PicturesGrid(
                                    pictures = profileData?.pictures ?: emptyList(),
                                    onPictureClick = onImageClick
                                )
                            }
                        }

                        1 -> {
                            if (likedPictures.isEmpty()) {
                                EmptyStateMessage(message = "У вас пока нет лайкнутых пинов")
                            } else {
                                PicturesGrid(pictures = likedPictures, onPictureClick = onImageClick)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    username: String,
    avatarUrl: String?,
    isUploading: Boolean = false,
    onAvatarClick: () -> Unit,
    onLogout: () -> Unit,
    avatarUpdateKey: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.size(40.dp))
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
                            .data(avatarUrl + "?v=$avatarUpdateKey")
                            .crossfade(true)
                            .placeholder(R.drawable.default_avatar)
                            .error(R.drawable.default_avatar)
                            .diskCachePolicy(CachePolicy.DISABLED)
                            .memoryCachePolicy(CachePolicy.DISABLED)
                            .build(),
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .border(
                                3.dp,
                                color = ColorForFocusButton,
                                shape = CircleShape
                            )
                    )
                }
            }
            Box(modifier = Modifier.size(40.dp))
            {
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
        Spacer(Modifier.size(10.dp))
        Text(
            text = username,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.size(10.dp))
    }
}


@Composable
private fun EmptyStateMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = Color.White, textAlign = TextAlign.Center)
    }
}

@Composable
private fun PicturesGrid(pictures: List<PictureResponse>, onPictureClick: (Long, String) -> Unit) {

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(4.dp),
        content = {
            itemsIndexed(pictures) { _, picture ->
                PictureCard(
                    imageUrl = picture.imageUrl,
                    id = picture.id,
                    onClick = {
                        onPictureClick(picture.id, picture.imageUrl)
                    }
                )
            }
        }
    )
}

