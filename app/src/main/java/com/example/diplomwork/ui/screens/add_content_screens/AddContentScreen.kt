package com.example.diplomwork.ui.screens.add_content_screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContentScreen(
    contentType: ContentType, // Принимаем тип контента
    onContentAdded: (ContentType) -> Unit, // Коллбэк при добавлении
    onBack: () -> Unit // Возврат назад
) {
    var text by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить контент") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        when (contentType) {
                            is ContentType.Post -> onContentAdded(ContentType.Post(text))
                            is ContentType.Picture -> onContentAdded(
                                ContentType.Picture(
                                    contentType.imageUri,
                                    title,
                                    description
                                )
                            )
                        }
                    }) {
                        Text("Опубликовать")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (contentType) {
                is ContentType.Post -> {
                    // Кнопка добавления картинки
                    Button(onClick = { /* Открыть галерею */ }) {
                        Text("Добавить изображение")
                    }

                    // Поле ввода текста поста
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("Введите текст поста") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is ContentType.Picture -> {
                    // Отображение выбранной картинки
                    Image(
                        painter = rememberAsyncImagePainter(contentType.imageUri),
                        contentDescription = "Выбранное изображение",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    // Поле ввода названия
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Название") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Поле ввода описания
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Краткое описание") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}