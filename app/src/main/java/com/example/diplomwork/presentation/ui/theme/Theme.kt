package com.example.diplomwork.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
// Цвета для светлой темы
val LightColorScheme = lightColorScheme(
    // Основные цвета
    primary = ButtonPrimary,  // Основная кнопка
    secondary = ButtonSecondary,  // Второстепенная кнопка
    background = BgDefault,  // Основной фон
    surface = BgElevated,  // Поднятые элементы
    error = Error,  // Цвет для ошибок

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

// Цвета для темной темы
val DarkColorScheme = darkColorScheme(
    // Основные цвета
    primary = ButtonPrimary,  // Основная кнопка
    secondary = ButtonSecondary,  // Второстепенная кнопка
    background = BgDefault,  // Основной фон
    surface = BgElevated,  // Поднятые элементы
    error = Error,  // Цвет для ошибок

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
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}