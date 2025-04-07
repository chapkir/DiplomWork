package com.example.diplomwork.ui.screens.add_content_screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.diplomwork.viewmodel.AddContentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContentScreen(
    imageUri: Uri?,
    onContentAdded: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddContentViewModel = hiltViewModel()
) {
    var description by remember { mutableStateOf("") }


    Log.e("piska", "v kartinke seichas - $imageUri")

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
                        viewModel.uploadPost(imageUri, description)
                        onContentAdded()
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
    }
}

