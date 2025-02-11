package com.example.diplomwork.system_settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat

@Composable
fun SystemInsetHeight(type: Int): MutableState<Dp> {

    val insetHeight = remember { mutableStateOf(0.dp) }

    val view = LocalView.current
    val density = LocalDensity.current

    LaunchedEffect(view) {
        val insets = ViewCompat.getRootWindowInsets(view)
        insetHeight.value = with(density) {
            insets?.getInsets(type)?.bottom?.toDp() ?: 0.dp
        }
    }
    return insetHeight
}

/*

WindowInsetsCompat.Type.statusBars() – высота статус-бара.
WindowInsetsCompat.Type.navigationBars() – высота нижней навигационной панели.
WindowInsetsCompat.Type.systemBars() – сумма высот статус-бара и навигационной панели.
WindowInsetsCompat.Type.captionBar() – высота верхней панели заголовка (если есть, например, у всплывающих окон).
WindowInsetsCompat.Type.ime() – высота клавиатуры (IME – Input Method Editor).
WindowInsetsCompat.Type.mandatorySystemGestures() – отступы для обязательных жестов системы.
WindowInsetsCompat.Type.systemGestures() – отступы для любых системных жестов.
WindowInsetsCompat.Type.tappableElement() – область, безопасная для нажатий (например, вокруг кнопок навигации).

val statusBarHeight = SystemInsetHeight(WindowInsetsCompat.Type.statusBars())    Высота статус-бара
val navBarHeight = SystemInsetHeight(WindowInsetsCompat.Type.navigationBars())   Высота навигационной панели
val imeHeight = SystemInsetHeight(WindowInsetsCompat.Type.ime())                 Высота клавиатуры

*/