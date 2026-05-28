package com.example.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.SavedArea
import com.example.data.SavedAreaRepository
import com.example.util.AreaCalculator
import com.example.util.MapUnit
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MeasureMode {
    AREA,
    DISTANCE,
    WALKING;

    fun getDisplayName(): String {
        return when (this) {
            AREA -> "Area Mode"
            DISTANCE -> "Distance Mode"
            WALKING -> "Walking Mode"
        }
    }
}

class MapViewModel(private val repository: SavedAreaRepository) : ViewModel() {

    // Current markers/points being drawn
    private val _points = MutableStateFlow<List<LatLng>>(emptyList())
    val points: StateFlow<List<LatLng>> = _points.asStateFlow()

    // Active mode selection
    private val _currentMode = MutableStateFlow(MeasureMode.AREA)
    val currentMode: StateFlow<MeasureMode> = _currentMode.asStateFlow()

    // Location Tracking State
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    private val _isTrackingRunning = MutableStateFlow(false)
    val isTrackingRunning: StateFlow<Boolean> = _isTrackingRunning.asStateFlow()

    // Map customization settings (Default states initialized from settings)
    private val _mapType = MutableStateFlow(MapType.NORMAL)
    val mapType: StateFlow<MapType> = _mapType.asStateFlow()

    private val _selectedUnit = MutableStateFlow(MapUnit.METERS)
    val selectedUnit: StateFlow<MapUnit> = _selectedUnit.asStateFlow()

    // Preferences Settings States
    val defaultUnitPreference = MutableStateFlow(MapUnit.METERS)
    val defaultMapTypePreference = MutableStateFlow(MapType.NORMAL)
    val defaultModePreference = MutableStateFlow(MeasureMode.AREA)
    val enableSimulationSetting = MutableStateFlow(true)
    val hapticFeedbackSetting = MutableStateFlow(true)

    // Dialog state for saving
    private val _showSaveDialog = MutableStateFlow(false)
    val showSaveDialog: StateFlow<Boolean> = _showSaveDialog.asStateFlow()

    // Saved projects from room DB
    val savedAreas: StateFlow<List<SavedArea>> = repository.allSavedAreas
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current bottom navigation screen routing (0 for Map, 1 for Catalog, 2 for Settings)
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Selected saved area details
    private val _selectedSavedArea = MutableStateFlow<SavedArea?>(null)
    val selectedSavedArea: StateFlow<SavedArea?> = _selectedSavedArea.asStateFlow()

    // Camera target to trigger fly-to animations
    private val _cameraTarget = MutableStateFlow<CameraPositionEvent?>(null)
    val cameraTarget: StateFlow<CameraPositionEvent?> = _cameraTarget.asStateFlow()

