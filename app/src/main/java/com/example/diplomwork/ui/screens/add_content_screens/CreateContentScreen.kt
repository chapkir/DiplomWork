package com.example.diplomwork.ui.screens.add_content_screens

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
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

    // Верхняя панель с заголовком
    Column(modifier = Modifier.fillMaxSize()) {
        // Заголовок
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Добавление $whatContentCreate",
            )
        }

        // Контент
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .weight(1f),
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

            // Поле ввода описания
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Краткое описание") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Кнопка "Опубликовать" во весь экран снизу
        Button(
            onClick = {
                when (whatContentCreate) {
                    "Picture" -> viewModel.uploadImage(imageUri, description)
                    "Post" -> viewModel.uploadPost(imageUri, description)
                }
                onContentAdded()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = {
                Text(text = "Опубликовать", modifier = Modifier.fillMaxWidth())
            }
        )
    }
}

