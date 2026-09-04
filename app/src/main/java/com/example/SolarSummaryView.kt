package com.example

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SolarSummaryView(
    viewModel: SolarViewModel,
    modifier: Modifier = Modifier,
    onNavigateTab: (Int) -> Unit = {}
) {
    val settings by viewModel.settings.collectAsState()
    val statsList = viewModel.energyPeriodStats
    var selectedPeriodIndex by remember { mutableStateOf(0) }
    val currentStats = statsList.getOrElse(selectedPeriodIndex) { statsList.first() }

    var isPeriodDropdownOpen by remember { mutableStateOf(false) }
    var showAlertsDialog by remember { mutableStateOf(false) }
    var showHardwareBomModal by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SolarBgDark)
    ) {
        // 1. Top Header: "< HOME 48.0V 10000W"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onNavigateTab(0) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = "HOME 48.0V 10000W",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = { showHardwareBomModal = true },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Hardware BOM",
                    tint = SolarCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // 2. Sub-navigation Tabs: Realtime | Stats (active) | Alerts | Settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Realtime Tab
            Text(
                text = "Realtime",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF8E8E9A),
                modifier = Modifier
                    .clickable { onNavigateTab(0) }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            )

            // Stats Tab (Active with red/coral underline)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = "Stats",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(2.5.dp)
                        .background(Color(0xFFFF5252), RoundedCornerShape(2.dp))
                )
            }

            // Alerts Tab
            Text(
                text = "Alerts",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF8E8E9A),
                modifier = Modifier
                    .clickable { showAlertsDialog = true }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            )

            // Settings Tab
            Text(
                text = "Settings",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF8E8E9A),
                modifier = Modifier
                    .clickable { onNavigateTab(2) }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Scrollable List of Energy & Savings Data
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            // Energy Dropdown Filter
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            ) {
                Column {
                    Text(
                        text = "Energy",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF9E9EAA),
                        modifier = Modifier.padding(start = 12.dp, bottom = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF10131A))
                            .border(1.dp, Color(0xFF2E3444), RoundedCornerShape(12.dp))
                            .clickable { isPeriodDropdownOpen = true }
                            .padding(horizontal = 16.dp, vertical = 13.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = currentStats.period,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Period",
                                tint = Color(0xFF9E9EAA),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isPeriodDropdownOpen,
                            onDismissRequest = { isPeriodDropdownOpen = false },
                            modifier = Modifier.background(Color(0xFF161A24))
                        ) {
                            statsList.forEachIndexed { index, item ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = item.period,
                                            fontWeight = if (index == selectedPeriodIndex) FontWeight.Bold else FontWeight.Normal,
                                            color = if (index == selectedPeriodIndex) SolarCyan else TextPrimary
                                        )
                                    },
                                    onClick = {
                                        selectedPeriodIndex = index
                                        isPeriodDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Card 1: Savings
            EnergyDataRowCard(
                title = "Savings",
                value = "${currentStats.savingsAmount} ${currentStats.currency}",
                icon = { SavingsCustomIcon() }
            )

            // Card 2: Solar Energy
            EnergyDataRowCard(
                title = "Solar Energy",
                value = "${String.format("%.1f", currentStats.solarEnergyKwh)} KWh",
                icon = { SolarEnergyCustomIcon() }
            )

            // Card 3: Grid Energy
            EnergyDataRowCard(
                title = "Grid Energy",
                value = "${String.format("%.1f", currentStats.gridEnergyKwh)} KWh",
                icon = { GridPylonCustomIcon() }
            )

            // Card 4: Solar Charging
            EnergyDataRowCard(
                title = "Solar Charging",
                value = "${String.format("%.1f", currentStats.solarChargingKwh)} KWh",
                icon = { SolarChargingCustomIcon() }
            )

            // Card 5: Grid Charging
            EnergyDataRowCard(
                title = "Grid Charging",
                value = "${String.format("%.1f", currentStats.gridChargingKwh)} KWh",
                icon = { GridChargingCustomIcon() }
            )

            // Card 6: Battery Used
            EnergyDataRowCard(
                title = "Battery Used",
                value = "${String.format("%.1f", currentStats.batteryUsedKwh)} KWh",
                icon = { BatteryUsedCustomIcon() }
            )

            // Card 7: Total Energy
            EnergyDataRowCard(
                title = "Total Energy",
                value = "${String.format("%.1f", currentStats.totalEnergyKwh)} KWh",
                icon = { TotalEnergyCustomIcon() }
            )

            // Card 8: Load Consumed
            EnergyDataRowCard(
                title = "Load Consumed",
                value = "${String.format("%.1f", currentStats.loadConsumedKwh)} KWh",
                icon = { LoadConsumedCustomIcon() }
            )

            // Section Header: Net Metering
            Text(
                text = "Net Metering",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp, bottom = 14.dp)
            )

            // Card 9: Grid Export
            EnergyDataRowCard(
                title = "Grid Export",
                value = "${String.format("%.1f", currentStats.gridExportKwh)} KWh",
                icon = { GridPylonCustomIcon() }
            )

            // Card 10: Grid Import
            EnergyDataRowCard(
                title = "Grid Import",
                value = "${String.format("%.1f", currentStats.gridImportKwh)} KWh",
                icon = { GridPylonCustomIcon() }
            )

            // Card 11: Export After Used
            EnergyDataRowCard(
                title = "Export After Used",
                value = "${String.format("%.1f", currentStats.exportAfterUsedKwh)} KWh",
                icon = { ExportMeterCustomIcon() }
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Alerts / System Status Dialog
    if (showAlertsDialog) {
        AlertDialog(
            onDismissRequest = { showAlertsDialog = false },
            containerColor = Color(0xFF141722),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = SolarCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("System Status & Alerts", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Hardware: ESP32-C6 DevKit-C N16R8 (WiFi 6 + BLE 5.0)", fontSize = 12.sp, color = TextPrimary)
                    Text("• Microcontroller: RISC-V 32-bit @ 160MHz", fontSize = 12.sp, color = TextSecondary)
                    Text("• All sensor telemetry normal (GPIO 1 Current, GPIO 2 Voltage)", fontSize = 12.sp, color = SolarGreen)
                    Text("• Protection Auto-cut: ACTIVE & SHORT-CIRCUIT SAFE", fontSize = 12.sp, color = SolarCyan)
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            showAlertsDialog = false
                            showHardwareBomModal = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SolarCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View Complete Hardware BOM", color = Color(0xFF0C1017), fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAlertsDialog = false }) {
                    Text("Close", color = TextSecondary)
                }
            }
        )
    }

    // Hardware BOM Modal
    if (showHardwareBomModal) {
        AlertDialog(
            onDismissRequest = { showHardwareBomModal = false },
            containerColor = Color(0xFF0F1218),
            modifier = Modifier.fillMaxWidth(0.96f).fillMaxHeight(0.85f),
            title = null,
            text = {
                Box(modifier = Modifier.fillMaxSize()) {
                    HardwareBomContent(modifier = Modifier.fillMaxSize())
                }
            },
            confirmButton = {
                TextButton(onClick = { showHardwareBomModal = false }) {
                    Text("Close", color = SolarCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// Reusable Energy Row Card matching user screenshot
@Composable
private fun EnergyDataRowCard(
    title: String,
    value: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131720)),
        border = BorderStroke(1.dp, Color(0xFF1E232F)),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(34.dp),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
        }
    }
}

// --- Custom Icons Pixel-Perfect to user screenshot ---

@Composable
private fun SavingsCustomIcon() {
    Canvas(modifier = Modifier.size(28.dp)) {
        val w = size.width
        val h = size.height

        // Cash note background (green)
        drawRoundRect(
            color = Color(0xFF10B981),
            topLeft = Offset(0f, h * 0.2f),
            size = Size(w * 0.78f, h * 0.6f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        // Cash note border (lighter green)
        drawRoundRect(
            color = Color(0xFF34D399),
            topLeft = Offset(0f, h * 0.2f),
            size = Size(w * 0.78f, h * 0.6f),
            cornerRadius = CornerRadius(6f, 6f),
            style = Stroke(width = 2f)
        )
        // Center circle in bill
        drawCircle(
            color = Color(0xFF047857),
            radius = h * 0.16f,
            center = Offset(w * 0.39f, h * 0.5f)
        )
        drawCircle(
            color = Color(0xFF6EE7B7),
            radius = h * 0.14f,
            center = Offset(w * 0.39f, h * 0.5f),
            style = Stroke(width = 1.5f)
        )

        // Golden coins stacked next to it
        drawCircle(
            color = Color(0xFFF59E0B),
            radius = h * 0.18f,
            center = Offset(w * 0.82f, h * 0.38f)
        )
        drawCircle(
            color = Color(0xFFFDE68A),
            radius = h * 0.14f,
            center = Offset(w * 0.82f, h * 0.38f),
            style = Stroke(width = 1.5f)
        )

        drawCircle(
            color = Color(0xFFD97706),
            radius = h * 0.18f,
            center = Offset(w * 0.85f, h * 0.65f)
        )
        drawCircle(
            color = Color(0xFFFDE68A),
            radius = h * 0.14f,
            center = Offset(w * 0.85f, h * 0.65f),
            style = Stroke(width = 1.5f)
        )
    }
}

@Composable
private fun SolarEnergyCustomIcon() {
    Canvas(modifier = Modifier.size(28.dp)) {
        val w = size.width
        val h = size.height

        // Sun in top-right
        val sunCenter = Offset(w * 0.8f, h * 0.26f)
        drawCircle(color = Color(0xFFFBBF24), radius = h * 0.15f, center = sunCenter)
        // Sun rays
        for (i in 0 until 6) {
            val angle = (i * 60f) * (Math.PI / 180f)
            val start = Offset(
                sunCenter.x + (h * 0.18f * kotlin.math.cos(angle)).toFloat(),
                sunCenter.y + (h * 0.18f * kotlin.math.sin(angle)).toFloat()
            )
            val end = Offset(
                sunCenter.x + (h * 0.27f * kotlin.math.cos(angle)).toFloat(),
                sunCenter.y + (h * 0.27f * kotlin.math.sin(angle)).toFloat()
            )
            drawLine(
                color = Color(0xFFF59E0B),
                start = start,
                end = end,
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }

        // Angled solar panel
        val panelPath = Path().apply {
            moveTo(w * 0.1f, h * 0.88f)
            lineTo(w * 0.32f, h * 0.40f)
            lineTo(w * 0.72f, h * 0.40f)
            lineTo(w * 0.62f, h * 0.88f)
            close()
        }
        drawPath(panelPath, color = Color(0xFF2563EB))
        drawPath(panelPath, color = Color(0xFF60A5FA), style = Stroke(width = 1.8f))

        // Grid lines inside panel
        drawLine(
            color = Color(0xFF93C5FD),
            start = Offset(w * 0.21f, h * 0.64f),
            end = Offset(w * 0.67f, h * 0.64f),
            strokeWidth = 1.5f
        )
        drawLine(
            color = Color(0xFF93C5FD),
            start = Offset(w * 0.52f, h * 0.40f),
            end = Offset(w * 0.36f, h * 0.88f),
            strokeWidth = 1.5f
        )
    }
}

@Composable
private fun GridPylonCustomIcon() {
    Canvas(modifier = Modifier.size(28.dp)) {
        val w = size.width
        val h = size.height
        val pylonColor = Color(0xFF00E5FF)

        // Pylon Tower A-frame legs
        drawLine(
            color = pylonColor,
            start = Offset(w * 0.22f, h * 0.9f),
            end = Offset(w * 0.44f, h * 0.12f),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = pylonColor,
            start = Offset(w * 0.78f, h * 0.9f),
            end = Offset(w * 0.56f, h * 0.12f),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )

        // Upper crossarm
        drawLine(
            color = pylonColor,
            start = Offset(w * 0.16f, h * 0.32f),
            end = Offset(w * 0.84f, h * 0.32f),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )
        // Lower crossarm
        drawLine(
            color = pylonColor,
            start = Offset(w * 0.24f, h * 0.52f),
            end = Offset(w * 0.76f, h * 0.52f),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )

        // Cross bracing X
        drawLine(
            color = pylonColor.copy(alpha = 0.8f),
            start = Offset(w * 0.35f, h * 0.52f),
            end = Offset(w * 0.65f, h * 0.74f),
            strokeWidth = 1.6f
        )
        drawLine(
            color = pylonColor.copy(alpha = 0.8f),
            start = Offset(w * 0.65f, h * 0.52f),
            end = Offset(w * 0.35f, h * 0.74f),
            strokeWidth = 1.6f
        )
    }
}

@Composable
private fun SolarChargingCustomIcon() {
    Canvas(modifier = Modifier.size(28.dp)) {
        val w = size.width
        val h = size.height

        // Battery body
        drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(w * 0.12f, h * 0.25f),
            size = Size(w * 0.46f, h * 0.62f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = Color(0xFFFBBF24),
            topLeft = Offset(w * 0.12f, h * 0.25f),
            size = Size(w * 0.46f, h * 0.62f),
            cornerRadius = CornerRadius(4f, 4f),
            style = Stroke(width = 2f)
        )
        // Battery top terminal
        drawRoundRect(
            color = Color(0xFFFBBF24),
            topLeft = Offset(w * 0.25f, h * 0.16f),
            size = Size(w * 0.20f, h * 0.10f),
            cornerRadius = CornerRadius(2f, 2f)
        )

        // Lightning inside battery
        val boltPath = Path().apply {
            moveTo(w * 0.36f, h * 0.34f)
            lineTo(w * 0.26f, h * 0.56f)
            lineTo(w * 0.36f, h * 0.56f)
            lineTo(w * 0.32f, h * 0.76f)
            lineTo(w * 0.46f, h * 0.50f)
            lineTo(w * 0.36f, h * 0.50f)
            close()
        }
        drawPath(boltPath, color = Color(0xFFFBBF24))

        // Sun with rays on right
        val sunCenter = Offset(w * 0.78f, h * 0.55f)
        drawCircle(color = Color(0xFFF59E0B), radius = h * 0.16f, center = sunCenter)
        for (i in 0 until 8) {
            val angle = (i * 45f) * (Math.PI / 180f)
            val start = Offset(
                sunCenter.x + (h * 0.20f * kotlin.math.cos(angle)).toFloat(),
                sunCenter.y + (h * 0.20f * kotlin.math.sin(angle)).toFloat()
            )
            val end = Offset(
                sunCenter.x + (h * 0.28f * kotlin.math.cos(angle)).toFloat(),
                sunCenter.y + (h * 0.28f * kotlin.math.sin(angle)).toFloat()
            )
            drawLine(
                color = Color(0xFFFBBF24),
                start = start,
                end = end,
                strokeWidth = 1.8f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun GridChargingCustomIcon() {
    Canvas(modifier = Modifier.size(28.dp)) {
        val w = size.width
        val h = size.height

        // Battery body
        drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(w * 0.12f, h * 0.25f),
            size = Size(w * 0.46f, h * 0.62f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = Color(0xFF38BDF8),
            topLeft = Offset(w * 0.12f, h * 0.25f),
            size = Size(w * 0.46f, h * 0.62f),
            cornerRadius = CornerRadius(4f, 4f),
            style = Stroke(width = 2f)
        )
        // Battery top terminal
        drawRoundRect(
            color = Color(0xFF38BDF8),
            topLeft = Offset(w * 0.25f, h * 0.16f),
            size = Size(w * 0.20f, h * 0.10f),
            cornerRadius = CornerRadius(2f, 2f)
        )

        // Electric lightning inside battery
        val boltPath = Path().apply {
            moveTo(w * 0.36f, h * 0.34f)
            lineTo(w * 0.26f, h * 0.56f)
            lineTo(w * 0.36f, h * 0.56f)
            lineTo(w * 0.32f, h * 0.76f)
            lineTo(w * 0.46f, h * 0.50f)
            lineTo(w * 0.36f, h * 0.50f)
            close()
        }
        drawPath(boltPath, color = Color(0xFF38BDF8))

        // AC Power plug on right
        val plugColor = Color(0xFF94A3B8)
        // Prongs
        drawLine(color = plugColor, start = Offset(w * 0.72f, h * 0.40f), end = Offset(w * 0.64f, h * 0.40f), strokeWidth = 2f)
        drawLine(color = plugColor, start = Offset(w * 0.72f, h * 0.60f), end = Offset(w * 0.64f, h * 0.60f), strokeWidth = 2f)
        // Plug head
        drawRoundRect(
            color = Color(0xFF475569),
            topLeft = Offset(w * 0.72f, h * 0.32f),
            size = Size(w * 0.16f, h * 0.36f),
            cornerRadius = CornerRadius(3f, 3f)
        )
        // Cable
        drawLine(color = Color(0xFF475569), start = Offset(w * 0.88f, h * 0.50f), end = Offset(w * 0.96f, h * 0.50f), strokeWidth = 2.5f)
    }
}

@Composable
private fun BatteryUsedCustomIcon() {
    Canvas(modifier = Modifier.size(28.dp)) {
        val w = size.width
        val h = size.height

        // Top terminal
        drawRoundRect(
            color = Color(0xFF10B981),
            topLeft = Offset(w * 0.38f, h * 0.12f),
            size = Size(w * 0.24f, h * 0.12f),
            cornerRadius = CornerRadius(3f, 3f)
        )
        // Battery body outline
        drawRoundRect(
            color = Color(0xFF10B981),
            topLeft = Offset(w * 0.22f, h * 0.22f),
            size = Size(w * 0.56f, h * 0.68f),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Inner Lightning Bolt in yellow
        val boltPath = Path().apply {
            moveTo(w * 0.54f, h * 0.32f)
            lineTo(w * 0.40f, h * 0.54f)
            lineTo(w * 0.52f, h * 0.54f)
            lineTo(w * 0.46f, h * 0.78f)
            lineTo(w * 0.64f, h * 0.48f)
            lineTo(w * 0.52f, h * 0.48f)
            close()
        }
        drawPath(boltPath, color = Color(0xFFFBBF24))
    }
}

@Composable
private fun TotalEnergyCustomIcon() {
    Canvas(modifier = Modifier.size(28.dp)) {
        val w = size.width
        val h = size.height

        // House roof
        val roofPath = Path().apply {
            moveTo(w * 0.5f, h * 0.14f)
            lineTo(w * 0.88f, h * 0.48f)
            lineTo(w * 0.78f, h * 0.48f)
            lineTo(w * 0.5f, h * 0.24f)
            lineTo(w * 0.22f, h * 0.48f)
            lineTo(w * 0.12f, h * 0.48f)
            close()
        }
        drawPath(roofPath, color = Color(0xFFE2E8F0))

        // House body
        drawRoundRect(
            color = Color(0xFF334155),
            topLeft = Offset(w * 0.24f, h * 0.46f),
            size = Size(w * 0.52f, h * 0.44f),
            cornerRadius = CornerRadius(3f, 3f)
        )
        drawRoundRect(
            color = Color(0xFF94A3B8),
            topLeft = Offset(w * 0.24f, h * 0.46f),
            size = Size(w * 0.52f, h * 0.44f),
            cornerRadius = CornerRadius(3f, 3f),
            style = Stroke(width = 1.8f)
        )

        // Energy spark / lightning in warm orange
        val sparkPath = Path().apply {
            moveTo(w * 0.54f, h * 0.32f)
            lineTo(w * 0.42f, h * 0.58f)
            lineTo(w * 0.54f, h * 0.58f)
            lineTo(w * 0.48f, h * 0.82f)
            lineTo(w * 0.66f, h * 0.52f)
            lineTo(w * 0.54f, h * 0.52f)
            close()
        }
        drawPath(sparkPath, color = Color(0xFFF97316))
    }
}

@Composable
private fun LoadConsumedCustomIcon() {
    Canvas(modifier = Modifier.size(28.dp)) {
        val w = size.width
        val h = size.height

        // Warm red/coral roof
        val roofPath = Path().apply {
            moveTo(w * 0.5f, h * 0.16f)
            lineTo(w * 0.88f, h * 0.50f)
            lineTo(w * 0.12f, h * 0.50f)
            close()
        }
        drawPath(roofPath, color = Color(0xFFEF4444))

        // Chimney
        drawRect(
            color = Color(0xFFDC2626),
            topLeft = Offset(w * 0.68f, h * 0.22f),
            size = Size(w * 0.10f, h * 0.18f)
        )

        // House body
        drawRoundRect(
            color = Color(0xFFF8FAFC),
            topLeft = Offset(w * 0.22f, h * 0.48f),
            size = Size(w * 0.56f, h * 0.42f),
            cornerRadius = CornerRadius(2f, 2f)
        )

        // Door (dark)
        drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(w * 0.42f, h * 0.62f),
            size = Size(w * 0.16f, h * 0.28f),
            cornerRadius = CornerRadius(2f, 2f)
        )
    }
}

@Composable
private fun ExportMeterCustomIcon() {
    Canvas(modifier = Modifier.size(28.dp)) {
        val w = size.width
        val h = size.height

        // Meter outer circle
        drawCircle(
            color = Color(0xFF1E293B),
            radius = h * 0.40f,
            center = Offset(w * 0.5f, h * 0.5f)
        )
        drawCircle(
            color = Color(0xFF64748B),
            radius = h * 0.40f,
            center = Offset(w * 0.5f, h * 0.5f),
            style = Stroke(width = 2f)
        )

        // Counter screen window
        drawRoundRect(
            color = Color(0xFF0F172A),
            topLeft = Offset(w * 0.28f, h * 0.32f),
            size = Size(w * 0.44f, h * 0.20f),
            cornerRadius = CornerRadius(2f, 2f)
        )
        // Digits line
        drawLine(
            color = Color(0xFF38BDF8),
            start = Offset(w * 0.34f, h * 0.42f),
            end = Offset(w * 0.66f, h * 0.42f),
            strokeWidth = 2f
        )

        // Green lightning in bottom half
        val sparkPath = Path().apply {
            moveTo(w * 0.54f, h * 0.56f)
            lineTo(w * 0.44f, h * 0.72f)
            lineTo(w * 0.54f, h * 0.72f)
            lineTo(w * 0.48f, h * 0.88f)
            lineTo(w * 0.62f, h * 0.68f)
            lineTo(w * 0.52f, h * 0.68f)
            close()
        }
        drawPath(sparkPath, color = Color(0xFF10B981))
    }
}
