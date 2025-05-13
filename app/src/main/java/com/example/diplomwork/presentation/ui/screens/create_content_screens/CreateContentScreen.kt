package com.example.diplomwork.presentation.ui.screens.create_content_screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.example.diplomwork.presentation.ui.components.LoadingSpinnerForElement
import com.example.diplomwork.presentation.ui.navigation.CreateContentScreenData
import com.example.diplomwork.presentation.ui.theme.ButtonPrimary
import com.example.diplomwork.presentation.ui.theme.ErrorColor
import com.example.diplomwork.presentation.viewmodel.CreateContentViewModel

@Composable
fun CreateContentScreen(
    createContentScreenData: CreateContentScreenData,
    onContentAdded: () -> Unit,
    onBack: () -> Unit,
    viewModel: CreateContentViewModel = hiltViewModel()
) {

    val focusManager = LocalFocusManager.current
    val createContentData by viewModel.createContentData.collectAsState()
    val imageUrls = createContentScreenData.imageUrls
    val imageUrlsUri = imageUrls.map { it.toUri() }

    var aspectRatio by remember { mutableFloatStateOf(1f) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.isError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .imePadding()
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
                text = "Добавление спота",
                textAlign = TextAlign.Center,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        }

        // Контент
        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyRow {
                items(imageUrlsUri) { imageUri ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(5.dp),
                        modifier = Modifier
                            .width(130.dp)
                            .padding(horizontal = 5.dp)
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
            }

            AddContentTextField(
                label = "Название",
                value = createContentData.title,
                onValueChange = { viewModel.updateCreateContentData { copy(title = it) } }
            )

            AddContentTextField(
                label = "Описание",
                value = createContentData.description,
                onValueChange = { viewModel.updateCreateContentData { copy(description = it) } }
            )

            AddContentTextField(
                label = "Геоданные",
                value = createContentData.geo,
                onValueChange = { viewModel.updateCreateContentData { copy(geo = it) } }
            )

            AddContentTextField(
                label = "Рейтинг",
                value = createContentData.rating,
                onValueChange = { viewModel.updateCreateContentData { copy(rating = it) } }
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
                            imageUris = imageUrlsUri,
                            onSuccess = onContentAdded
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButtonPrimary,
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
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun AddContentTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false
) {

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = isError,
        modifier = if (label != "Описание") {
            Modifier
                .fillMaxWidth(0.95f)
        } else {
            Modifier
                .fillMaxWidth(0.95f)
                .height(100.dp)
        },
        singleLine = label != "Описание",
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        textStyle = TextStyle(
            fontSize = 15.sp,
            color = Color.White
        ),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.Gray,
            errorBorderColor = ErrorColor,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.Gray,
            errorLabelColor = ErrorColor,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.Gray,
            cursorColor = Color.White,
            focusedTrailingIconColor = Color.White,
            unfocusedTrailingIconColor = Color.Gray,
            errorTrailingIconColor = Color.White
        )
    )
}

