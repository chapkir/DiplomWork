package com.example.diplomwork.ui.screens.home_screen.top_bar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import com.example.diplomwork.R
import com.example.diplomwork.system_settings.SystemInsetHeight
import com.example.diplomwork.ui.theme.ColorForBottomMenu
import com.example.diplomwork.ui.theme.Dimens.TopBarHeight

@Composable
fun getTopBarForScreen(currentRoute: String?): @Composable () -> Unit {
    return when (currentRoute) {
        "home_screen" -> {
            {
                CustomTopBar(
                    title = "Лента картинок"
//                    actions =
//                    {
//                        IconButton(onClick = { /* Действие */ })
//                        {
//                            Icon(
//                                painter = painterResource(id = R.drawable.ic_search),
//                                contentDescription = "search",
//                                modifier = Modifier.size(15.dp),
//                                tint = Color.White
//                            )
                    //                    }
                    //              }
                )
            }
        }

        "info_screen" -> {
            { CustomTopBar(title = "хз че тут будет") }
        }

        "add_screen" -> {
            { CustomTopBar(title = "Добавить") }
        }

        "favs_screen" -> {
            {
                CustomTopBar(
                    title = "Избранное"
                    )
            }
        }

        "login_screen" -> {
            {
                CustomTopBar(
                    title = "Профиль"
                    )
            }
        }

        else -> {
            {}
        }
    }
}

@Composable
fun CustomTopBar(title: String, actions: @Composable RowScope.() -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                TopBarHeight + SystemInsetHeight(WindowInsetsCompat.Type.statusBars()).value
            )
            .background(ColorForBottomMenu),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(1f))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(13.dp)
        )
        Spacer(Modifier.weight(1f))
        actions()
    }
}