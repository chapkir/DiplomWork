package com.example.diplomwork.presentation.ui.screens.registration_screen

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.icu.util.Calendar
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.diplomwork.R
import com.example.diplomwork.presentation.ui.components.LoadingSpinnerForElement
import com.example.diplomwork.presentation.ui.theme.BgDefault
import com.example.diplomwork.presentation.ui.theme.ButtonPrimary
import com.example.diplomwork.presentation.ui.theme.ErrorColor
import com.example.diplomwork.presentation.viewmodel.EditProfileViewModel
import com.example.diplomwork.presentation.viewmodel.RegisterViewModel
import kotlinx.coroutines.launch
import java.util.Locale

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
    onBack: () -> Unit,
    registerViewModel: RegisterViewModel = hiltViewModel(),
    editProfileViewModel: EditProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val registerData by registerViewModel.registerData.collectAsState()
    val isUsernameExists by registerViewModel.isUsernameExists.collectAsState()
    val editProfileData by editProfileViewModel.editProfileData.collectAsState()
    val step by registerViewModel.step.collectAsState()
    val isLoading by registerViewModel.isLoading.collectAsState()
    val errorMessage by registerViewModel.errorMessage.collectAsState()
    val confirmPassword by registerViewModel.confirmPassword.collectAsState()
    val isEnteredPasswordsMatch by registerViewModel.isEnteredPasswordsMatch.collectAsState()

    val genders = listOf("Мужской", "Женский", "Другой")
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(step) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDefault),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        StepIndicator(
            step,
            onBackOut = onBack,
            onBackStep = { registerViewModel.previousStep() },
            onEditSkip = {
                hideKeyboard(context)
                onCompleteRegistration()
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        when (step) {
            0 -> {
                StepTitle("Как тебя зовут?")
                RegisterTextField("Введите имя пользователя", registerData.username, {
                    registerViewModel.updateRegisterData {
                        copy(
                            username = it
                                .replace(" ", "")
                                .filter { c -> c.code in 32..126 })
                    }
                }, KeyboardType.Text, focusRequester, isError = isUsernameExists)
                if (isUsernameExists) {
                    Spacer(Modifier.height(7.dp))
                    Box(modifier = Modifier.fillMaxWidth(0.85f)) {
                        Text(
                            text = "Имя пользователя уже занято. Пожалуйста, попробуйте другое.",
                            modifier = Modifier.padding(start = 5.dp, end = 20.dp),
                            color = ErrorColor,
                            fontSize = 13.sp,
                        )
                    }
                }
                Spacer(Modifier.height(15.dp))
                RegisterTextField("Введите свое имя", registerData.firstName, {
                    registerViewModel.updateRegisterData {
                        copy(firstName = it.replace(" ", ""))
                    }
                }, KeyboardType.Text)
            }

            1 -> {
                StepTitle("Придумайте пароль")
                RegisterTextField("Введите пароль", registerData.password, {
                    registerViewModel.updateRegisterData {
                        copy(password = it.replace(" ", ""))
                    }
                }, KeyboardType.Password, focusRequester, isPassword = true)
                Spacer(Modifier.height(15.dp))
                RegisterTextField("Повторите пароль", confirmPassword, {
                    registerViewModel.onConfirmPasswordChange(it.replace(" ", ""))
                }, KeyboardType.Password, isPassword = true, isError = !isEnteredPasswordsMatch)
                if (!isEnteredPasswordsMatch) {
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
            }

            2 -> {
                StepTitle("Введите email и дату рождения")
                RegisterTextField("Введите email", registerData.email, {
                    registerViewModel.updateRegisterData {
                        copy(email = it.replace(" ", ""))
                    }
                }, KeyboardType.Email, focusRequester)
                Spacer(Modifier.height(15.dp))
                DatePickerButton(
                    date = registerData.birthDate,
                    onDateSelected = {
                        registerViewModel.updateRegisterData { copy(birthDate = it) }
                    }
                )
            }

            3 -> {
                StepTitle("Расскажите о себе")
                RegisterTextField("О себе", editProfileData.bio ?: "", {
                    editProfileViewModel.updateProfileData { copy(bio = it) }
                }, KeyboardType.Text, focusRequester)

                Spacer(Modifier.height(15.dp))

                RegisterTextField("Город", editProfileData.city ?: "", {
                    editProfileViewModel.updateProfileData { copy(city = it) }
                }, KeyboardType.Text)

                Spacer(Modifier.height(15.dp))

                DropdownSelector("Пол", genders, editProfileData.gender ?: "") {
                    editProfileViewModel.updateProfileData { copy(gender = it) }
                }
            }
        }
        errorMessage?.let {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = it,
                color = Color.Red,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(25.dp))

        NavigationButton(
            step = step,
            isNextEnabled = registerViewModel.isCurrentStepValid(),
            isLoading = isLoading,
            onNext = {
                when (step) {
                    0 -> {
                        coroutineScope.launch {
                            if (!registerViewModel.checkUsernameExists())
                                registerViewModel.nextStep()
                        }
                    }

                    1 -> {
                        registerViewModel.nextStep()
                    }

                    2 -> registerViewModel.register {
                        focusManager.clearFocus()
                        registerViewModel.nextStep()
                    }

                    3 -> {
                        hideKeyboard(context)
                        editProfileViewModel.saveProfile()
                        onCompleteRegistration()
                    }
                }
            }
        )
    }

}


@Composable
fun StepTitle(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.9f),
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
fun RegisterTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    focusRequester: FocusRequester = remember { FocusRequester() },
    isPassword: Boolean = false,
    isError: Boolean = false
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        visualTransformation = if (isPassword && !passwordVisible)
            PasswordVisualTransformation()
        else VisualTransformation.None,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            if (passwordVisible) R.drawable.ic_eye_crossed else R.drawable.ic_eye
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 4.dp)
                    )
                }
            }
        } else null,
        isError = isError,
        modifier = if (label != "О себе") {
            Modifier
                .fillMaxWidth(0.85f)
                .focusRequester(focusRequester)
        } else {
            Modifier
                .fillMaxWidth(0.85f)
                .height(155.dp)
                .focusRequester(focusRequester)
        },
        singleLine = label != "О себе",
        textStyle = TextStyle(
            fontSize = if (isPassword && !passwordVisible) 21.sp else 18.sp,
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

@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText =
        selected.ifEmpty { "Выберите $label".lowercase().replaceFirstChar { it.uppercase() } }

    Box(Modifier.fillMaxWidth(0.85f)) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            textStyle = TextStyle(color = if (selected.isEmpty()) Color.Gray else Color.White),
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

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .heightIn(max = 150.dp)
                .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(10.dp))
                .border(1.dp, Color.White, RoundedCornerShape(10.dp))
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp)
                )
            }
        }
    }
}

