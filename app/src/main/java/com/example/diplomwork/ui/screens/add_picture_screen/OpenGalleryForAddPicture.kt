package com.example.diplomwork.ui.screens.add_picture_screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.diplomwork.viewmodel.AddPictureDialogViewModel

@Composable
fun OpenGalleryForAddPicture(
    isDialogOpen: MutableState<Boolean>,
    onRefresh: () -> Unit,
    viewModel: AddPictureDialogViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Лаунчер для выбора фото из галереи
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    // Запрос разрешения на доступ к файлам
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        } else {
            Toast.makeText(
                context,
                "Необходимо разрешение для доступа к галерее",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Для устройств с Android 10 (API 29) и выше
    val mediaPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        } else {
            Toast.makeText(
                context,
                "Необходимо разрешение для доступа к изображениям",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val openGallery = {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // Для Android 10 и выше (используем новый тип разрешения)
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    galleryLauncher.launch("image/*")
                } else {
                    mediaPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                // Для Android 6.0 - Android 9.0
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    galleryLauncher.launch("image/*")
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }

            else -> {
                // Для более старых версий Android
                galleryLauncher.launch("image/*")
            }
        }
    }

    if (isDialogOpen.value) {
        AddPictureDialog(
            onDismiss = { isDialogOpen.value = false },
            onAddPhoto = openGallery,
            onRefresh = onRefresh,
            viewModel = viewModel
        )
    }
}