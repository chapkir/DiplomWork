package com.example.diplomwork.ui.screens.add_photo_screen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.diplomwork.R
import com.example.diplomwork.ui.theme.ColorForAddPhotoDialog
import com.example.diplomwork.ui.theme.ColorForBottomMenu

@Composable
fun AddPhotoDialog(
    onDismiss: () -> Unit,
    onAddPhoto: (Uri?) -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = ColorForAddPhotoDialog
            ),
            shape = RoundedCornerShape(13.dp)
        ) {
            Box(
                modifier = Modifier.background(ColorForAddPhotoDialog)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Что вы хотите добавить",
                        modifier = Modifier
                            .padding(top = 20.dp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onAddPhoto(null); onDismiss() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ColorForBottomMenu
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_home),
                                    contentDescription = "Add picture",
                                    modifier = Modifier
                                        .padding(7.dp)
                                        .size(22.dp)
                                )
                                Text("Фото")
                            }
                        }

                        Button(
                            onClick = { onDismiss() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ColorForBottomMenu
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_picture),
                                    contentDescription = "Add post",
                                    modifier = Modifier
                                        .padding(7.dp)
                                        .size(22.dp)
                                )
                                Text("Пост")
                            }
                        }
                    }
                }
            }
        }
    }
}





