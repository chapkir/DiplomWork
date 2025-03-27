package com.example.diplomwork.ui.screens.profile_screen

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.example.diplomwork.R
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.model.PictureResponse
import com.example.diplomwork.model.ProfileResponse
import com.example.diplomwork.network.ApiClient
import com.example.diplomwork.ui.components.LoadingSpinnerForScreen
import com.example.diplomwork.ui.theme.ColorForBottomMenu
import com.example.diplomwork.ui.util.ImageUtils
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import coil.request.CachePolicy
import com.example.diplomwork.ui.theme.ColorForFocusButton
import com.example.diplomwork.ui.theme.ColorForHint

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
    var likedPins by remember { mutableStateOf<List<PictureResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isUploading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var avatarUpdateCounter by remember { mutableStateOf(0) }
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

    suspend fun loadProfile() {
        try {
            profileData = ApiClient.apiService.getProfile()
            profileImageUrl = profileData?.profileImageUrl
            Log.d("ProfileScreen", "Загружен профиль: ${profileData?.username}, аватар: $profileImageUrl")
        } catch (e: Exception) {
            Log.e("ProfileScreen", "Ошибка при загрузке профиля: ${e.message}")
            throw e
        }
    }

    // Функция для загрузки аватарки на сервер
    fun uploadAvatarToServer(uri: Uri, context: Context) {
        scope.launch {
            isUploading = true
            try {
                val imageFile = ImageUtils.copyUriToFile(context, uri)

                if (imageFile != null) {
                    try {
                        // Создаем MultipartBody.Part для файла изображения
                        val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

                        // Отправляем запрос на сервер
                        val response = ApiClient.apiService.uploadProfileImage(body)

                        // Обрабатываем успешный ответ
                        if (response.isSuccessful) {
                            // Обновляем URL аватарки
                            val updatedProfile = response.body()
                            val newImageUrl = updatedProfile?.get("profileImageUrl")

                            // Сначала обновляем локальную переменную profileImageUrl
                            profileImageUrl = newImageUrl

                            // Увеличиваем счетчик обновлений для перерисовки UI
                            avatarUpdateCounter++

                            // Затем обновляем данные профиля
                            try {
                                profileData = ApiClient.apiService.getProfile()
                                // Теперь profileData содержит актуальные данные, включая обновленный URL аватара

                                Toast.makeText(context, "Аватар успешно обновлен", Toast.LENGTH_SHORT).show()
                                Log.d("ProfileScreen", "Аватар успешно загружен: $profileImageUrl")
                            } catch (e: Exception) {
                                Log.e("ProfileScreen", "Ошибка при обновлении профиля после загрузки аватара: ${e.message}")
                                // Даже если не удалось обновить полный профиль,
                                // аватар все равно уже обновлен в интерфейсе
                            }
                        } else {
                            // Обрабатываем ошибку
                            val errorMessage = response.errorBody()?.string() ?: "Неизвестная ошибка"
                            Toast.makeText(context, "Ошибка при загрузке аватара: $errorMessage", Toast.LENGTH_SHORT).show()
                            Log.e("ProfileScreen", "Ошибка при загрузке аватара: $errorMessage")
                        }
                    } finally {
                        // Удаляем временный файл
                        try {
                            imageFile.delete()
                        } catch (e: Exception) {
                            Log.e("ProfileScreen", "Ошибка при удалении временного файла: ${e.message}")
                        }
                    }
                } else {
                    Toast.makeText(context, "Не удалось подготовить изображение", Toast.LENGTH_SHORT).show()
                    Log.e("ProfileScreen", "Не удалось подготовить изображение")
                }
            } catch (e: Exception) {
                Log.e("ProfileScreen", "Ошибка при загрузке аватара: ${e.message}", e)
                Toast.makeText(context, "Ошибка при загрузке аватара: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isUploading = false
            }
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

            loadProfile()
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

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uploadAvatarToServer(it, context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorForBottomMenu)
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
                            error = null
                            isLoading = true
                            scope.launch {
                                try {
                                    loadProfile()
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
                ProfileHeader(
                    username = profileData?.username ?: "Неизвестный",
                    avatarUrl = profileImageUrl,
                    isUploading = isUploading,
                    onAvatarClick = { pickImageLauncher.launch("image/*") },
                    onLogout = onLogout,
                    avatarUpdateKey = avatarUpdateCounter
                )

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
                        if (profileData?.pins?.isEmpty() == true) {
                            EmptyStateMessage(message = "У вас пока нет пинов")
                        } else {
                            PinsGrid(
                                pins = profileData?.pins ?: emptyList(),
                                onPinClick = onImageClick
                            )
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
private fun PinsGrid(pins: List<PictureResponse>, onPinClick: (Long, String) -> Unit) {

    var aspectRatio by remember { mutableStateOf(1f) }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(4.dp)
    ) {
        itemsIndexed(pins) { _, pin ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(pin.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = pin.description,
                contentScale = ContentScale.Crop,
                onState = { state ->
                    //isLoading = state is AsyncImagePainter.State.Loading
                    //isError = state is AsyncImagePainter.State.Error
                    if (state is AsyncImagePainter.State.Success) {
                        val size = state.painter.intrinsicSize
                        if (size.width > 0 && size.height > 0) {
                            aspectRatio = size.width / size.height
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .aspectRatio(aspectRatio)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onPinClick(pin.id, pin.imageUrl) }
            )
        }
    }
}