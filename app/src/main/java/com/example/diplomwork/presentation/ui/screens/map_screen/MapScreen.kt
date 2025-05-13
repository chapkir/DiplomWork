package com.example.diplomwork.presentation.ui.screens.map_screen

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.scale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.diplomwork.R
import com.example.diplomwork.presentation.viewmodel.MapViewModel
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

@Suppress("DEPRECATION")
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val searchQuery by viewModel.searchQuery.collectAsState()
    val userLocation by viewModel.userLocation.collectAsState()
    val searchResultPoint by viewModel.searchResultPoint.collectAsState()
    val searchResultName by viewModel.searchResultName.collectAsState()
    val searchResultAddress by viewModel.searchResultAddress.collectAsState()


    // Инициализация MapKit
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

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            label = { Text("Введите адрес или название места") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                viewModel.searchPlace()
            })
        )

        Button(
            onClick = { viewModel.searchPlace() },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text("Поиск")
        }

        Button(
            onClick = {
                searchResultPoint?.let {

                }
            },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text("Добавить место")
        }

        searchResultPoint?.let {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Название: ${searchResultName ?: "—"}")
                Text("Адрес: ${searchResultAddress ?: "—"}")
                Text("Координаты: ${it.latitude}, ${it.longitude}")
            }
        }

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

                val center = searchResultPoint ?: Point(59.938784, 30.314997)
                view.map.move(CameraPosition(center, 9.5f, 0f, 0f))

                searchResultPoint?.let {
                    val icon = getResizedImageProvider(context, R.drawable.ic_marker_png, 64, 64)
                    view.map.mapObjects.addPlacemark(it).setIcon(icon)
                }

                userLocation?.let {
                    val icon =
                        getResizedImageProvider(context, R.drawable.ic_navigation_png, 76, 76)
                    view.map.mapObjects.addPlacemark(it).setIcon(icon)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
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