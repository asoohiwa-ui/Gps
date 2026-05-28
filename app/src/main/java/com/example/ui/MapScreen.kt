package com.example.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.AreaCalculator
import com.example.util.MapUnit
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    viewModel: MapViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val points by viewModel.points.collectAsStateWithLifecycle()
    val mapType by viewModel.mapType.collectAsStateWithLifecycle()
    val selectedUnit by viewModel.selectedUnit.collectAsStateWithLifecycle()
    val showSaveDialog by viewModel.showSaveDialog.collectAsStateWithLifecycle()

    val currentMode by viewModel.currentMode.collectAsStateWithLifecycle()
    val isTrackingRunning by viewModel.isTrackingRunning.collectAsStateWithLifecycle()
    val enableSimulationSetting by viewModel.enableSimulationSetting.collectAsStateWithLifecycle()

    val sqm by viewModel.areaSquareMeters.collectAsStateWithLifecycle()
    val perimeter by viewModel.perimeterMeters.collectAsStateWithLifecycle()
    val totalDistance by viewModel.totalDistanceMeters.collectAsStateWithLifecycle()
    val selectedSavedArea by viewModel.selectedSavedArea.collectAsStateWithLifecycle()

    val formattedArea = AreaCalculator.formatArea(sqm)
    val formattedPerimeter = AreaCalculator.formatPerimeter(perimeter)
    val formattedDistance = AreaCalculator.formatPerimeter(totalDistance)

    // Camera State
    val cameraPositionState = rememberCameraPositionState {
        // Default Googleplex SF coordinates
        position = CameraPosition.fromLatLngZoom(LatLng(37.4220, -122.0841), 16f)
    }

    // Observe flew-to triggers
    val cameraTargetEvent by viewModel.cameraTarget.collectAsStateWithLifecycle()
    LaunchedEffect(cameraTargetEvent) {
        cameraTargetEvent?.let { event ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(event.target, event.zoom),
                1000
            )
            viewModel.consumeCameraEvent()
        }
    }

    // Permission launcher
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasLocationPermission && currentMode == MeasureMode.WALKING && isTrackingRunning) {
            viewModel.startWalkingTracking(context)
        }
    }

    // Map UI Setting Toggle Dropdowns
    var showMapTypeDropdown by remember { mutableStateOf(false) }
    var showUnitDropdown by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // Real Google Map
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .testTag("interactive_google_map"),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapType = mapType,
                isMyLocationEnabled = hasLocationPermission
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = false,
                zoomControlsEnabled = false,
                tiltGesturesEnabled = true
            ),
            onMapClick = { latLng ->
                if (currentMode != MeasureMode.WALKING) {
                    viewModel.addPoint(latLng)
                }
            }
        ) {
            // Draw markers for all vertices
            points.forEachIndexed { index, point ->
                Marker(
                    state = MarkerState(position = point),
                    title = "Node #${index + 1}",
                    snippet = "Tap to remove",
                    onClick = {
                        if (currentMode != MeasureMode.WALKING) {
                            viewModel.removePointAt(index)
                        }
                        true
                    }
                )
            }

            // Draw styling-accurate transparent polygon or polyline based on mode
            if (currentMode == MeasureMode.AREA && points.size >= 3) {
                Polygon(
                    points = points,
                    fillColor = Color(0x443DDC84), // Translucent Primary Green
                    strokeColor = Color(0xFF3DDC84),
                    strokeWidth = 6f
                )
            } else if ((currentMode == MeasureMode.DISTANCE || currentMode == MeasureMode.WALKING) && points.size >= 2) {
                Polyline(
                    points = points,
                    color = Color(0xFF3B8BE5), // Secondary blue line for distances
                    width = 8f
                )
            }
        }

        // Live Calculations Floating Card Overlay (Sticky Top)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (currentMode) {
                                    MeasureMode.AREA -> Icons.Default.SquareFoot
                                    MeasureMode.DISTANCE -> Icons.Default.Map
                                    MeasureMode.WALKING -> Icons.Default.DirectionsWalk
                                },
                                contentDescription = "Active Mode Icon",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = selectedSavedArea?.name ?: when (currentMode) {
                                    MeasureMode.AREA -> "Area Measure"
                                    MeasureMode.DISTANCE -> "Distance Measure"
                                    MeasureMode.WALKING -> "Walking Live GPS"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (selectedSavedArea != null) {
                                Text(
                                    text = "Viewing saved compilation",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Unit selector button
                    Button(
                        onClick = { showUnitDropdown = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.height(32.dp).testTag("unit_selector_pill")
                    ) {
                        Text(
                            text = selectedUnit.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Premium Interactive Mode Segments Selection Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MeasureMode.values().forEach { mode ->
                        val isSelected = currentMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { viewModel.setCurrentMode(mode) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.getDisplayName().split(" ")[0],
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.testTag("mode_tab_${mode.name.lowercase()}")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Live dynamic stats display depending on the active mode selection
                if (currentMode == MeasureMode.AREA) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Calculated Area Card inside header
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "CALCULATED AREA",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (points.size >= 3) formattedArea.getFormattedString(selectedUnit) else "Need 3+ points",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.testTag("area_text_view")
                                )
                            }
                        }

                        // Perimeter Card
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "PERIMETER",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (points.size >= 2) formattedPerimeter.getFormattedString(selectedUnit) else "Need 2+ points",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    // Distance or Walking Mode: show cumulative progressive distance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (currentMode == MeasureMode.WALKING) "GPS WALK DISTANCE" else "PATH DISTANCE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (points.size >= 2) formattedDistance.getFormattedString(selectedUnit) else "0.0 m (Need 2+ nodes)",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.testTag("distance_text_view")
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "TOTAL NODES",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${points.size} coordinates",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // GPS Tracking controller buttons for WALKING MODE
                if (currentMode == MeasureMode.WALKING) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isTrackingRunning) {
                            Button(
                                onClick = {
                                    if (hasLocationPermission) {
                                        viewModel.startWalkingTracking(context)
                                    } else {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.weight(1f).testTag("start_walk_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start Walk", fontSize = 13.sp)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.stopWalkingTracking() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f).testTag("stop_walk_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Stop Tracking", fontSize = 13.sp)
                            }
                        }

                        // Simulator triggers for checking walk maps in browser environment
                        if (enableSimulationSetting) {
                            OutlinedButton(
                                onClick = { viewModel.simulateWalkingStep() },
                                modifier = Modifier.weight(1.2f).testTag("simulate_step_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.DirectionsWalk, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Simulate Step", fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Instructions if points are insufficient
                val minNeeded = if (currentMode == MeasureMode.AREA) 3 else 2
                if (points.size < minNeeded) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Help Info",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (currentMode) {
                                MeasureMode.AREA -> {
                                    if (points.isEmpty()) "Tap Map to start plotting boundary vertices."
                                    else if (points.size == 1) "Plot a 2nd boundary node to begin."
                                    else "Plot a 3rd boundary node to enclose and view calculated area."
                                }
                                MeasureMode.DISTANCE -> {
                                    if (points.isEmpty()) "Tap along any pathways to log node coordinates."
                                    else "Plot a 2nd boundary node to measure path lengths."
                                }
                                MeasureMode.WALKING -> {
                                    if (points.isEmpty()) "Tap 'Start Walk' or 'Simulate Step' to record GPS tracks."
                                    else "Tracking coordinates automatically as you walk boundaries."
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Side Menu Map Configuration Options (Layers dropdown and center-to-GPS fab)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Map type picker button
            FloatingActionButton(
                onClick = { showMapTypeDropdown = true },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp).testTag("map_type_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Map Types"
                )
            }

            // Current location tracker FAB
            FloatingActionButton(
                onClick = {
                    if (hasLocationPermission) {
                        // Move to user GPS location
                        val locationClient = LocationServices.getFusedLocationProviderClient(context)
                        locationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null) {
                                coroutineScope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(location.latitude, location.longitude),
                                            16f
                                        ),
                                        1000
                                    )
                                }
                            }
                        }
                    } else {
                        // Request permissions
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = if (hasLocationPermission) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(44.dp).testTag("gps_location_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Center on My Location"
                )
            }
        }

        // Action Overlay Controls Bar (Floating bottom center, hides if WALKING to avoid manual interference)
        if (currentMode != MeasureMode.WALKING || points.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .align(Alignment.BottomCenter),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // CLEAR BUTTON
                    IconButton(
                        onClick = { viewModel.clearPoints() },
                        enabled = points.isNotEmpty(),
                        modifier = Modifier.testTag("clear_map_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear All Vertices",
                            tint = if (points.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                        )
                    }

                    // UNDO BUTTON (disabled in Walking Mode)
                    IconButton(
                        onClick = { viewModel.undoLastPoint() },
                        enabled = points.isNotEmpty() && currentMode != MeasureMode.WALKING,
                        modifier = Modifier.testTag("undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo Vertex",
                            tint = if (points.isNotEmpty() && currentMode != MeasureMode.WALKING) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }

                    // SAVE MAP REGION BUTTON
                    val requiredPoints = if (currentMode == MeasureMode.AREA) 3 else 2
                    val meetingMinPoints = points.size >= requiredPoints

                    Button(
                        onClick = { viewModel.setShowSaveDialog(true) },
                        enabled = meetingMinPoints,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .padding(horizontal = 8.dp)
                            .testTag("save_polygon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save project"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentMode == MeasureMode.AREA) "Save Area" else "Save Path",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Dropdown Menu: Map Layers Types
        DropdownMenu(
            expanded = showMapTypeDropdown,
            onDismissRequest = { showMapTypeDropdown = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = { Text("Normal Map") },
                onClick = {
                    viewModel.setMapType(MapType.NORMAL)
                    showMapTypeDropdown = false
                },
                leadingIcon = { Icon(Icons.Default.Map, "Normal") }
            )
            DropdownMenuItem(
                text = { Text("Satellite") },
                onClick = {
                    viewModel.setMapType(MapType.SATELLITE)
                    showMapTypeDropdown = false
                },
                leadingIcon = { Icon(Icons.Default.Layers, "Satellite") }
            )
            DropdownMenuItem(
                text = { Text("Terrain") },
                onClick = {
                    viewModel.setMapType(MapType.TERRAIN)
                    showMapTypeDropdown = false
                },
                leadingIcon = { Icon(Icons.Default.Layers, "Terrain") }
            )
            DropdownMenuItem(
                text = { Text("Hybrid") },
                onClick = {
                    viewModel.setMapType(MapType.HYBRID)
                    showMapTypeDropdown = false
                },
                leadingIcon = { Icon(Icons.Default.Layers, "Hybrid") }
            )
        }

        // Dropdown Menu: Measurement Unit Selection
        DropdownMenu(
            expanded = showUnitDropdown,
            onDismissRequest = { showUnitDropdown = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            MapUnit.values().forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.getDisplayName()) },
                    onClick = {
                        viewModel.setSelectedUnit(unit)
                        showUnitDropdown = false
                    },
                    leadingIcon = {
                        RadioButton(
                            selected = (selectedUnit == unit),
                            onClick = {
                                viewModel.setSelectedUnit(unit)
                                showUnitDropdown = false
                            }
                        )
                    }
                )
            }
        }

        // Savable Dialogue Box
        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.setShowSaveDialog(false) },
                icon = { Icon(Icons.Default.LocationOn, contentDescription = "Pin Icon", tint = MaterialTheme.colorScheme.primary) },
                title = { Text(text = if (currentMode == MeasureMode.AREA) "Save Area Measurement" else "Save Path Measurement") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = if (currentMode == MeasureMode.AREA) {
                                "Save this ${formattedArea.getFormattedString(selectedUnit)} region to your history catalog."
                            } else {
                                "Save this ${formattedDistance.getFormattedString(selectedUnit)} path to your history catalog."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Name input
                        OutlinedTextField(
                            value = viewModel.inputName,
                            onValueChange = { viewModel.inputName = it },
                            label = { Text("Project Name") },
                            placeholder = { Text("E.g. Orchard Fence Line") },
                            modifier = Modifier.fillMaxWidth().testTag("save_dialog_name_input"),
                            singleLine = true,
                            isError = viewModel.inputName.isBlank()
                        )

                        // Description input
                        OutlinedTextField(
                            value = viewModel.inputDescription,
                            onValueChange = { viewModel.inputDescription = it },
                            label = { Text("Description / Notes") },
                            placeholder = { Text("E.g. Measured via GPS auto points") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )

                        // Category select row
                        Column {
                            Text(
                               text = "Category Tag",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Property", "Farm", "Backyard", "Lakes/Water", "Forestry", "Real Estate").forEach { cat ->
                                    val isSelected = viewModel.inputCategory == cat
                                    SuggestionChip(
                                        onClick = { viewModel.inputCategory = cat },
                                        label = { Text(cat) },
                                        icon = {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Place,
                                                    contentDescription = "Selected",
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        },
                                        modifier = Modifier.testTag("tag_chip_$cat")
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.saveCurrentArea() },
                        enabled = viewModel.inputName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("save_dialog_confirm_button")
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.setShowSaveDialog(false) }) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(26.dp)
            )
        }
    }
}
