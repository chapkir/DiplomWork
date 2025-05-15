package com.example.diplomwork.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController

// Цвета для светлой темы
val LightColorScheme = lightColorScheme(
    // Основные цвета
    primary = ButtonPrimary,                 // Основная кнопка (общая для всех тем)
    secondary = LightButtonSecondary,        // Второстепенная кнопка
    background = LightBgDefault,             // Основной фон
    surface = LightBgElevated,               // Поверхности, карточки
    error = LightErrorColor,                 // Цвет ошибок

    // Цвета текста на фоне элементов
    onPrimary = LightTextPrimary,            // Текст на основной кнопке
    onSecondary = LightTextPrimary,          // Текст на второстепенной кнопке
    onBackground = LightTextPrimary,         // Текст на основном фоне
    onSurface = LightTextPrimary,            // Текст на поверхностях
    onError = LightTextPrimary,              // Текст на фоне ошибок

    // Контейнеры и акценты
    primaryContainer = LightBgProfile,       // Контейнер основной кнопки
    secondaryContainer = LightBgElevated,    // Контейнер второстепенной кнопки
    surfaceVariant = LightBgOverlay,         // Полупрозрачные карточки / overlay
    onPrimaryContainer = LightTextPrimary,   // Текст на контейнере основной кнопки
    onSecondaryContainer = LightTextPrimary  // Текст на контейнере второстепенной кнопки
)

// Цвета для темной темы
val DarkColorScheme = darkColorScheme(
    // Основные цвета
    primary = ButtonPrimary,  // Основная кнопка
    secondary = ButtonSecondary,  // Второстепенная кнопка
    background = BgDefault,  // Основной фон
    surface = BgElevated,  // Поднятые элементы
    error = ErrorColor,  // Цвет для ошибок

    // Для контрастных текстов
    onPrimary = TextPrimary,  // Цвет текста на основной кнопке
    onSecondary = TextPrimary,  // Цвет текста на второстепенной кнопке
    onBackground = TextPrimary,  // Цвет текста на основном фоне
    onSurface = TextPrimary,  // Цвет текста на поднятых элементах
    onError = TextPrimary,  // Цвет текста на ошибках

    // Акценты
    primaryContainer = BgProfile,  // Фон для контейнера основной кнопки
    secondaryContainer = BgElevated,  // Фон для контейнера второстепенной кнопки
    surfaceVariant = BgOverlay,  // Фон для вариаций поверхности (например, карточки)
    onPrimaryContainer = TextPrimary,  // Текст на фоне контейнера основной кнопки
    onSecondaryContainer = TextPrimary  // Текст на фоне контейнера второстепенной кнопки
)

@Composable
fun DiplomWorkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> DarkColorScheme
    }

    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = colorScheme.background,   // Цвет фона статус-бара и навигации
            darkIcons = false                 // false — иконки будут светлыми
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}