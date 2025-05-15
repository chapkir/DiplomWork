package com.example.diplomwork.presentation.ui.screens.login_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.diplomwork.R
import com.example.diplomwork.presentation.ui.components.CustomVisualTransformationForPassword
import com.example.diplomwork.presentation.ui.components.LoadingSpinnerForElement
import com.example.diplomwork.presentation.ui.theme.BgDefault
import com.example.diplomwork.presentation.ui.theme.ButtonPrimary
import com.example.diplomwork.presentation.ui.theme.TextPrimary
import com.example.diplomwork.presentation.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    loginViewModel: LoginViewModel = hiltViewModel()
) {

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDefault)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(115.dp))
            Text(
                text = "Spotsy",
                color = ButtonPrimary,
                fontSize = 60.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(50.dp))

            LoginTextField(
                value = username,
                onValueChange = { loginViewModel.onUsernameChange(it.replace(" ", "")) },
                label = "Имя пользователя",
                icon = Icons.Default.Person,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            LoginTextField(
                value = password,
                onValueChange = { loginViewModel.onPasswordChange(it.replace(" ", "")) },
                label = "Пароль",
                icon = Icons.Default.Lock,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(0.8f),
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePassword = { passwordVisible = !passwordVisible }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { loginViewModel.login() },
                modifier = Modifier.fillMaxWidth(0.75f),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonPrimary.copy(alpha = 0.96f),
                    contentColor = TextPrimary,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.White
                )
            ) {
                if (isLoading) {
                    LoadingSpinnerForElement()
                } else {
                    Text("Войти", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            TextButton(onClick = onNavigateToRegister) {
                Text("Перейти к регистрации", color = Color.LightGray, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(15.dp))

            loginError?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }


        PrivacyPolicyText(
            modifier = Modifier
                .align(Alignment.BottomCenter)
        )
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
                            .size(23.dp)
                            .padding(end = 3.dp),
                        contentDescription = "Toggle password visibility"
                    )
                }
            }
        } else null,
        enabled = enabled,
        singleLine = true,
        maxLines = 1,
        shape = RoundedCornerShape(15.dp),
        modifier = modifier,
        textStyle = TextStyle(
            fontSize = if (isPassword && !passwordVisible) 20.sp else 17.sp,
            color = TextPrimary
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.Gray,
            focusedLeadingIconColor = Color.White,
            unfocusedLeadingIconColor = Color.Gray,
            focusedTextColor = TextPrimary,
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

@Suppress("DEPRECATION")
@Composable
fun PrivacyPolicyText(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    val annotatedText = buildAnnotatedString {
        append("Продолжая, вы соглашаетесь с положениями таких документов Spotsy, как ")

        pushStringAnnotation(
            tag = "URL_PRIVACY",
            annotation = "http://chapkir.ru/privacy.html"
        )
        withStyle(
            style = SpanStyle(
                color = ButtonPrimary.copy(alpha = 0.9f),
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Medium
            )
        ) {
            append("Политика конфиденциальности")
        }
        pop()

        append(" и ")

        pushStringAnnotation(
            tag = "URL_PRIVACY",
            annotation = "http://chapkir.ru/privacy.html"
        )
        withStyle(
            style = SpanStyle(
                color = ButtonPrimary.copy(alpha = 0.9f),
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Medium
            )
        ) {
            append("Пользовательское соглашение")
        }
        pop()

        append(", а также подтверждаете, что прочли их.")
    }

    ClickableText(
        text = annotatedText,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 13.dp),
        style = TextStyle(
            fontSize = 12.sp,
            color = Color.LightGray,
            textAlign = TextAlign.Center
        ),
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = "URL_PRIVACY", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    uriHandler.openUri(annotation.item)
                }
        }
    )
}