package com.example.diplomwork.ui.screens.login_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.diplomwork.R
import com.example.diplomwork.ui.components.CustomVisualTransformationForPassword
import com.example.diplomwork.ui.components.LoadingSpinnerForElement
import com.example.diplomwork.ui.theme.ColorForBackground
import com.example.diplomwork.ui.theme.ColorForFocusButton
import com.example.diplomwork.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val username by loginViewModel.username.collectAsState()
    val password by loginViewModel.password.collectAsState()
    val loginSuccess by loginViewModel.loginSuccess.collectAsState()
    val isLoading by loginViewModel.isLoading.collectAsState()
    val loginError by loginViewModel.loginError.collectAsState()
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(loginSuccess) {
        if (loginSuccess == true) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorForBackground)
            .imePadding(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.size(15.dp))
        Text(
            text = "Введите данные для входа",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(25.dp))

        LoginTextField(
            value = username,
            onValueChange = loginViewModel::onUsernameChange,
            label = "Логин",
            icon = Icons.Default.Person,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .focusRequester(focusRequester)
        )

        Spacer(modifier = Modifier.height(10.dp))

        LoginTextField(
            value = password,
            onValueChange = loginViewModel::onPasswordChange,
            label = "Пароль",
            icon = Icons.Default.Lock,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(0.85f),
            isPassword = true,
            passwordVisible = passwordVisible,
            onTogglePassword = { passwordVisible = !passwordVisible }
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = { loginViewModel.login() },
            modifier = Modifier.fillMaxWidth(0.85f),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorForFocusButton,
                contentColor = Color.White,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.White
            ),
            enabled = !isLoading && username.isNotBlank() && password.isNotBlank()
        ) {
            if (isLoading) {
                LoadingSpinnerForElement()
            } else {
                Text("Войти", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        }

        loginError?.let {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = it,
                color = Color.Red,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("Нет аккаунта? Зарегистрируйтесь!", color = Color.White)
        }
    }
}

@Composable
fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        visualTransformation =
            if (isPassword && !passwordVisible) CustomVisualTransformationForPassword()
            else VisualTransformation.None,
        trailingIcon = if (isPassword && onTogglePassword != null) {
            {
                val image = if (passwordVisible) R.drawable.ic_eye_crossed else R.drawable.ic_eye
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        painter = painterResource(image),
                        modifier = Modifier
                            .size(26.dp)
                            .padding(end = 4.dp),
                        contentDescription = "Toggle password visibility"
                    )
                }
            }
        } else null,
        enabled = enabled,
        maxLines = 1,
        shape = RoundedCornerShape(15.dp),
        modifier = modifier,
        textStyle = TextStyle(
            fontSize = if (isPassword && !passwordVisible) 21.sp else 18.sp,
            color = Color.White
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.Gray,
            focusedLeadingIconColor = Color.White,
            unfocusedLeadingIconColor = Color.Gray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.Gray,
            cursorColor = Color.White,
            focusedTrailingIconColor = Color.Gray,
            unfocusedTrailingIconColor = Color.Gray,
            disabledTextColor = Color.Gray,
            disabledBorderColor = Color.Gray,
            disabledLabelColor = Color.Gray,
            disabledSupportingTextColor = Color.Gray,
            disabledLeadingIconColor = Color.Gray,
            disabledTrailingIconColor = Color.Gray
        )
    )
}
