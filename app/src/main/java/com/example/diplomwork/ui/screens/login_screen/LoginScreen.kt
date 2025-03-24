package com.example.diplomwork.ui.screens.login_screen

import android.widget.Toast
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomwork.R
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.model.LoginRequest
import com.example.diplomwork.network.ApiClient
import com.example.diplomwork.ui.components.LoadingSpinnerForScreen
import com.example.diplomwork.ui.theme.ColorForBottomMenu
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val focusRequester = FocusRequester()
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorForBottomMenu)
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


        OutlinedTextField(
            value = username,
            onValueChange =
            {
                username = it
                    .replace(" ", "")
                    .filter { c -> c.code in 32..126 }
            },
            label = { Text("Логин") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .focusRequester(focusRequester),
            enabled = !isLoading,
            maxLines = 1,
            shape = RoundedCornerShape(15.dp),
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
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange =
            {
                password = it
                    .replace(" ", "")
                    .filter { c -> c.code in 32..126 }
            },
            label = { Text("Пароль") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation =
            if (!passwordVisible) PasswordVisualTransformation()
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
            modifier = Modifier.fillMaxWidth(0.85f),
            enabled = !isLoading,
            maxLines = 1,
            shape = RoundedCornerShape(15.dp),
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
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        isLoading = true
                        val response = ApiClient.apiService.login(LoginRequest(username, password))
                        sessionManager.saveAuthToken(response.token)
                        onLoginSuccess()
                    } catch (e: Exception) {
                        Toast.makeText(
                            context, "Ошибка авторизации: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(0.85f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red.copy(alpha = 0.9f),
                contentColor = Color.White,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.White

            ),
            enabled = !isLoading && username.isNotBlank() && password.isNotBlank()
        ) {
            if (isLoading) {
                LoadingSpinnerForScreen()
            } else {
                Text(
                    text = "Войти",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { onNavigateToRegister() }) {
            Text("Нет аккаунта? Зарегистрируйтесь!", color = Color.White)
        }
    }
}
