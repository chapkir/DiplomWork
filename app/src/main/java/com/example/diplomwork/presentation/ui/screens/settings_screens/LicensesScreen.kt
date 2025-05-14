package com.example.diplomwork.presentation.ui.screens.settings_screens

import android.util.Log
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplomwork.R
import com.example.diplomwork.presentation.ui.theme.BgDefault
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.libraryColors
import com.mikepenz.aboutlibraries.util.withContext

@Composable
fun LicensesScreen(
    onBack: () -> Unit
) {

    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(start = 22.dp)
                    .size(32.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_left),
                    contentDescription = "OnBack",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(22.dp))
            Text(
                text = "Сведения о приложении",
                color = Color.White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        HorizontalDivider()

        LibrariesContainer(
            modifier = Modifier.fillMaxSize(),
            librariesBlock = { ctx ->
                Libs.Builder().withContext(ctx).build()
            },
            header = {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                    ) {
                        SettingItem(
                            title = "Условия использования отдельных сервисов Яндекс Карт",
                            onClick = { uriHandler.openUri("https://yandex.ru/legal/maps_api/") },
                            actionIcon = R.drawable.ic_arrow_up_right
                        )
                        HorizontalDivider()
                        Text(
                            text = "Лицензии",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray,
                            modifier = Modifier.padding(start = 15.dp, top = 20.dp)
                        )
                    }
                }
            },
            divider = { HorizontalDivider() },
            footer = {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Привет всем кто долистал <3")
                    }
                }
            },
            colors = LibraryDefaults.libraryColors(
                backgroundColor = BgDefault
            ),
            showAuthor = true,
            showVersion = true,
            showLicenseBadges = true,
            onLibraryClick = { library ->
                Log.d("LicensesScreen", "Clicked on library: ${library.name}")
            }
        )
    }
}
