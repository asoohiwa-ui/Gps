package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.MapUnit
import com.google.maps.android.compose.MapType

@Composable
fun SettingsScreen(
    viewModel: MapViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Dropdown expanding states
    var unitMenuExpanded by remember { mutableStateOf(false) }
    var modeMenuExpanded by remember { mutableStateOf(false) }
    var mapTypeMenuExpanded by remember { mutableStateOf(false) }

    // VM Settings States
    val defaultUnit by viewModel.defaultUnitPreference.collectAsState()
    val defaultMapType by viewModel.defaultMapTypePreference.collectAsState()
    val defaultMode by viewModel.defaultModePreference.collectAsState()
    val enableSimulation by viewModel.enableSimulationSetting.collectAsState()
    val hapticFeedback by viewModel.hapticFeedbackSetting.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App settings header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings Page",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Application Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Configure measurements, map styling, and behaviors",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Section 1: Map Preferences configuration card
        SettingsSectionCard(title = "Measurement Preferences") {
            // Default Unit Preference Dropdown
            SettingsRow(
                icon = Icons.Default.SquareFoot,
                title = "Primary Unit Category",
                subtitle = "Choose standard metrics for display",
                action = {
                    Box {
                        OutlinedButton(
                            onClick = { unitMenuExpanded = true },
                            modifier = Modifier.testTag("setting_unit_dropdown")
                        ) {
                            Text(defaultUnit.getDisplayName())
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand units")
                        }
                        DropdownMenu(
                            expanded = unitMenuExpanded,
                            onDismissRequest = { unitMenuExpanded = false }
                        ) {
                            MapUnit.values().forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit.getDisplayName()) },
                                    onClick = {
                                        viewModel.defaultUnitPreference.value = unit
                                        viewModel.applySettingsPreferences()
                                        unitMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Default Mode preference dropdown
            SettingsRow(
                icon = Icons.Default.Palette,
                title = "Default Measurement Mode",
                subtitle = "Active mode when initializing maps",
                action = {
                    Box {
                        OutlinedButton(
                            onClick = { modeMenuExpanded = true },
                            modifier = Modifier.testTag("setting_mode_dropdown")
                        ) {
                            Text(defaultMode.getDisplayName())
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand modes")
                        }
                        DropdownMenu(
                            expanded = modeMenuExpanded,
                            onDismissRequest = { modeMenuExpanded = false }
                        ) {
                            MeasureMode.values().forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode.getDisplayName()) },
                                    onClick = {
                                        viewModel.defaultModePreference.value = mode
                                        viewModel.applySettingsPreferences()
                                        modeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Default Map Type Preference Dropdown
            SettingsRow(
                icon = Icons.Default.Layers,
                title = "Default Map Satellites",
                subtitle = "Map backdrop layer when rendered",
                action = {
                    Box {
                        OutlinedButton(
                            onClick = { mapTypeMenuExpanded = true },
                            modifier = Modifier.testTag("setting_map_dropdown")
                        ) {
                            Text(defaultMapType.name)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Expand styles")
                        }
                        DropdownMenu(
                            expanded = mapTypeMenuExpanded,
                            onDismissRequest = { mapTypeMenuExpanded = false }
                        ) {
                            listOf(MapType.NORMAL, MapType.SATELLITE, MapType.TERRAIN, MapType.HYBRID).forEach { mType ->
                                DropdownMenuItem(
                                    text = { Text(mType.name) },
                                    onClick = {
                                        viewModel.defaultMapTypePreference.value = mType
                                        viewModel.applySettingsPreferences()
                                        mapTypeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }

        // Section 2: General Settings Configuration
        SettingsSectionCard(title = "General & Simulation Settings") {
            SettingsRow(
                icon = Icons.Default.PlayArrow,
                title = "Walker Path Simulation",
                subtitle = "Allows manual simulation steps on Walk mode",
                action = {
                    Switch(
                        checked = enableSimulation,
                        onCheckedChange = { viewModel.enableSimulationSetting.value = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SettingsRow(
                icon = Icons.Default.Vibration,
                title = "Haptic Node Feedback",
                subtitle = "Tactile click when plotting coordinates",
                action = {
                    Switch(
                        checked = hapticFeedback,
                        onCheckedChange = { viewModel.hapticFeedbackSetting.value = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            )
        }

        // Section 3: Visual Guides / Help Instructions Card
        SettingsSectionCard(title = "Measurement Mode Guidebook") {
            GuidebookItem(
                title = "Area Mode",
                icon = Icons.Default.SquareFoot,
                description = "Tap on 3 or more locations on the map. The app will automatically compute the closed geometric surface area enclosed by your points and display it in your selected units."
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            GuidebookItem(
                title = "Distance Mode",
                icon = Icons.Default.Map,
                description = "Tap along any pathway or property boundaries. Calculates overall distances between all consecutive nodes as an open string path (perfect for fence alignment or track mapping)."
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            GuidebookItem(
                title = "Walking Mode",
                icon = Icons.Default.DirectionsWalk,
                description = "Walk around boundaries with high-accuracy GPS activated. The system appends path nodes automatically in real-time as you walk. Use the simulator toggle if checking inside buildings."
            )
        }

        // Section 4: Critical Data Integrity / Destructive tools
        SettingsSectionCard(title = "Administrative & Maintenance") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDeleteConfirmDialog = true }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Clear All Database Entries",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Clear Local History Database",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                    Text(
                        text = "Irreversibly delete all measured projects and coordinates",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Confirmation Alert dialogue
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Confirm Erase History",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you absolutely sure you want to delete all saved measurement projects? This action cannot be undone and will restore your database to an empty catalog."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllSavedAreas()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("confirm_delete_all")
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    content: @Composable ColumnScopeWrapper.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            ColumnScopeWrapper().content()
        }
    }
}

class ColumnScopeWrapper {
    @Composable
    fun SettingsRow(
        icon: ImageVector,
        title: String,
        subtitle: String,
        action: @Composable () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            action()
        }
    }

    @Composable
    fun GuidebookItem(
        title: String,
        icon: ImageVector,
        description: String
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
