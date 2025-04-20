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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.diplomwork.ui.theme.ColorForHint
import com.example.diplomwork.viewmodel.EditProfileViewModel
import com.example.diplomwork.viewmodel.RegisterViewModel

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
fun RegisterScreen(
    onCompleteRegistration: () -> Unit,
    registerViewModel: RegisterViewModel = hiltViewModel(),
    editProfileViewModel: EditProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val registerData by registerViewModel.registerData.collectAsState()
    val editProfileData by editProfileViewModel.editProfileData.collectAsState()
    val step by registerViewModel.step.collectAsState()
    val isLoading by registerViewModel.isLoading.collectAsState()
    val errorMessage by registerViewModel.errorMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorForBackground),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        StepIndicator(step)

        Spacer(modifier = Modifier.height(32.dp))

        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(step) {
            focusRequester.requestFocus()
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (step) {
                0 -> {
                    Text(
                        text = "Как вас зовут?",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(25.dp))

                    CustomOutlinedTextField(
                        registerData.username,
                        {
                            registerViewModel.updateRegisterData {
                                copy(
                                    username = it.replace(" ", "")
                                        .filter { c -> c.code in 32..126 })
                            }
                        },
                        "Логин",
                        KeyboardType.Text,
                        focusRequester,
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    CustomOutlinedTextField(
                        registerData.firstName,
                        {
                            registerViewModel.updateRegisterData {
                                copy(
                                    firstName = it.replace(" ", "")
                                        .filter { c -> c.code in 32..126 })
                            }
                        },
                        "Имя",
                        KeyboardType.Text,
                    )
                }

                1 -> {
                    Text(
                        text = "Придумайте пароль",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(25.dp))

                    CustomOutlinedTextField(
                        registerData.password,
                        {
                            registerViewModel.updateRegisterData {
                                copy(password = it.replace(" ", ""))
                            }
                        },
                        "Пароль",
                        KeyboardType.Password,
                        focusRequester,
                        isPassword = true
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    CustomOutlinedTextField(
                        registerData.password,
                        {
                            registerViewModel.updateRegisterData {
                                copy(password = it.replace(" ", ""))
                            }
                        },
                        "Пароль",
                        KeyboardType.Password,
                        focusRequester,
                        isPassword = true
                    )
                }

                2 -> {
                    Text(
                        text = "Введите email",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(25.dp))

                    CustomOutlinedTextField(
                        registerData.email,
                        {
                            registerViewModel.updateRegisterData {
                                copy(email = it.replace(" ", ""))
                            }
                        },
                        "Email",
                        KeyboardType.Email,
                        focusRequester,
                    )
                }

                3 -> {
                    Text(
                        text = "Расскажите о себе",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(25.dp))

                    CustomOutlinedTextField(
                        editProfileData.bio,
                        {
                            editProfileViewModel.updateProfileData {
                                copy(bio = it)
                            }
                        },
                        "bio",
                        KeyboardType.Text,
                        focusRequester,
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(19.dp))

        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = if (step > 0) Arrangement.SpaceBetween else Arrangement.End
        ) {
            if (step > 0) {
                Button(
                    onClick = { registerViewModel.previousStep() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorForFocusButton,
                        contentColor = Color.White,
                    )
                ) {
                    Text("Назад")
                }
            }

            val isNextEnabled = registerViewModel.isCurrentStepValid()

            Button(
                onClick = {
                    when (step) {
                        0 -> registerViewModel.nextStep()
                        1 -> registerViewModel.nextStep()
                        2 -> {
                            registerViewModel.register {
                                focusManager.clearFocus()
                                Toast.makeText(context, "Регистрация успешна!", Toast.LENGTH_SHORT)
                                    .show()
                                registerViewModel.nextStep()
                            }
                        }
                        3 -> {
                            hideKeyboard(context)
                            editProfileViewModel.saveProfile()
                            onCompleteRegistration()
                        }
                    }
                },
                enabled = isNextEnabled && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorForFocusButton,
                    contentColor = Color.White,
                    disabledContainerColor = Color.Gray,
                    disabledContentColor = Color.White
                )
            ) {
                if (isLoading && step == 3) {
                    LoadingSpinnerForElement()
                } else {
                    Text(text = if (step < 3) "Далее" else "Завершить")
                }
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = it,
                color = Color.Red,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = "Подсказка:",
            color = ColorForHint.copy(alpha = 0.9f),
            modifier = Modifier.padding(top = 25.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "• Вводите логин на латинице •" +
                    "\n• Вводите данные без пробелов •" +
                    "\n• В поле email обязательна @ •" +
                    "\n• Длина пароля минимум 8 символов •",
            color = ColorForHint.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 1.dp),
            textAlign = TextAlign.Center
        )
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

@Composable
fun StepIndicator(currentStep: Int) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        for (i in 0..3) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (i == currentStep) Color.White else Color.Gray)
                    .padding(4.dp)
            )
            if (i < 3) Spacer(modifier = Modifier.width(8.dp))
        }
    }
}
