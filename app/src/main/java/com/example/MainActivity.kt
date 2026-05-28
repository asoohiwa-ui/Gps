package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.SavedAreaRepository
import com.example.ui.HistoryScreen
import com.example.ui.MapScreen
import com.example.ui.SettingsScreen
import com.example.ui.MapViewModel
import com.example.ui.MapViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.material.icons.filled.Settings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Turn on Edge-to-Edge full render layouts
        enableEdgeToEdge()
        
        // Get database infrastructure singleton
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = SavedAreaRepository(database.savedAreaDao())

        setContent {
            MyApplicationTheme {
                val viewModel: MapViewModel = viewModel(
                    factory = MapViewModelFactory(repository)
                )

                val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp,
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("bottom_nav_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { viewModel.changeTab(0) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = "Map measurer screen"
                                    )
                                },
                                label = { Text("Map Measure") },
                                modifier = Modifier.testTag("nav_tab_map")
                            )

                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { viewModel.changeTab(1) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "Measurement history catalog"
                                    )
                                },
                                label = { Text("History") },
                                modifier = Modifier.testTag("nav_tab_history")
                            )

                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { viewModel.changeTab(2) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings and configurations"
                                    )
                                },
                                label = { Text("Settings") },
                                modifier = Modifier.testTag("nav_tab_settings")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            0 -> {
                                MapScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            1 -> {
                                HistoryScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else -> {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
