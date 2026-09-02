package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SolarBgDark
import com.example.ui.theme.SolarBorder
import com.example.ui.theme.SolarCardBg
import com.example.ui.theme.SolarCyan
import com.example.ui.theme.SolarGold
import com.example.ui.theme.SolarGreen
import com.example.ui.theme.SolarRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Real-Time Energy Flow UI Component
 *
 * Displays live real-time energy flow with Ampere (A) values alongside Wattage (W),
 * clearly indicating whether Grid, Solar, or Battery source is active using dynamic
 * animated status icons, live flow conduit animations, and source distribution breakdowns.
 */
@Composable
fun RealtimeEnergyFlowCard(
    metrics: SolarMetrics,
    accentColor: Color,
    onToggleSolar: () -> Unit,
    onToggleBattery: () -> Unit,
    onToggleGrid: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSolarActive = metrics.isSolarOn && metrics.panelPower > 0f
    val isBatteryCharging = metrics.batteryCurrent > 0.05f
    val isBatteryDischarging = metrics.batteryCurrent < -0.05f
    val isBatteryActive = isBatteryCharging || isBatteryDischarging
    val isGridActive = metrics.isGridOn && metrics.gridPower > 0f

    // Animated infinite transitions for dynamic status icons & flow conduits
    val infiniteTransition = rememberInfiniteTransition(label = "energy_flow_transition")
    val flowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flow_particles"
    )

    val sunRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sun_rotation"
    )

    val beaconPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beacon_pulse"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SolarCardBg),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    if (isSolarActive) SolarGold.copy(alpha = 0.6f) else SolarBorder,
                    if (isGridActive) SolarGreen.copy(alpha = 0.6f) else SolarBorder,
                    if (isBatteryActive) SolarCyan.copy(alpha = 0.6f) else SolarBorder
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("realtime_energy_flow_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val activeCount = listOf(isSolarActive, isGridActive, isBatteryActive).count { it }
            val statusText = when {
                isSolarActive && isBatteryCharging -> "SOLAR GENERATING & CHARGING"
                isSolarActive && isBatteryDischarging -> "SOLAR + BATTERY SUPPLY"
                isSolarActive -> "SOLAR POWERED"
                isGridActive && isBatteryCharging -> "GRID BYPASS + CHARGING"
                isGridActive -> "GRID BYPASS ACTIVE"
                isBatteryDischarging -> "BATTERY BACKUP RUNNING"
                else -> "SYSTEM IDLE / STANDBY"
            }

            val statusColor = when {
                isSolarActive -> SolarGold
                isGridActive -> SolarGreen
                isBatteryActive -> SolarCyan
                else -> Color.Gray
            }

            // 1. Header with dynamic active synthesis pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(accentColor.copy(alpha = 0.15f), CircleShape)
                            .border(1.dp, accentColor.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Energy Flow",
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Real-Time Energy Flow",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Live Amperes (A) & Wattage (W) Matrix",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(alpha = 0.12f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(statusColor.copy(alpha = 0.8f), statusColor.copy(alpha = 0.4f)))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(statusColor.copy(alpha = beaconPulse), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (activeCount > 1) "$activeCount SOURCES ACTIVE" else if (activeCount == 1) "1 SOURCE ACTIVE" else "STANDBY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-status banner showing specific active routing
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SolarBgDark,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(SolarBorder, SolarBorder.copy(alpha = 0.3f)))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = SolarGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Active Route: ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Text(
                            text = statusText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusColor
                        )
                    }

                    Text(
                        text = "${String.format("%.1f", metrics.loadPower)}W Total Load",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SolarCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Animated Real-Time Energy Flow Pipeline (Schematic Canvas)
            EnergyFlowSchematic(
                metrics = metrics,
                flowOffset = flowOffset,
                isSolarActive = isSolarActive,
                isGridActive = isGridActive,
                isBatteryCharging = isBatteryCharging,
                isBatteryDischarging = isBatteryDischarging,
                accentColor = accentColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Grid, Solar, Battery Source Cards with Amperes (A) & Wattage (W)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // SOLAR SOURCE CARD
                SourceChannelCard(
                    sourceName = "SOLAR PV",
                    wattage = metrics.panelPower,
                    ampere = metrics.panelCurrent,
                    voltage = metrics.panelVoltage,
                    isActive = isSolarActive,
                    activeColor = SolarGold,
                    statusText = if (isSolarActive) "ACTIVE" else "OFFLINE",
                    subMetric = "${metrics.panelOutputPct}% Eff",
                    icon = Icons.Default.WbSunny,
                    iconRotation = if (isSolarActive) sunRotation else 0f,
                    iconScale = if (isSolarActive) 1.05f else 0.9f,
                    pulseAlpha = if (isSolarActive) beaconPulse else 0.4f,
                    modifier = Modifier.weight(1f)
                )

                // AC GRID SOURCE CARD
                SourceChannelCard(
                    sourceName = "AC GRID",
                    wattage = metrics.gridPower,
                    ampere = metrics.gridCurrent,
                    voltage = metrics.gridVoltage,
                    isActive = isGridActive,
                    activeColor = SolarGreen,
                    statusText = if (isGridActive) "SUPPLYING" else "OUTAGE",
                    subMetric = "50 Hz AC",
                    icon = if (isGridActive) Icons.Default.Power else Icons.Default.PowerOff,
                    iconRotation = 0f,
                    iconScale = 1f,
                    pulseAlpha = if (isGridActive) beaconPulse else 0.4f,
                    modifier = Modifier.weight(1f)
                )

                // BATTERY SOURCE CARD
                val battIcon = when {
                    isBatteryCharging -> Icons.Default.BatteryChargingFull
                    isBatteryDischarging -> Icons.Default.BatteryAlert
                    else -> Icons.Default.BatteryFull
                }
                val battColor = when {
                    isBatteryCharging -> SolarGreen
                    isBatteryDischarging -> SolarGold
                    else -> SolarCyan
                }
                val battStatus = when {
                    isBatteryCharging -> "CHARGING"
                    isBatteryDischarging -> "DISCHARGING"
                    else -> "STANDBY"
                }

                SourceChannelCard(
                    sourceName = "BATTERY",
                    wattage = kotlin.math.abs(metrics.batteryPower),
                    ampere = metrics.batteryCurrent,
                    voltage = metrics.batteryVoltage,
                    isActive = isBatteryActive,
                    activeColor = battColor,
                    statusText = battStatus,
                    subMetric = "${metrics.batteryPct}% (${metrics.batteryCapacityAh}Ah)",
                    icon = battIcon,
                    iconRotation = 0f,
                    iconScale = 1f,
                    pulseAlpha = if (isBatteryActive) beaconPulse else 0.4f,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Central Load Consumption & Source Contribution Breakdown Bar
            LoadDistributionCard(
                metrics = metrics,
                isSolarActive = isSolarActive,
                isGridActive = isGridActive,
                isBatteryDischarging = isBatteryDischarging
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Interactive Tactical Source Toggles (Allow test driving flow)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Toggles:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )

                QuickSourceToggleChip(
                    label = "Solar",
                    isOn = metrics.isSolarOn,
                    activeColor = SolarGold,
                    onToggle = onToggleSolar,
                    modifier = Modifier.weight(1f)
                )

                QuickSourceToggleChip(
                    label = "AC Grid",
                    isOn = metrics.isGridOn,
                    activeColor = SolarGreen,
                    onToggle = onToggleGrid,
                    modifier = Modifier.weight(1f)
                )

                QuickSourceToggleChip(
                    label = "Charging",
                    isOn = metrics.isChargingOn,
                    activeColor = SolarCyan,
                    onToggle = onToggleBattery,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Source Channel Tile showing both Ampere (A) & Wattage (W) with dynamic status icon
 */
@Composable
private fun SourceChannelCard(
    sourceName: String,
    wattage: Float,
    ampere: Float,
    voltage: Float,
    isActive: Boolean,
    activeColor: Color,
    statusText: String,
    subMetric: String,
    icon: ImageVector,
    iconRotation: Float = 0f,
    iconScale: Float = 1f,
    pulseAlpha: Float = 1f,
    modifier: Modifier = Modifier
) {
    val containerBg = if (isActive) activeColor.copy(alpha = 0.08f) else SolarBgDark.copy(alpha = 0.5f)
    val borderColor = if (isActive) activeColor.copy(alpha = 0.45f) else SolarBorder

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(listOf(borderColor, borderColor.copy(alpha = 0.2f)))
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top: Dynamic Icon with Glow
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(
                        color = if (isActive) activeColor.copy(alpha = 0.18f * pulseAlpha) else Color.Gray.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = if (isActive) activeColor.copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = sourceName,
                    tint = if (isActive) activeColor else Color.Gray,
                    modifier = Modifier
                        .size(18.dp)
                        .scale(iconScale)
                        .rotate(iconRotation)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Source Label & Active Status Badge
            Text(
                text = sourceName,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextSecondary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(
                            color = if (isActive) activeColor.copy(alpha = pulseAlpha) else SolarRed,
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = statusText,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isActive) activeColor else Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // PRIMARY METRIC: WATTAGE (W)
            Text(
                text = "${wattage.toInt()} W",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = if (isActive) TextPrimary else TextSecondary
            )

            // CRITICAL USER REQUEST METRIC: AMPERE (A)
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isActive) activeColor.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.08f),
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Text(
                    text = "${if (ampere > 0 && sourceName == "BATTERY") "+" else ""}${String.format("%.2f", ampere)} A",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isActive) activeColor else Color.Gray,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Secondary: Voltage & info
            Text(
                text = "${String.format("%.1f", voltage)}V • $subMetric",
                fontSize = 8.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Animated schematic drawing showing conduits from Solar, Grid, and Battery
 * into the Central Inverter Hub and then into the Home Appliance Load.
 */
@Composable
private fun EnergyFlowSchematic(
    metrics: SolarMetrics,
    flowOffset: Float,
    isSolarActive: Boolean,
    isGridActive: Boolean,
    isBatteryCharging: Boolean,
    isBatteryDischarging: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SolarBgDark),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(SolarBorder, SolarBorder.copy(alpha = 0.4f)))
        ),
        modifier = modifier
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            val width = constraints.maxWidth.toFloat()
            val height = constraints.maxHeight.toFloat()

            // Coordinates
            val solarX = width * 0.15f
            val solarY = height * 0.25f

            val gridX = width * 0.15f
            val gridY = height * 0.50f

            val batteryX = width * 0.15f
            val batteryY = height * 0.78f

            val hubX = width * 0.55f
            val hubY = height * 0.50f

            val loadX = width * 0.88f
            val loadY = height * 0.50f

            Canvas(modifier = Modifier.fillMaxSize()) {
                val lineStroke = 2.5.dp.toPx()

                // 1. Solar Conduit (Solar -> Hub)
                val solarLineColor = if (isSolarActive) SolarGold.copy(alpha = 0.35f) else Color.Gray.copy(alpha = 0.15f)
                drawLine(
                    color = solarLineColor,
                    start = Offset(solarX + 20.dp.toPx(), solarY),
                    end = Offset(hubX - 25.dp.toPx(), hubY - 12.dp.toPx()),
                    strokeWidth = lineStroke
                )

                // 2. Grid Conduit (Grid -> Hub)
                val gridLineColor = if (isGridActive) SolarGreen.copy(alpha = 0.35f) else Color.Gray.copy(alpha = 0.15f)
                drawLine(
                    color = gridLineColor,
                    start = Offset(gridX + 20.dp.toPx(), gridY),
                    end = Offset(hubX - 25.dp.toPx(), hubY),
                    strokeWidth = lineStroke
                )

                // 3. Battery Conduit (Battery <-> Hub)
                val battLineColor = if (isBatteryCharging || isBatteryDischarging) SolarCyan.copy(alpha = 0.35f) else Color.Gray.copy(alpha = 0.15f)
                drawLine(
                    color = battLineColor,
                    start = Offset(batteryX + 20.dp.toPx(), batteryY),
                    end = Offset(hubX - 25.dp.toPx(), hubY + 12.dp.toPx()),
                    strokeWidth = lineStroke
                )

                // 4. Output Conduit (Hub -> Load)
                val isLoadOn = metrics.isLoadOn && metrics.loadPower > 0
                val loadLineColor = if (isLoadOn) SolarCyan.copy(alpha = 0.45f) else Color.Gray.copy(alpha = 0.15f)
                drawLine(
                    color = loadLineColor,
                    start = Offset(hubX + 25.dp.toPx(), hubY),
                    end = Offset(loadX - 22.dp.toPx(), loadY),
                    strokeWidth = lineStroke
                )

                // Draw Animated Energy Flow Particles (Dots)
                val particleRadius = 3.5.dp.toPx()

                // Solar Flow Particles (Moving Solar -> Hub)
                if (isSolarActive) {
                    val pStart = Offset(solarX + 20.dp.toPx(), solarY)
                    val pEnd = Offset(hubX - 25.dp.toPx(), hubY - 12.dp.toPx())
                    for (i in 0 until 3) {
                        val fraction = ((flowOffset + i * 33.3f) % 100f) / 100f
                        val posX = pStart.x + (pEnd.x - pStart.x) * fraction
                        val posY = pStart.y + (pEnd.y - pStart.y) * fraction
                        drawCircle(
                            color = SolarGold,
                            radius = particleRadius,
                            center = Offset(posX, posY)
                        )
                    }
                }

                // Grid Flow Particles (Moving Grid -> Hub)
                if (isGridActive) {
                    val pStart = Offset(gridX + 20.dp.toPx(), gridY)
                    val pEnd = Offset(hubX - 25.dp.toPx(), hubY)
                    for (i in 0 until 3) {
                        val fraction = ((flowOffset + i * 33.3f) % 100f) / 100f
                        val posX = pStart.x + (pEnd.x - pStart.x) * fraction
                        val posY = pStart.y + (pEnd.y - pStart.y) * fraction
                        drawCircle(
                            color = SolarGreen,
                            radius = particleRadius,
                            center = Offset(posX, posY)
                        )
                    }
                }

                // Battery Flow Particles
                if (isBatteryCharging) {
                    // Moving Hub -> Battery (Charging)
                    val pStart = Offset(hubX - 25.dp.toPx(), hubY + 12.dp.toPx())
                    val pEnd = Offset(batteryX + 20.dp.toPx(), batteryY)
                    for (i in 0 until 3) {
                        val fraction = ((flowOffset + i * 33.3f) % 100f) / 100f
                        val posX = pStart.x + (pEnd.x - pStart.x) * fraction
                        val posY = pStart.y + (pEnd.y - pStart.y) * fraction
                        drawCircle(
                            color = SolarGreen,
                            radius = particleRadius,
                            center = Offset(posX, posY)
                        )
                    }
                } else if (isBatteryDischarging) {
                    // Moving Battery -> Hub (Discharging)
                    val pStart = Offset(batteryX + 20.dp.toPx(), batteryY)
                    val pEnd = Offset(hubX - 25.dp.toPx(), hubY + 12.dp.toPx())
                    for (i in 0 until 3) {
                        val fraction = ((flowOffset + i * 33.3f) % 100f) / 100f
                        val posX = pStart.x + (pEnd.x - pStart.x) * fraction
                        val posY = pStart.y + (pEnd.y - pStart.y) * fraction
                        drawCircle(
                            color = SolarGold,
                            radius = particleRadius,
                            center = Offset(posX, posY)
                        )
                    }
                }

                // Load Output Particles (Hub -> Load)
                if (isLoadOn) {
                    val pStart = Offset(hubX + 25.dp.toPx(), hubY)
                    val pEnd = Offset(loadX - 22.dp.toPx(), loadY)
                    for (i in 0 until 4) {
                        val fraction = ((flowOffset + i * 25f) % 100f) / 100f
                        val posX = pStart.x + (pEnd.x - pStart.x) * fraction
                        val posY = pStart.y + (pEnd.y - pStart.y) * fraction
                        drawCircle(
                            color = SolarCyan,
                            radius = particleRadius,
                            center = Offset(posX, posY)
                        )
                    }
                }
            }

            // Interactive Node Badges on top of Canvas
            // 1. Solar Input Label
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 4.dp, start = 4.dp)
            ) {
                SchematicNodeBadge(
                    name = "☀️ SOLAR",
                    metric = "${metrics.panelPower.toInt()}W • ${metrics.panelCurrent}A",
                    color = if (isSolarActive) SolarGold else Color.Gray
                )
            }

            // 2. Grid Input Label
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
            ) {
                SchematicNodeBadge(
                    name = "🔌 GRID",
                    metric = "${metrics.gridPower.toInt()}W • ${String.format("%.2f", metrics.gridCurrent)}A",
                    color = if (isGridActive) SolarGreen else Color.Gray
                )
            }

            // 3. Battery Input Label
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 4.dp, start = 4.dp)
            ) {
                SchematicNodeBadge(
                    name = "🔋 BATTERY",
                    metric = "${kotlin.math.abs(metrics.batteryPower).toInt()}W • ${metrics.batteryCurrent}A",
                    color = if (isBatteryCharging || isBatteryDischarging) SolarCyan else Color.Gray
                )
            }

            // Central Inverter Hub Node
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SolarCardBg,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(accentColor, SolarCyan))
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(start = 20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Inverter Hub",
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "IPS HUB",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "DSP Inverter",
                        fontSize = 7.sp,
                        color = TextSecondary
                    )
                }
            }

            // 4. Output Load Node
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SolarCardBg,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(SolarCyan, SolarCyan.copy(alpha = 0.3f)))
                ),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Load Appliances",
                        tint = SolarCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${metrics.loadPower.toInt()} W",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = "${metrics.loadCurrent} A Load",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolarCyan
                    )
                }
            }
        }
    }
}

