package com.example.diplomwork.presentation.ui.screens.settings_screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.diplomwork.R
import com.example.diplomwork.presentation.ui.components.CustomVisualTransformationForPassword
import com.example.diplomwork.presentation.ui.components.LoadingSpinnerForElement
import com.example.diplomwork.presentation.ui.theme.ErrorColor
import com.example.diplomwork.presentation.viewmodel.ChangePasswordViewModel

@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
    isPasswordChanged: () -> Unit,
    changePasswordViewModel: ChangePasswordViewModel = hiltViewModel()
) {
    val changePasswordData by changePasswordViewModel.changePasswordData.collectAsState()
    val isPasswordsMatch by changePasswordViewModel.isPasswordsMatch.collectAsState()
    val isLoading by changePasswordViewModel.isLoading.collectAsState()
    val errorMessage by changePasswordViewModel.errorMessage.collectAsState()
    val isButtonEnabled = changePasswordViewModel.isCurrentStepValid()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Ошибка
        errorMessage?.let {
            if (it.isNotEmpty()) {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Старый пароль
        ChangePasswordTextField(
            value = changePasswordData.oldPassword,
            onValueChange = {
                changePasswordViewModel.updateChangePasswordData { copy(oldPassword = it) }
            },
            label = "Старый пароль",
        )

        // Новый пароль
        ChangePasswordTextField(
            value = changePasswordData.newPassword,
            onValueChange = {
                changePasswordViewModel.updateChangePasswordData { copy(newPassword = it) }
            },
            label = "Новый пароль",
        )

        // Подтверждение нового пароля
        ChangePasswordTextField(
            value = changePasswordData.confirmPassword,
            onValueChange = {
                changePasswordViewModel.updateChangePasswordData { copy(confirmPassword = it) }
            },
            label = "Подтвердите новый пароль",
            isError = !isPasswordsMatch
        )

        if (!isPasswordsMatch) {
            Spacer(Modifier.height(7.dp))
            Box(modifier = Modifier.fillMaxWidth(0.85f)) {
                Text(
                    text = "Пароли не совпадают.",
                    modifier = Modifier.padding(start = 5.dp, end = 20.dp),
                    color = ErrorColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                changePasswordViewModel.changePassword(isPasswordChanged)
            },
            enabled = isButtonEnabled && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                LoadingSpinnerForElement()
            } else {
                Text("Изменить пароль")
            }
        }
    }
}

@Composable
fun ChangePasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
        visualTransformation =
            if (!passwordVisible) CustomVisualTransformationForPassword()
            else VisualTransformation.None,
        trailingIcon = {
            val image = if (passwordVisible) R.drawable.ic_eye_crossed else R.drawable.ic_eye
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    painter = painterResource(image),
                    modifier = Modifier
                        .size(26.dp)
                        .padding(end = 4.dp),
                    contentDescription = "Toggle password visibility"
                )
            }
        },
        isError = isError,
        modifier = Modifier
            .fillMaxWidth(0.85f),
        textStyle = TextStyle(
            fontSize = if (!passwordVisible) 21.sp else 18.sp,
            color = Color.White
        ),
        maxLines = 1,
        shape = RoundedCornerShape(15.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = Color.White,
            errorBorderColor = ErrorColor,
            errorLabelColor = ErrorColor,
            unfocusedLabelColor = Color.Gray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.Gray,
            cursorColor = Color.White,
            focusedTrailingIconColor = Color.Gray,
            unfocusedTrailingIconColor = Color.Gray,
        )
    )
}