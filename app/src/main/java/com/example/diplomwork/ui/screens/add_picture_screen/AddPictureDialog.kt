package com.example.diplomwork.ui.screens.add_picture_screen

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
import com.example.diplomwork.ui.theme.ColorForBackground
import com.example.diplomwork.viewmodel.AddPictureDialogViewModel

@Composable
fun AddPictureDialog(
    onDismiss: () -> Unit,
    onAddPhoto: () -> Unit,
    onRefresh: () -> Unit,
    viewModel: AddPictureDialogViewModel
) {
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val showPreview by viewModel.showPreview.collectAsState()

    var description by remember { mutableStateOf("") }

    if (showPreview && selectedImageUri != null) {
        PicturePreviewDialog(
            imageUri = selectedImageUri!!,
            onDismiss = {
                viewModel.onDismissPreview()
                onDismiss()
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
                                onClick = { onAddPhoto() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ColorForBackground
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
                                    containerColor = ColorForBackground
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