@Composable
fun NavigationButton(
    step: Int,
    isNextEnabled: Boolean,
    isLoading: Boolean,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth(0.8f),
            enabled = isNextEnabled && !isLoading,
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ButtonPrimary,
                contentColor = Color.White,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.White
            )
        ) {
            if (isLoading && step == 3) LoadingSpinnerForElement()
            else Text(
                if (step < 3) "Далее" else "Завершить",
                fontWeight = FontWeight.Bold, fontSize = 17.sp
            )
        }
    }
}

@Composable
fun StepIndicator(
    currentStep: Int,
    onBackOut: () -> Unit,
    onBackStep: () -> Unit,
    onEditSkip: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        if (currentStep != 3) {
            IconButton(
                onClick = {
                    if (currentStep == 0) onBackOut()
                    else onBackStep()
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 15.dp)
                    .size(30.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_left),
                    contentDescription = "OnBack",
                    tint = Color.White
                )
            }
        }

        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0..3) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (i == currentStep) Color.White else Color.Gray)
                )
                if (i < 3) Spacer(modifier = Modifier.width(8.dp))
            }
        }

        if (currentStep == 3) {
            IconButton(
                onClick = {
                    onEditSkip()
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 15.dp)
                    .size(30.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = "Skip",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun DatePickerButton(
    date: String,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val locale = Locale.getDefault()

    OutlinedButton(
        onClick = {
            DatePickerDialog(
                context,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formatted = String.format(
                        locale, "%02d.%02d.%04d", selectedDay,
                        selectedMonth + 1, selectedYear
                    )
                    onDateSelected(formatted)
                }, year, month, day
            ).show()
        },
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(57.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
    ) {
        Text(
            text = if (date.isEmpty()) "Выберите дату рождения" else "Вы родились $date",
            color = if (date.isEmpty()) Color.Gray else Color.White,
            fontWeight = if (date.isEmpty()) FontWeight.Normal else FontWeight.Medium,
            fontSize = 17.sp
        )
    }
}

