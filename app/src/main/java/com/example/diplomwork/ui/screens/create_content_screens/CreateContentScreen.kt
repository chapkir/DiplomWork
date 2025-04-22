package com.example.diplomwork.ui.screens.create_content_screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.diplomwork.ui.components.LoadingSpinnerForElement
import com.example.diplomwork.ui.navigation.CreateContentScreenData
import com.example.diplomwork.ui.theme.ColorForFocusButton
import com.example.diplomwork.viewmodel.AddContentViewModel

@Composable
fun CreateContentScreen(
    createContentScreenData: CreateContentScreenData,
    onContentAdded: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddContentViewModel = hiltViewModel()
) {

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    var description by remember { mutableStateOf("") }
    val imageUri = createContentScreenData.imageUrl.toUri()
    val whatContentCreate = createContentScreenData.whatContentCreate
    var aspectRatio by remember { mutableFloatStateOf(1f) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.isError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Заголовок
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text =
                    (when (whatContentCreate) {
                        "Picture" -> "Добавление картинки"
                        else -> "Добавление поста"
                    }),
                textAlign = TextAlign.Center,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp

            )
        }

        // Контент
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (whatContentCreate) {
                "Picture" -> {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(5.dp),
                        modifier = Modifier
                            .width(130.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUri)
                                .crossfade(300)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            onState = { state ->
                                if (state is AsyncImagePainter.State.Success) {
                                    val size = state.painter.intrinsicSize
                                    if (size.width > 0 && size.height > 0) {
                                        aspectRatio = size.width / size.height
                                    }
                                }
                            },
                            modifier = Modifier
                                .aspectRatio(aspectRatio)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }

                "Post" -> {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Выбранное изображение",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Краткое описание") },
                shape = RoundedCornerShape(15.dp),
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.Gray,
                    focusedLeadingIconColor = Color.White,
                    unfocusedLeadingIconColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.Gray,
                    cursorColor = Color.White,
                    focusedTrailingIconColor = Color.Gray,
                    unfocusedTrailingIconColor = Color.Gray,
                    disabledTextColor = Color.Gray,
                    disabledBorderColor = Color.Gray,
                    disabledLabelColor = Color.Gray,
                    disabledSupportingTextColor = Color.Gray,
                    disabledLeadingIconColor = Color.Gray,
                    disabledTrailingIconColor = Color.Gray
                )
            )

            error?.let {
                Text(text = it, color = Color.Red)
            }


            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        viewModel.uploadContent(
                            type = whatContentCreate,
                            imageUri = imageUri,
                            description = description,
                            onSuccess = onContentAdded
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorForFocusButton,
                        contentColor = Color.White,
                        disabledContainerColor = Color.Gray,
                        disabledContentColor = Color.White
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingSpinnerForElement()
                        }
                    } else {
                        Text(
                            "Опубликовать",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

