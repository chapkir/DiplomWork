package com.example.diplomwork.presentation.ui.components.spot_card

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri

@Composable
fun GeoText(latitude: Double, longitude: Double, placeName: String = "") {
    val context = LocalContext.current

    Text(
        text = "$latitude, $longitude",
        fontSize = 12.sp,
        color = Color.Gray,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier.clickable {
            try {
                val encodedPlaceName = placeName.replace(" ", "+")

                val mapUri = if (encodedPlaceName.isNotBlank()) {
                    "https://yandex.ru/maps/?pt=$longitude,$latitude,pm2blm&z=16&l=map&text=$encodedPlaceName"
                } else {
                    "https://yandex.ru/maps/?pt=$longitude,$latitude,pm2blm&z=16&l=map"
                }

                val intent = Intent(Intent.ACTION_VIEW, mapUri.toUri())

                val chooser = Intent.createChooser(intent, "Выберите приложение для открытия карты")

                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(chooser)
                } else {
                    Log.d("GeoText", "No app found to handle URI")
                }
            } catch (e: Exception) {
                Log.e("GeoText", "Error creating intent: ${e.message}")
            }
        }
    )
}