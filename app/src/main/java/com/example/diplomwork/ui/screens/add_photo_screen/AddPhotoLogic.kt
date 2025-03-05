package com.example.diplomwork.ui.screens.add_photo_screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun OpenGalleryAndSaveImage(isDialogOpen: MutableState<Boolean>) {

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) } // Переменная для хранения URI изображения

    // Лаунчер для выбора фото из галереи
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Сохраняем URI выбранного изображения
        selectedImageUri = uri
    }

    // Функция для открытия галереи
    val openGallery = {
        galleryLauncher.launch("image/*") // Открытие галереи для выбора изображения
    }

    val closeDialog = { isDialogOpen.value = false }

    if (isDialogOpen.value) {
        AddPhotoDialog(
            onDismiss = closeDialog,
            onAddPhoto = {
                openGallery()
                closeDialog()
            })
    }
}