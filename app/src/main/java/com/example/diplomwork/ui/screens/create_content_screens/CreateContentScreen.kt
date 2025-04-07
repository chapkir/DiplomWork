package com.example.diplomwork.ui.screens.create_content_screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.diplomwork.ui.components.LoadingSpinnerForScreen
import com.example.diplomwork.ui.navigation.CreateContentScreenData
import com.example.diplomwork.viewmodel.AddContentViewModel

@Composable
fun CreateContentScreen(
    createContentScreenData: CreateContentScreenData,
    onContentAdded: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddContentViewModel = hiltViewModel()
) {
    var description by remember { mutableStateOf("") }
    val imageUri = createContentScreenData.imageUrl.toUri()
    val whatContentCreate = createContentScreenData.whatContentCreate

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.isError.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Заголовок
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Добавление $whatContentCreate")
        }

        // Контент
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Выбранное изображение",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Краткое описание") },
                modifier = Modifier.fillMaxWidth()
            )

            error?.let {
                Text(text = it, color = Color.Red)
            }
        }

        // Кнопка
        Button(
            onClick = {
                viewModel.uploadContent(
                    type = whatContentCreate,
                    imageUri = imageUri,
                    description = description,
                    onSuccess = onContentAdded
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
             LoadingSpinnerForScreen()
            } else {
                Text("Опубликовать", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }
    }
}

