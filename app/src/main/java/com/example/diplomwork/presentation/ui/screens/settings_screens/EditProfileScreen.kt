package com.example.diplomwork.presentation.ui.screens.settings_screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.diplomwork.R
import com.example.diplomwork.presentation.ui.components.CustomVisualTransformationForPassword
import com.example.diplomwork.presentation.ui.components.LoadingSpinnerForElement
import com.example.diplomwork.presentation.viewmodel.EditProfileViewModel

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onEditSuccess: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val profileState by viewModel.editProfileData.collectAsState()
    val isProfileSaved by viewModel.isProfileSaved.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    val focusManager = LocalFocusManager.current

    LaunchedEffect(isProfileSaved) {
        if (isProfileSaved) {
            viewModel.resetSavedFlag()
            onEditSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(start = 15.dp)
                    .size(32.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_left),
                    contentDescription = "OnBack",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(15.dp))
            Text(
                text = "Редактировать профиль",
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        HorizontalDivider()
        Spacer(modifier = Modifier.height(25.dp))

        CustomOutlinedTextField(
            value = profileState.firstName,
            onValueChange = { viewModel.updateProfileData { copy(firstName = it) } },
            label = "Имя",
            keyboardType = KeyboardType.Text
        )

        Spacer(modifier = Modifier.height(12.dp))

        CustomOutlinedTextField(
            value = profileState.email,
            onValueChange = { viewModel.updateProfileData { copy(email = it) } },
            label = "Email",
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(12.dp))

        CustomOutlinedTextField(
            value = profileState.city,
            onValueChange = { viewModel.updateProfileData { copy(city = it) } },
            label = "Город",
            keyboardType = KeyboardType.Text
        )

        Spacer(modifier = Modifier.height(12.dp))

        CustomOutlinedTextField(
            value = profileState.gender,
            onValueChange = { viewModel.updateProfileData { copy(gender = it) } },
            label = "Пол",
            keyboardType = KeyboardType.Text
        )

        Spacer(modifier = Modifier.height(12.dp))

        CustomOutlinedTextField(
            value = profileState.bio,
            onValueChange = { viewModel.updateProfileData { copy(bio = it) } },
            label = "О себе",
            keyboardType = KeyboardType.Text
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                viewModel.saveProfile()
            },
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            if (isLoading) {
                LoadingSpinnerForElement()
            } else {
                Text("Сохранить", color = Color.Black)
            }
        }

        successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun CustomOutlinedTextField(
    value: String?,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    focusRequester: FocusRequester = remember { FocusRequester() },
    isPassword: Boolean = false
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = value ?: "",
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        visualTransformation =
            if (isPassword && !passwordVisible) CustomVisualTransformationForPassword()
            else VisualTransformation.None,
        trailingIcon = {
            if (isPassword) {
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
            }
        },
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .focusRequester(focusRequester),
        textStyle = TextStyle(
            fontSize = if (isPassword && !passwordVisible) 21.sp else 18.sp,
            color = Color.White
        ),
        maxLines = 1,
        shape = RoundedCornerShape(15.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.Gray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.Gray,
            cursorColor = Color.White,
            focusedTrailingIconColor = Color.Gray,
            unfocusedTrailingIconColor = Color.Gray,
        )
    )
}