@Composable
private fun SchematicNodeBadge(
    name: String,
    metric: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(color.copy(alpha = 0.5f), color.copy(alpha = 0.2f)))
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)) {
            Text(text = name, fontSize = 7.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = metric, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}

/**
 * Load Output Distribution Card displaying source contributions in Watts and Amps
 */
@Composable
private fun LoadDistributionCard(
    metrics: SolarMetrics,
    isSolarActive: Boolean,
    isGridActive: Boolean,
    isBatteryDischarging: Boolean
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SolarBgDark,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(SolarBorder, SolarBorder.copy(alpha = 0.3f)))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ElectricMeter,
                        contentDescription = null,
                        tint = SolarCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Total Active Load Demand",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${String.format("%.1f", metrics.loadPower)} W",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = SolarCyan
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SolarCyan.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${String.format("%.2f", metrics.loadCurrent)} A",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SolarCyan,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Source Share Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Solar Contribution
                SharePill(
                    label = "Solar Share",
                    watt = metrics.loadPanelShareW,
                    ampere = if (metrics.panelVoltage > 0) metrics.loadPanelShareW / metrics.panelVoltage else 0f,
                    color = SolarGold,
                    isActive = isSolarActive,
                    modifier = Modifier.weight(1f)
                )

                // Battery Contribution
                SharePill(
                    label = "Battery Share",
                    watt = metrics.loadBatteryShareW,
                    ampere = if (metrics.batteryVoltage > 0) metrics.loadBatteryShareW / metrics.batteryVoltage else 0f,
                    color = SolarCyan,
                    isActive = isBatteryDischarging,
                    modifier = Modifier.weight(1f)
                )

                // Grid Contribution
                SharePill(
                    label = "Grid Share",
                    watt = metrics.loadGridShareW,
                    ampere = if (metrics.gridVoltage > 0) metrics.loadGridShareW / metrics.gridVoltage else 0f,
                    color = SolarGreen,
                    isActive = isGridActive && metrics.loadGridShareW > 0,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SharePill(
    label: String,
    watt: Float,
    ampere: Float,
    color: Color,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) color.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.03f),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    if (isActive) color.copy(alpha = 0.4f) else SolarBorder.copy(alpha = 0.5f),
                    if (isActive) color.copy(alpha = 0.1f) else SolarBorder.copy(alpha = 0.2f)
                )
            )
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) color else TextSecondary
            )
            Text(
                text = "${watt.toInt()} W",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isActive) TextPrimary else TextSecondary.copy(alpha = 0.6f)
            )
            Text(
                text = "${String.format("%.2f", ampere)} A",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) color else TextSecondary.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Tactical quick-toggle button to test-drive energy channels
 */
@Composable
private fun QuickSourceToggleChip(
    label: String,
    isOn: Boolean,
    activeColor: Color,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onToggle,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isOn) activeColor.copy(alpha = 0.15f) else Color.Transparent
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    if (isOn) activeColor else SolarBorder,
                    if (isOn) activeColor.copy(alpha = 0.5f) else SolarBorder
                )
            )
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(28.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(
                    color = if (isOn) activeColor else Color.Gray,
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label ${if (isOn) "ON" else "OFF"}",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isOn) activeColor else TextSecondary
        )
    }
}
