package com.example.diplomwork.ui.screens.registration_screen

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomwork.R
import com.example.diplomwork.auth.SessionManager
import com.example.diplomwork.model.LoginRequest
import com.example.diplomwork.model.RegisterRequest
import com.example.diplomwork.network.ApiClient
import com.example.diplomwork.ui.components.LoadingSpinnerForElement
import com.example.diplomwork.ui.theme.ColorForBottomMenu
import kotlinx.coroutines.launch

fun hideKeyboard(context: Context) {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val activity = context as Activity
    val currentFocus = activity.currentFocus
    if (currentFocus != null) {
        inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
    }
}

@Composable
fun RegisterScreen(onCompleteRegistration: () -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorForBottomMenu),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        StepIndicator(step)

        Spacer(modifier = Modifier.height(32.dp))

        when (step) {
            0 -> StepField(
                "Введите имя пользователя",
                "Логин",
                username,
                {
                    username = it.replace(" ", "")
                        .filter { c -> c.code in 32..126 }
                }
            )

            1 -> StepField(
                "Введите email",
                "Email",
                email,
                { email = it.replace(" ", "") },
                KeyboardType.Email
            )

            2 -> StepField(
                "Придумайте пароль",
                "Пароль",
                password,
                { password = it.replace(" ", "") },
                KeyboardType.Password,
                isPassword = true
            )
        }

        Spacer(modifier = Modifier.height(19.dp))

        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = if (step > 0) Arrangement.SpaceBetween else Arrangement.End
        ) {
            if (step > 0) {
                Button(
                    onClick = { step-- },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White,
                    )
                ) {
                    Text("Назад")
                }
            }

            val isNextEnabled = when (step) {
                0 -> username.isNotBlank() && !username.contains(" ")
                1 -> email.isNotBlank() && email.contains("@") && !email.contains(" ")
                2 -> password.isNotBlank() && password.length >= 8 && !password.contains(" ")
                else -> false
            }

            Button(
                onClick = {
                    if (step < 2) {
                        step++
                    } else {
                        scope.launch {
                            try {
                                isLoading = true

                                // Регистрация
                                val registerResponse = ApiClient.apiService.register(
                                    RegisterRequest(username, email, password)
                                )

                                // Автоматическая авторизация
                                val loginResponse = ApiClient.apiService.login(
                                    LoginRequest(username, password)
                                )

                                // Сохранение токена
                                sessionManager.saveAuthToken(loginResponse.token)

                                // Очистка фокуса и скрытие клавиатуры
                                focusManager.clearFocus()
                                hideKeyboard(context)

                                Toast.makeText(context, "Регистрация успешна!", Toast.LENGTH_SHORT)
                                    .show()
                                onCompleteRegistration()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG)
                                    .show()
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                enabled = isNextEnabled && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red.copy(alpha = 0.9f),
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.White
                )
            ) {
                if (isLoading && step == 2) {
                    LoadingSpinnerForElement()
                } else {
                    Text(
                        text = if (step < 2) "Далее" else "Завершить"
                    )
                }
            }
        }

        Text(
            text = "Подсказка:",
            color = Color.Gray,
            modifier = Modifier.padding(top = 25.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "• Вводите логин на латинице •" +
                    "\n• Вводите данные без пробелов •" +
                    "\n• В поле email обязательна @ •" +
                    "\n• Длина пароля минимум 8 символов •",
            color = Color.Gray.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 1.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StepField(
    title: String,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(25.dp))

        CustomOutlinedTextField(
            value, onValueChange, label, keyboardType, focusRequester, isPassword
        )
    }
}

@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    focusRequester: FocusRequester,
    isPassword: Boolean = false
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        visualTransformation =
        if (isPassword && !passwordVisible) PasswordVisualTransformation()
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

@Composable
fun StepIndicator(currentStep: Int) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        for (i in 0..2) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (i == currentStep) Color.White else Color.Gray)
                    .padding(4.dp)
            )
            if (i < 2) Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

