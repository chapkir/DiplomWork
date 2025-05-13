package com.example.diplomwork.presentation.ui.screens.map_screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.diplomwork.R
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider

@SuppressLint("MissingPermission")
@Composable
fun MapScreen() {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

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

    var userLocation by remember { mutableStateOf<Point?>(null) }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLocation = Point(it.latitude, it.longitude)
                    Log.d("UserLocation", "Пользователь: ${it.latitude}, ${it.longitude}")
                }
            }
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var searchResultPoint by remember { mutableStateOf<Point?>(null) }
    var searchResultName by remember { mutableStateOf<String?>(null) }
    var searchResultAddress by remember { mutableStateOf<String?>(null) }

    val searchManager = remember {
        SearchFactory.getInstance().createSearchManager(SearchManagerType.ONLINE)
    }

    // Функция поиска
    fun searchPlace(query: String) {
        val center = Point(59.938784, 30.314997)
        val options = SearchOptions()

        searchManager.submit(
            query,
            Geometry.fromPoint(center),
            options,
            object : Session.SearchListener {
                override fun onSearchResponse(response: Response) {
                    val result = response.collection.children.firstOrNull()?.obj
                    val point = result?.geometry?.firstOrNull()?.point
                    val name = result?.name
                    val address = result?.descriptionText

                    if (point != null) {
                        searchResultPoint = point
                        searchResultName = name
                        searchResultAddress = address
                        Log.d("SearchResult", "Найдено: $name, $address (${point.latitude}, ${point.longitude})")
                    }
                }

                override fun onSearchError(error: Error) {
                    Log.e("SearchError", "Ошибка поиска:")
                }
            }
        )
    }

    var mapView by remember { mutableStateOf<MapView?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Введите адрес или название места") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                searchPlace(searchQuery)
            })
        )

        Button(
            onClick = {
                searchPlace(searchQuery)
            },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text("Поиск")
        }

        Button(
            onClick = {
                searchResultPoint?.let {
                    Log.i("Добавленное место", """
                        Название: ${searchResultName ?: "неизвестно"}
                        Адрес: ${searchResultAddress ?: "неизвестно"}
                        Координаты: ${it.latitude}, ${it.longitude}
                    """.trimIndent())
                }
            },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text("Добавить место")
        }

        // Вывод информации о месте
        searchResultPoint?.let {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Название: ${searchResultName ?: "—"}")
                Text("Адрес: ${searchResultAddress ?: "—"}")
                Text("Координаты: ${it.latitude}, ${it.longitude}")
            }
        }

        userLocation?.let { location ->
            mapView?.map?.mapObjects?.addPlacemark(location)
                ?.setIcon(ImageProvider.fromResource(context, R.drawable.ic_user))
        }

        AndroidView(
            factory = {
                MapView(context).also { mapView = it }
            },
            update = { view ->
                view.map.mapObjects.clear()

                val start = Point(59.938784, 30.314997)
                val position = searchResultPoint ?: start
                view.map.move(CameraPosition(position, 9f, 0f, 0f))

                searchResultPoint?.let {
                    val placemark = view.map.mapObjects.addPlacemark(it)
                    placemark.setIcon(ImageProvider.fromResource(context, R.drawable.ic_add))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}
