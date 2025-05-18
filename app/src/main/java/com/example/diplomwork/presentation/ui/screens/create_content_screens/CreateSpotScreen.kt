package com.example.diplomwork.presentation.ui.screens.create_content_screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
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
import com.example.diplomwork.R
import com.example.diplomwork.presentation.ui.components.LoadingSpinnerForElement
import com.example.diplomwork.presentation.ui.navigation.CreateSpotScreenData
import com.example.diplomwork.presentation.ui.theme.ButtonPrimary
import com.example.diplomwork.presentation.ui.theme.ErrorColor
import com.example.diplomwork.presentation.viewmodel.CreateSpotViewModel

@Composable
fun CreateSpotScreen(
    createSpotScreenData: CreateSpotScreenData,
    onContentAdded: () -> Unit,
    onBack: () -> Unit,
    viewModel: CreateSpotViewModel = hiltViewModel()
) {

    val createSpotData by viewModel.createSpotData.collectAsState()
    val imageUrls = createSpotScreenData.imageUrls
    val imageUrlsUri = imageUrls.map { it.toUri() }

    var aspectRatio by remember { mutableFloatStateOf(1f) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.isError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
        ) {
            IconButton(
                onClick = { onBack() },
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp)
                    .size(35.dp)
                    .align(Alignment.CenterStart)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_left),
                    contentDescription = "Back",
                    tint = Color.White,
                )
            }
            Text(
                text = "Добавление спота",
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        HorizontalDivider()

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))

                ImagesRow(imageUrlsUri)
            }

            item {
                AddContentTextField(
                    label = "Название",
                    value = createSpotData.title,
                    onValueChange = { viewModel.updateCreateContentData { copy(title = it) } }
                )
            }

            item {
                AddContentTextField(
                    label = "Описание (необязательно)",
                    value = createSpotData.description,
                    onValueChange = { viewModel.updateCreateContentData { copy(description = it) } }
                )
            }

            item {
                AddContentTextField(
                    label = "Координаты",
                    value = createSpotData.geo,
                    onValueChange = { viewModel.updateCreateContentData { copy(geo = it) } },
                    readOnly = true
                )
            }

            item {
                AddContentTextField(
                    label = "Название места",
                    value = createSpotData.spotName,
                    onValueChange = { viewModel.updateCreateContentData { copy(spotName = it) } },
                    readOnly = true
                )
            }

            item {
                AddContentTextField(
                    label = "Рейтинг",
                    value = createSpotData.rating,
                    onValueChange = { viewModel.updateCreateContentData { copy(rating = it) } }
                )
            }

            item {
                error?.let {
                    Text(text = it, color = Color.Red)
                }
            }


            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                ) {
                    Button(
                        onClick = {
                            viewModel.uploadContent(
                                imageUris = imageUrlsUri,
                                onSuccess = onContentAdded,
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButtonPrimary,
                            contentColor = Color.White,
                            disabledContainerColor = Color.Gray,
                            disabledContentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center,
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
}

@Composable
fun ImagesRow(imageUrlsUri: List<Uri>) {
    var aspectRatio by remember { mutableFloatStateOf(1f) }

    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current

    // containerSize возвращает IntSize (width, height) в PX
    val screenWidthPx = windowInfo.containerSize.width

    val imageWidthDp = 130.dp
    val imagePaddingDp = 10.dp // по 5.dp с каждой стороны

    // Переводим размеры в пиксели
    val imageWidthPx = with(density) { (imageWidthDp + imagePaddingDp).toPx() }
    val totalImageWidthPx = imageWidthPx * imageUrlsUri.size

    val shouldCenter = totalImageWidthPx <= screenWidthPx

    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .clip(RoundedCornerShape(12.dp)),
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (shouldCenter)
                Arrangement.Center
            else
                Arrangement.spacedBy(imagePaddingDp),
        ) {
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
    }
}

@Composable
private fun AddContentTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    readOnly: Boolean = false,
) {

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = isError,
        readOnly = readOnly,
        modifier = if (label != "Описание") {
            Modifier
                .fillMaxWidth(0.9f)
        } else {
            Modifier
                .fillMaxWidth(0.9f)
                .height(100.dp)
        },
        singleLine = label != "Описание",
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        textStyle = TextStyle(
            fontSize = 15.sp,
            color = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
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

