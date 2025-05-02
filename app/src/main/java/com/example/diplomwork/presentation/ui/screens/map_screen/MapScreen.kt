package com.example.diplomwork.presentation.ui.screens.map_screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.diplomwork.R
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

@Suppress("DEPRECATION")
@Composable
fun MapScreen() {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val fusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val lifecycleObserver = rememberUpdatedState(
        newValue = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> MapKitFactory.getInstance().onStart()
                Lifecycle.Event.ON_STOP -> MapKitFactory.getInstance().onStop()
                else -> {}
            }
        }
    )

    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver.value)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver.value)
        }
    }

    var userLocation by remember { mutableStateOf<Point?>(null) }

    // Запрос разрешения
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) @androidx.annotation.RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]) { isGranted ->
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLocation = Point(it.latitude, it.longitude)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            val mapView = MapView(context)

            // Камера в СПБ по умолчанию
            val startLocation = Point(59.9311, 30.3609)
            val cameraPosition = CameraPosition(userLocation ?: startLocation, 12.0f, 0.0f, 0.0f)
            mapView.map.move(cameraPosition)

            // Если есть пользовательская локация — отобразим метку
            userLocation?.let { location ->
                val placemark = mapView.map.mapObjects.addPlacemark(location)
                placemark.setIcon(ImageProvider.fromResource(context, R.drawable.ic_add))
            }

            mapView
        }
    )
}