    // State metrics computed in real-time
    val areaSquareMeters: StateFlow<Double> = _points.map { pts ->
        if (_currentMode.value == MeasureMode.AREA) {
            AreaCalculator.calculateSquareMeters(pts)
        } else {
            0.0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val perimeterMeters: StateFlow<Double> = _points.map { pts ->
        val isClosed = _currentMode.value == MeasureMode.AREA
        AreaCalculator.calculatePerimeterMeters(pts, closed = isClosed)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalDistanceMeters: StateFlow<Double> = _points.map { pts ->
        AreaCalculator.calculatePerimeterMeters(pts, closed = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Dialog input fields
    var inputName by mutableStateOf("")
    var inputDescription by mutableStateOf("")
    var inputCategory by mutableStateOf("Property")

    fun addPoint(latLng: LatLng) {
        _selectedSavedArea.value = null
        _points.value = _points.value + latLng
    }

    fun removePointAt(index: Int) {
        if (index in _points.value.indices) {
            _selectedSavedArea.value = null
            val updated = _points.value.toMutableList()
            updated.removeAt(index)
            _points.value = updated
        }
    }

    fun undoLastPoint() {
        val current = _points.value
        if (current.isNotEmpty()) {
            _selectedSavedArea.value = null
            _points.value = current.dropLast(1)
        }
    }

    fun clearPoints() {
        _selectedSavedArea.value = null
        _points.value = emptyList()
    }

    fun setMapType(type: MapType) {
        _mapType.value = type
    }

    fun setSelectedUnit(unit: MapUnit) {
        _selectedUnit.value = unit
    }

    fun setCurrentMode(mode: MeasureMode) {
        // Stop tracking if changing away from Walking Mode
        if (mode != MeasureMode.WALKING) {
            stopWalkingTracking()
        }
        _currentMode.value = mode
        _selectedSavedArea.value = null
        _points.value = emptyList() // clear current points on mode change to avoid mixing types
    }

    // Walking GPS Tracking Engine
    @SuppressLint("MissingPermission")
    fun startWalkingTracking(context: Context) {
        if (_isTrackingRunning.value) return
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 5000L
        ).apply {
            setMinUpdateIntervalMillis(2000L)
            setMinUpdateDistanceMeters(2.0f)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    addPoint(latLng)
                }
            }
        }

        try {
            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
            _isTrackingRunning.value = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopWalkingTracking() {
        if (!_isTrackingRunning.value) return
        try {
            locationCallback?.let {
                fusedLocationClient?.removeLocationUpdates(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _isTrackingRunning.value = false
    }

    // Simulated GPS walking coordination helper for browser developers
    fun simulateWalkingStep() {
        val currentPts = _points.value
        val lastPt = if (currentPts.isNotEmpty()) {
            currentPts.last()
        } else {
            // Default center start if empty list (SF Googleplex)
            LatLng(37.4220, -122.0841)
        }

        // Add small random noise to make a walking path
        val latJitter = (Math.random() - 0.5) * 0.00015
        val lngJitter = (Math.random() - 0.5) * 0.00015
        val nextPt = LatLng(lastPt.latitude + latJitter, lastPt.longitude + lngJitter)

        _points.value = _points.value + nextPt
        
        // Auto trigger camera centering so the developer visually follows the simulated walker
        _cameraTarget.value = CameraPositionEvent(nextPt, 17f)
    }

    // Save Settings Config Preferences
    fun applySettingsPreferences() {
        _selectedUnit.value = defaultUnitPreference.value
        _mapType.value = defaultMapTypePreference.value
        setCurrentMode(defaultModePreference.value)
    }

    fun setShowSaveDialog(show: Boolean) {
        if (show) {
            inputName = ""
            inputDescription = ""
            inputCategory = when (_currentMode.value) {
                MeasureMode.AREA -> "Property"
                MeasureMode.DISTANCE -> "Real Estate"
                MeasureMode.WALKING -> "Farm"
            }
        }
        _showSaveDialog.value = show
    }

    fun changeTab(tabIndex: Int) {
        _currentTab.value = tabIndex
    }

    fun saveCurrentArea() {
        val pts = _points.value
        val modeVal = _currentMode.value
        
        // Metric is Area for AREA mode, otherwise total distance
        val primaryMetric = if (modeVal == MeasureMode.AREA) {
            areaSquareMeters.value
        } else {
            totalDistanceMeters.value
        }

        val requiredPoints = if (modeVal == MeasureMode.AREA) 3 else 2
        if (pts.size < requiredPoints || primaryMetric <= 0.0 || inputName.isBlank()) return

        viewModelScope.launch {
            val entity = SavedArea.fromLatLngList(
                name = inputName.trim(),
                description = inputDescription.trim(),
                category = inputCategory.trim(),
                areaSquareMeters = primaryMetric,
                points = pts,
                mode = modeVal.name
            )
            repository.insert(entity)
            setShowSaveDialog(false)
        }
    }

    fun deleteSavedArea(id: Int) {
        viewModelScope.launch {
            if (_selectedSavedArea.value?.id == id) {
                _selectedSavedArea.value = null
            }
            repository.deleteById(id)
        }
    }

    fun clearAllSavedAreas() {
        viewModelScope.launch {
            _selectedSavedArea.value = null
            repository.deleteAll()
        }
    }

    fun selectAndFocusSavedArea(area: SavedArea) {
        _selectedSavedArea.value = area
        val pts = area.getLatLngList()
        
        // Setup state mode to reflect the imported layout mode
        val modeParsed = try {
            MeasureMode.valueOf(area.mode)
        } catch (e: Exception) {
            MeasureMode.AREA
        }
        _currentMode.value = modeParsed
        _points.value = pts

        // Calculate center for focusing camera
        val center = getPolygonCenter(pts)
        if (center != null) {
            val zoom = getAppropriateZoomLevel(pts)
            _cameraTarget.value = CameraPositionEvent(LatLng(center.latitude, center.longitude), zoom)
        }
        // Rotate back to map screen
        _currentTab.value = 0
    }

    fun consumeCameraEvent() {
        _cameraTarget.value = null
    }

    private fun getPolygonCenter(points: List<LatLng>): LatLng? {
        if (points.isEmpty()) return null
        var minLat = 90.0
        var maxLat = -90.0
        var minLng = 180.0
        var maxLng = -180.0
        for (p in points) {
            minLat = minOf(minLat, p.latitude)
            maxLat = maxOf(maxLat, p.latitude)
            minLng = minOf(minLng, p.longitude)
            maxLng = maxOf(maxLng, p.longitude)
        }
        return LatLng((minLat + maxLat) / 2.0, (minLng + maxLng) / 2.0)
    }

    private fun getAppropriateZoomLevel(points: List<LatLng>): Float {
        if (points.isEmpty()) return 15f
        var minLat = 90.0
        var maxLat = -90.0
        var minLng = 180.0
        var maxLng = -180.0
        for (p in points) {
            minLat = minOf(minLat, p.latitude)
            maxLat = maxOf(maxLat, p.latitude)
            minLng = minOf(minLng, p.longitude)
            maxLng = maxOf(maxLng, p.longitude)
        }

        val latDiff = maxLat - minLat
        val lngDiff = maxLng - minLng
        val maxDiff = maxOf(latDiff, lngDiff)

        return when {
            maxDiff < 0.001 -> 18f
            maxDiff < 0.005 -> 16f
            maxDiff < 0.01 -> 15f
            maxDiff < 0.05 -> 13f
            maxDiff < 0.1 -> 12f
            maxDiff < 0.5 -> 10f
            maxDiff < 1.0 -> 9f
            else -> 6f
        }
    }
}

data class CameraPositionEvent(
    val target: LatLng,
    val zoom: Float
)

class MapViewModelFactory(private val repository: SavedAreaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
