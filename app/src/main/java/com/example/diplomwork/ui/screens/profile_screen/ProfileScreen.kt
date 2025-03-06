package com.example.diplomwork.ui.screens.profile_screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.diplomwork.R
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.model.PinResponse
import com.example.diplomwork.model.ProfileResponse
import com.example.diplomwork.network.ApiClient
import com.example.diplomwork.ui.theme.ColorForBottomMenu
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onImageClick: (Long, String) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    var profileData by remember { mutableStateOf<ProfileResponse?>(null) }
    var likedPins by remember { mutableStateOf<List<PinResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Публикации", "Лайки")

    suspend fun loadLikedPins() {
        try {
            likedPins = ApiClient.apiService.getLikedPins()
            Log.d("ProfileScreen", "Загружено ${likedPins.size} лайкнутых пинов")
        } catch (e: Exception) {
            Log.e("ProfileScreen", "Ошибка при загрузке лайкнутых пинов: ${e.message}")
            Toast.makeText(context, "Ошибка при загрузке лайкнутых пинов", Toast.LENGTH_SHORT)
                .show()
        }
    }

    LaunchedEffect(Unit) {
        if (!sessionManager.isLoggedIn()) {
            Log.d("ProfileScreen", "Пользователь не авторизован, перенаправление на экран входа")
            onNavigateToLogin()
            return@LaunchedEffect
        }

        try {
            val token = sessionManager.getAuthToken()
            if (token == null) {
                error = "Токен авторизации отсутствует"
                isLoading = false
                return@LaunchedEffect
            }

            profileData = ApiClient.apiService.getProfile()
            loadLikedPins()
            isLoading = false
        } catch (e: HttpException) {
            error = "Ошибка загрузки профиля (код ${e.code()})"
            isLoading = false
            if (e.code() == 401 || e.code() == 403) {
                sessionManager.clearSession()
                Toast.makeText(
                    context,
                    "Сессия истекла. Пожалуйста, войдите снова",
                    Toast.LENGTH_LONG
                ).show()
                onNavigateToLogin()
            }
        } catch (e: Exception) {
            error = "Ошибка: ${e.message}"
            isLoading = false
        }
    }

    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 1) {
            loadLikedPins()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorForBottomMenu)
    ) {
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
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
                            error = null
                            isLoading = true
                            scope.launch {
                                try {
                                    profileData = ApiClient.apiService.getProfile()
                                    loadLikedPins()
                                    isLoading = false
                                } catch (e: Exception) {
                                    error = "Ошибка: ${e.message}"
                                    isLoading = false
                                }
                            }
                        }) {
                            Text("Повторить")
                        }
                    }
                }
            }

            profileData != null -> {
                ProfileHeader(username = profileData!!.username, onLogout = onLogout)

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    contentColor = Color.White,
                    containerColor = ColorForBottomMenu
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index }) {
                            Text(text = title, color = Color.White)
                        }
                    }
                }

                when (selectedTabIndex) {
                    0 -> {
                        if (profileData!!.pins.isEmpty()) {
                            EmptyStateMessage(message = "У вас пока нет пинов")
                        } else {
                            PinsGrid(pins = profileData!!.pins, onPinClick = onImageClick)
                        }
                    }

                    1 -> {
                        if (likedPins.isEmpty()) {
                            EmptyStateMessage(message = "У вас пока нет лайкнутых пинов")
                        } else {
                            PinsGrid(pins = likedPins, onPinClick = onImageClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(username: String, onLogout: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Text(
            text = username,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onLogout) {
            Icon(
                painter = painterResource(id = R.drawable.ic_login),
                contentDescription = "Exit",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EmptyStateMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = Color.White, textAlign = TextAlign.Center)
    }
}

@Composable
private fun PinsGrid(pins: List<PinResponse>, onPinClick: (Long, String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.padding(8.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        items(pins) { pin ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(pin.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = pin.description,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onPinClick(pin.id, pin.imageUrl) }
            )
        }
    }
}
