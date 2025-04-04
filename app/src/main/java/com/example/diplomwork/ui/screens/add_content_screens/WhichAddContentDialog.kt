package com.example.diplomwork.ui.screens.add_content_screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.diplomwork.R
import com.example.diplomwork.ui.screens.gallery_screen.GalleryDialog
import com.example.diplomwork.ui.theme.ColorForAddPhotoDialog
import com.example.diplomwork.ui.theme.ColorForBackground
import com.example.diplomwork.viewmodel.AddContentViewModel


@Composable
fun WhichAddContentDialog(
    navController: NavController,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit,
    viewModel: AddContentViewModel = hiltViewModel()
) {
    var isGalleryOpen by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            colors = CardDefaults.cardColors(containerColor = ColorForAddPhotoDialog),
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
                            onClick = { isGalleryOpen = true }, // Открываем галерею
                            colors = ButtonDefaults.buttonColors(containerColor = ColorForBackground),
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
                            onClick = {
                                onDismiss()
                                navController.navigate("addContent/post")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorForBackground),
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

    // Открытие галереи
    if (isGalleryOpen) {
        GalleryDialog(
            onImageSelected = { uri ->
                isGalleryOpen = false
                onDismiss()
                navController.navigate("addContent/picture/${Uri.encode(uri.toString())}")
            },
            onDismiss = { isGalleryOpen = false }
        )
    }
}

