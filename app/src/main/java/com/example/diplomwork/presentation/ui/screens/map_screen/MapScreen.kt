package com.example.diplomwork.presentation.ui.screens.map_screen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.view.inputmethod.InputMethodManager
import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.scale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.diplomwork.R
import com.example.diplomwork.presentation.ui.theme.BgElevated
import com.example.diplomwork.presentation.ui.theme.ButtonPrimary
import com.example.diplomwork.presentation.ui.theme.ErrorColor
import com.example.diplomwork.presentation.viewmodel.MapViewModel
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

private fun hideKeyboard(context: Context) {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val activity = context as Activity
    val currentFocus = activity.currentFocus
    if (currentFocus != null) {
        inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
    }
}

@Suppress("DEPRECATION")
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    onLocationSelected: (String?, Double, Double) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val searchQuery by viewModel.searchQuery.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val searchResultPoint by viewModel.searchResultPoint.collectAsState()
    val searchResultName by viewModel.searchResultName.collectAsState()
    val searchResultAddress by viewModel.searchResultAddress.collectAsState()

    DisposableEffect(Unit) {
        MapKitFactory.initialize(context)
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> MapKitFactory.getInstance().onStart()
                Lifecycle.Event.ON_STOP -> MapKitFactory.getInstance().onStop()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    var mapView by remember { mutableStateOf<MapView?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Карта на весь экран
        AndroidView(
            factory = {
                MapView(context).apply {
                    mapView = this
                    onWindowFocusChanged(true)
                    requestFocus()
                }
            },
            update = { view ->
                view.map.mapObjects.clear()

                val cameraPosition = if (searchResultPoint != null) {
                    // 🔍 Если был поиск — приблизить и наклонить
                    CameraPosition(searchResultPoint!!, 15f, 0f, 60f)
                } else {
                    // 🏙 Изначально — вид на центр Петербурга, вид сверху
                    CameraPosition(Point(59.938784, 30.314997), 10f, 0f, 0f)
                }

                view.map.move(cameraPosition)

                // 📌 Маркер найденного места
                searchResultPoint?.let {
                    val icon = getResizedImageProvider(
                        context,
                        R.drawable.ic_marker_png,
                        76,
                        76,
                    )
                    view.map.mapObjects.addPlacemark(it).setIcon(icon)
                }

                // 📍 Маркер текущей геопозиции пользователя
                userLocation?.let {
                    val icon = getResizedImageProvider(
                        context,
                        R.drawable.ic_navigation_png,
                        76,
                        76,
                    )
                    view.map.mapObjects.addPlacemark(it).setIcon(icon)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 17.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    leadingIcon = {
                        IconButton(
                            onClick = { onBack() },
                            modifier = Modifier
                                .size(36.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_arrow_left),
                                contentDescription = "OnBack",
                                tint = Color.White
                            )
                        }
                    },
                    placeholder = {
                        Text(
                            text = "Введите адрес или название",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                hideKeyboard(context)
                                viewModel.searchPlace()
                            },
                            modifier = Modifier.padding(end = 5.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_search),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.95f),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    ),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BgElevated.copy(alpha = 0.85f),
                        unfocusedContainerColor = BgElevated.copy(alpha = 0.85f),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        errorBorderColor = ErrorColor,
                        focusedPlaceholderColor = Color.White,
                        unfocusedPlaceholderColor = Color.White,
                        errorLabelColor = ErrorColor,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            hideKeyboard(context)
                            viewModel.searchPlace()
                        }
                    )
                )
            }
            Text(
                text = "Чтобы отобразить карту коснитесь экрана",
                modifier = Modifier.padding(horizontal = 12.dp),
                color = BgElevated.copy(alpha = 0.85f),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        searchResultPoint?.let {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BgElevated.copy(alpha = 0.9f)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.95f)
                    .padding(bottom = 25.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Название: ${searchResultName ?: "—"}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Адрес: ${searchResultAddress ?: "—"}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Координаты: ${it.latitude}, ${it.longitude}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    Button(
                        onClick = {
                            onLocationSelected(
                                searchResultName,
                                it.latitude,
                                it.longitude
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonPrimary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Добавить место",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

fun getResizedImageProvider(
    context: Context,
    @DrawableRes resId: Int,
    width: Int,
    height: Int
): ImageProvider {
    val originalBitmap = BitmapFactory.decodeResource(context.resources, resId)
    val scaledBitmap = originalBitmap.scale(width, height)
    return ImageProvider.fromBitmap(scaledBitmap)
}