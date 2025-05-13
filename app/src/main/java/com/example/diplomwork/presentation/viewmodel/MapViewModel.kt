package com.example.diplomwork.presentation.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.runtime.Error
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private val _userLocation = MutableStateFlow<Point?>(null)
    val userLocation: StateFlow<Point?> = _userLocation.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResultPoint = MutableStateFlow<Point?>(null)
    val searchResultPoint: StateFlow<Point?> = _searchResultPoint.asStateFlow()

    private val _searchResultName = MutableStateFlow<String?>(null)
    val searchResultName: StateFlow<String?> = _searchResultName.asStateFlow()

    private val _searchResultAddress = MutableStateFlow<String?>(null)
    val searchResultAddress: StateFlow<String?> = _searchResultAddress.asStateFlow()

    private val searchManager = SearchFactory.getInstance()
        .createSearchManager(SearchManagerType.ONLINE)

    init {
        fetchUserLocation()
    }

    private fun fetchUserLocation() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    _userLocation.value = Point(it.latitude, it.longitude)
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchPlace() {
        val center = Point(59.938784, 30.314997)
        val options = SearchOptions()

        searchManager.submit(
            _searchQuery.value,
            Geometry.fromPoint(center),
            options,
            object : Session.SearchListener {
                override fun onSearchResponse(response: Response) {
                    val result = response.collection.children.firstOrNull()?.obj
                    val point = result?.geometry?.firstOrNull()?.point
                    val name = result?.name
                    val address = result?.descriptionText

                    if (point != null) {
                        _searchResultPoint.value = point
                        _searchResultName.value = name
                        _searchResultAddress.value = address
                    }
                }

                override fun onSearchError(p0: Error) {
                    Log.e("SearchError", "Ошибка поиска")
                }
            }
        )
    }
}