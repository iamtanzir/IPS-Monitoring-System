package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SolarBgDark
import com.example.ui.theme.SolarCyan
import com.example.ui.theme.SolarHeroBg
import com.example.ui.theme.SolarHeroText
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SolarViewModel = viewModel()
            val settings by viewModel.settings.collectAsState()
            val accentColor = viewModel.getAccentColor()

            MyApplicationTheme(accentColor = accentColor) {
                var selectedTabIndex by remember { mutableStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = SolarBgDark,
                    contentWindowInsets = WindowInsets(0.dp),
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF0C0C10),
                            tonalElevation = 0.dp,
                            modifier = Modifier.border(BorderStroke(1.dp, Color(0xFF1C1C24)))
                        ) {
                            NavigationBarItem(
                                selected = selectedTabIndex == 0,
                                onClick = { selectedTabIndex = 0 },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { 
                                    Text(
                                        "Home", 
                                        fontSize = 11.sp, 
                                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium
                                    ) 
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SolarCyan,
                                    selectedTextColor = SolarCyan,
                                    indicatorColor = SolarCyan.copy(alpha = 0.15f),
                                    unselectedIconColor = Color(0xFF70707C),
                                    unselectedTextColor = Color(0xFF70707C)
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTabIndex == 1,
                                onClick = { selectedTabIndex = 1 },
                                icon = { Icon(Icons.Default.Analytics, contentDescription = "Summary") },
                                label = { 
                                    Text(
                                        "Summary", 
                                        fontSize = 11.sp, 
                                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium
                                    ) 
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SolarCyan,
                                    selectedTextColor = SolarCyan,
                                    indicatorColor = SolarCyan.copy(alpha = 0.15f),
                                    unselectedIconColor = Color(0xFF70707C),
                                    unselectedTextColor = Color(0xFF70707C)
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTabIndex == 2,
                                onClick = { selectedTabIndex = 2 },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { 
                                    Text(
                                        "Settings", 
                                        fontSize = 11.sp, 
                                        fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Medium
                                    ) 
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SolarCyan,
                                    selectedTextColor = SolarCyan,
                                    indicatorColor = SolarCyan.copy(alpha = 0.15f),
                                    unselectedIconColor = Color(0xFF70707C),
                                    unselectedTextColor = Color(0xFF70707C)
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding().coerceIn(0.dp, 32.dp)
                    val safeTopPadding = if (statusBarHeight > 0.dp) statusBarHeight else 8.dp
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SolarBgDark)
                            .padding(
                                top = safeTopPadding,
                                bottom = innerPadding.calculateBottomPadding()
                            )
                    ) {
                        when (selectedTabIndex) {
                            0 -> SolarHomeView(viewModel = viewModel)
                            1 -> SolarSummaryView(viewModel = viewModel)
                            2 -> SolarSettingsView(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
