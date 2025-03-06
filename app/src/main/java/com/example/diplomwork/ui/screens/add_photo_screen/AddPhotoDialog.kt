package com.example.diplomwork.ui.screens.add_photo_screen

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.diplomwork.R
import com.example.diplomwork.ui.theme.ColorForAddPhotoDialog
import com.example.diplomwork.ui.theme.ColorForBottomMenu
import android.widget.Toast
import com.example.diplomwork.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import android.provider.MediaStore
import android.provider.OpenableColumns
import java.io.FileOutputStream
import java.io.InputStream

@Composable
fun AddPhotoDialog(
    onDismiss: () -> Unit,
    onAddPhoto: () -> Unit,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showPreview by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        if (uri != null) {
            showPreview = true
        }
    }

    if (showPreview && selectedImageUri != null) {
        ImagePreviewDialog(
            imageUri = selectedImageUri!!,
            onDismiss = {
                showPreview = false
                onDismiss()
            },
            onPublish = { description ->
                scope.launch {
                    try {
                        isLoading = true
                        val file = createTempFileFromUri(context, selectedImageUri!!)
                        if (file != null) {
                            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                            val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())

                            withContext(Dispatchers.IO) {
                                try {
                                    val response = ApiClient.imageUploadService.uploadImage(body, descriptionBody)
                                    withContext(Dispatchers.Main) {
                                        if (response.isSuccessful) {
                                            val pin = response.body()
                                            if (pin != null) {
                                                Toast.makeText(context, "Изображение успешно загружено", Toast.LENGTH_SHORT).show()
                                                showPreview = false
                                                onRefresh()
                                                onDismiss()
                                            } else {
                                                Toast.makeText(context, "Ошибка при загрузке изображения", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "Ошибка: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("AddPhotoDialog", "Ошибка при загрузке", e)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Ошибка при загрузке: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                } finally {
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(context, "Ошибка при подготовке файла", Toast.LENGTH_LONG).show()
                            isLoading = false
                        }
                    } catch (e: Exception) {
                        Log.e("AddPhotoDialog", "Ошибка", e)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Ошибка: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            isLoading = false
                        }
                    }
                }
            },
            onPublishSuccess = {
                onRefresh()
            }
        )
    } else {
        Dialog(onDismissRequest = { onDismiss() }) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = ColorForAddPhotoDialog
                ),
                shape = RoundedCornerShape(13.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Box(
                    modifier = Modifier
                        .background(ColorForAddPhotoDialog)
                        .padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Text(
                            text = "Что вы хотите добавить",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    filePicker.launch("image/*")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ColorForBottomMenu
                                ),
                                modifier = Modifier.width(120.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_picture),
                                        contentDescription = "Add picture",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Фото")
                                }
                            }

                            Button(
                                onClick = { onDismiss() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ColorForBottomMenu
                                ),
                                modifier = Modifier.width(120.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_home),
                                        contentDescription = "Add post",
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Пост")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun createTempFileFromUri(context: Context, uri: Uri): File? {
    return try {
        val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        } ?: "temp_image_${System.currentTimeMillis()}.jpg"

        val tempFile = File(context.cacheDir, fileName)
        tempFile.createNewFile()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val bufferedInputStream = inputStream.buffered(8192)
            FileOutputStream(tempFile).buffered(8192).use { outputStream ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesRead = 0L
                val fileSize = getFileSize(context, uri)

                while (bufferedInputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    // Логируем прогресс
                    if (fileSize > 0) {
                        val progress = (totalBytesRead.toFloat() / fileSize * 100).toInt()
                        Log.d("AddPhotoDialog", "Прогресс загрузки: $progress%")
                    }
                }
                outputStream.flush()
            }
        }

        // Проверяем размер файла
        if (tempFile.length() > 10 * 1024 * 1024) { // 10MB
            throw IllegalArgumentException("Файл слишком большой. Максимальный размер: 10MB")
        }

        tempFile
    } catch (e: Exception) {
        Log.e("AddPhotoDialog", "Ошибка при создании временного файла", e)
        null
    }
}

private fun getFileSize(context: Context, uri: Uri): Long {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        cursor.moveToFirst()
        cursor.getLong(sizeIndex)
    } ?: -1
}

private fun getRealPathFromURI(context: Context, uri: Uri): String? {
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    val cursor = context.contentResolver.query(uri, projection, null, null, null)
    return cursor?.use {
        val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        it.moveToFirst()
        it.getString(columnIndex)
    }
}
