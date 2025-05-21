package com.example.diplomwork.presentation.ui.screens.settings_screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomwork.presentation.ui.theme.ErrorColor
import com.example.diplomwork.presentation.viewmodel.SettingsViewModel

@Composable
fun FeedbackScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val feedbackData by viewModel.feedbackData.collectAsState()
    val rating by remember { mutableIntStateOf(0) }

    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {

        Box(modifier = Modifier.fillMaxWidth())
        {
            SettingsHeader(onBack = onBack, title = "Обратная связь")
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 11.dp, end = 11.dp)
                .imePadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            item {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = ParagraphStyle(
                                lineHeight = 20.sp,
                                textAlign = TextAlign.Center
                            )
                        ) {
                            append("\nКаждый день мы стараемся становиться лучше для вас 💪\n\n")
                            append("Нам важно знать, что вы думаете о нашем приложении — поделитесь своим мнением! 💬\n")
                        }
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            item {
                FeedbackTextField(
                    value = feedbackData.whatLiked ?: "",
                    onValueChange = { viewModel.updateWhatLiked(it) },
                    label = "Что понравилось",
                    placeholder = "Что Вам понравилось в нашем приложении?"
                )
            }

            item {
                Spacer(Modifier.height(15.dp))

                FeedbackTextField(
                    value = feedbackData.whatDisliked ?: "",
                    onValueChange = { viewModel.updateWhatDisliked(it) },
                    label = "Что не понравилось",
                    placeholder = "Что Вам не понравилось в нашем приложении?"
                )
            }

            item {
                Spacer(Modifier.height(15.dp))

                FeedbackTextField(
                    value = feedbackData.recommendations ?: "",
                    onValueChange = { viewModel.updateRecommendations(it) },
                    label = "Пожелания / предложения",
                    placeholder = "Что бы Вы хотели видеть в нашем приложении?"
                )
            }

            item {
                Spacer(Modifier.height(22.dp))

                Button(
                    onClick = {
                        viewModel.sendFeedback()
                        Toast.makeText(context, "Спасибо за Ваш отзыв!", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.7f),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Text(
                        text = "Отправить",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun FeedbackTextField(
    value: String,
    label: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontSize = 14.sp,
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = Color.Gray
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        singleLine = false,
        textStyle = TextStyle(
            fontSize = 16.sp,
            color = Color.White
        ),
        shape = RoundedCornerShape(15.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.Gray,
            errorBorderColor = ErrorColor,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.Gray,
            errorLabelColor = ErrorColor,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.Gray,
            cursorColor = Color.White,
            focusedTrailingIconColor = Color.White,
            unfocusedTrailingIconColor = Color.Gray,
            errorTrailingIconColor = Color.White
        )
    )
}