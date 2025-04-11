package com.example.diplomwork.ui.screens.profile_screen.profile_components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.diplomwork.R
import com.example.diplomwork.ui.components.LoadingSpinnerForScreen

@Composable
fun Avatar(
    avatarUrl: String?,
    isUploading: Boolean,
    onAvatarClick: () -> Unit,
    avatarUpdateKey: Int
) {
    Box(
        modifier = Modifier
            .size(68.dp)
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
            )

            if (avatarUrl.isNullOrEmpty()) {
                Text(
                    text = "Добавить аватар",
                    color = Color.Gray ,
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}