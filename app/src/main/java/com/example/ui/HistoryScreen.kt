package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SavedArea
import com.example.util.AreaCalculator
import com.example.util.MapUnit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: MapViewModel,
    modifier: Modifier = Modifier
) {
    val savedAreas by viewModel.savedAreas.collectAsStateWithLifecycle()
    val selectedUnit by viewModel.selectedUnit.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    // Filter items based on query
    val filteredAreas = remember(savedAreas, searchQuery) {
        if (searchQuery.isBlank()) {
            savedAreas
        } else {
            val q = searchQuery.lowercase()
            savedAreas.filter {
                it.name.lowercase().contains(q) ||
                        it.description.lowercase().contains(q) ||
                        it.category.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // History Title / Delete All bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "History Log",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Area Catalog",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (savedAreas.isNotEmpty()) {
                TextButton(
                    onClick = { showDeleteAllDialog = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("delete_all_areas_button")
                ) {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear all database")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear All")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Textfield
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, tags, notes...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear text",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_history_input"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Empty Status placeholder with a helpful drawing tip
        if (filteredAreas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Modern placeholder styling
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terrain,
                            contentDescription = "Blank",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = if (savedAreas.isEmpty()) "No Saved Measurements Yet" else "No matches found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (savedAreas.isEmpty()) {
                            "Navigate to the Map, tap three or more nodes of boundaries, and press 'Save Area' to chronicle your land reports here."
                        } else {
                            "Try different keywords to look up your agricultural zones or saved locations."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Lazy Column Area list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("saved_areas_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredAreas, key = { it.id }) { area ->
                    SavedAreaItemCard(
                        area = area,
                        displayUnit = selectedUnit,
                        onClick = { viewModel.selectAndFocusSavedArea(area) },
                        onDelete = { viewModel.deleteSavedArea(area.id) }
                    )
                }
            }
        }

        // Wipe Entire DB Dialog
        if (showDeleteAllDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAllDialog = false },
                title = { Text("Erase All Saved Records?") },
                text = { Text("This operation is permanent. It will empty all listed property boundaries and coordinates records from your catalog.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearAllSavedAreas()
                            showDeleteAllDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete All")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAllDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SavedAreaItemCard(
    area: SavedArea,
    displayUnit: MapUnit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(area.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())
        sdf.format(Date(area.timestamp))
    }

    val formattedMetricStr = remember(area.areaSquareMeters, displayUnit, area.mode) {
        if (area.mode.equals("Area", ignoreCase = true)) {
            AreaCalculator.formatArea(area.areaSquareMeters).getFormattedString(displayUnit)
        } else {
            AreaCalculator.formatPerimeter(area.areaSquareMeters).getFormattedString(displayUnit)
        }
    }

    val categoryIcon = getCategoryIcon(area.category)
    val categoryColor = getCategoryColor(area.category)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_area_card_${area.id}")
    ) {
        // Shadow asymmetry layout detail
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 2.dp)
                .background(Color.Transparent),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.height(100.dp)) {}
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Avatar Circle
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = area.category,
                        tint = categoryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Metadata Core
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = area.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }

                    if (area.description.isNotBlank()) {
                        Text(
                            text = area.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category pill tag
                        Text(
                            text = area.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = categoryColor,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(categoryColor.copy(alpha = 0.08f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )

                        // Mode pill tag
                        Text(
                            text = area.mode,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )

                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Measurements Display & Delete Action
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formattedMetricStr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    IconButton(
                        onClick = { onDelete() },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("delete_area_card_${area.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove record",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "farm" -> Icons.Default.Agriculture
        "property" -> Icons.Default.Map
        "backyard" -> Icons.Default.Terrain
        "lakes/water" -> Icons.Default.Water
        "forestry" -> Icons.Default.Forest
        "real estate" -> Icons.Default.LocationCity
        else -> Icons.Default.Layers
    }
}

private fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "farm" -> Color(0xFFE5A93B) // Golden
        "property" -> Color(0xFF3B8BE5) // Blue
        "backyard" -> Color(0xFF4CAF50) // Emerald Green
        "lakes/water" -> Color(0xFF00BCD4) // Teal
        "forestry" -> Color(0xFF2E7D32) // Forest green
        "real estate" -> Color(0xFF9C27B0) // Purple
        else -> Color(0xFF757575) // Neutral Grey
    }
}
