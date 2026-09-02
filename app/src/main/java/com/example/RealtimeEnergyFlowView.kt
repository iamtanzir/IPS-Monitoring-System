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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.geometry.Size
import com.example.ui.theme.SolarCardElevated
import com.example.ui.theme.SolarOrange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Battery Chemistry Types with operational parameters, nominal voltages,
 * safe depth of discharge (DoD), and C-rate charging envelope.
 */
enum class BatteryChemistry(
    val code: String,
    val displayName: String,
    val shortName: String,
    val cellStructure: String,
    val nominalVoltage: String,
    val absorptionVoltage: Float,
    val floatVoltage: Float,
    val cutoffVoltage: Float,
    val maxSafeDoD: Int,
    val maxChargeCRate: String,
    val cRateLimit: Float,
    val tagColor: Color,
    val description: String,
    val maintenanceTip: String
) {
    LFP(
        code = "LFP",
        displayName = "LFP (LiFePO4 Lithium)",
        shortName = "LFP 4S",
        cellStructure = "4S 3.2V Cells",
        nominalVoltage = "12.8V",
        absorptionVoltage = 14.4f,
        floatVoltage = 13.6f,
        cutoffVoltage = 10.5f,
        maxSafeDoD = 90,
        maxChargeCRate = "0.50C (~50A)",
        cRateLimit = 0.50f,
        tagColor = Color(0xFF00E5FF),
        description = "Lithium Iron Phosphate • 3000+ Deep Cycles • 90% Safe DoD • Flat Discharge Curve",
        maintenanceTip = "BMS Active Balance active. High efficiency CC/CV charging. Avoid charging below 0°C freezing point."
    ),
    GEL(
        code = "GEL",
        displayName = "GEL Deep Cycle (VRLA)",
        shortName = "GEL VRLA",
        cellStructure = "6S 2.0V Cells",
        nominalVoltage = "12.0V",
        absorptionVoltage = 14.2f,
        floatVoltage = 13.5f,
        cutoffVoltage = 10.8f,
        maxSafeDoD = 60,
        maxChargeCRate = "0.15C (~15A)",
        cRateLimit = 0.15f,
        tagColor = Color(0xFF818CF8),
        description = "Sealed Gel Electrolyte • Maintenance-Free • Deep Discharge Recovery • Heat Resilient",
        maintenanceTip = "Sealed recombinant VRLA design. Zero electrolyte refilling. Do not equalize or exceed 14.3V charging voltage."
    ),
    ACID(
        code = "ACID",
        displayName = "Tall Tubular Lead-Acid",
        shortName = "Tubular Acid",
        cellStructure = "6S 2.0V Cells",
        nominalVoltage = "12.0V",
        absorptionVoltage = 14.6f,
        floatVoltage = 13.8f,
        cutoffVoltage = 10.8f,
        maxSafeDoD = 50,
        maxChargeCRate = "0.10C (~10A)",
        cRateLimit = 0.10f,
        tagColor = Color(0xFFF59E0B),
        description = "Heavy Duty Tubular Plate • Long Backup Life • High Motor Surge Tolerance",
        maintenanceTip = "Periodic distilled water inspection required every 90 days. Equalization boost cycle supported (14.6V)."
    );

    companion object {
        fun fromString(value: String): BatteryChemistry {
            return when {
                value.contains("LFP", ignoreCase = true) ||
                value.contains("Lithium", ignoreCase = true) ||
                value.contains("LiFePO4", ignoreCase = true) -> LFP

                value.contains("GEL", ignoreCase = true) -> GEL

                else -> ACID
            }
        }
    }
}

/**
 * Real-Time Energy Flow UI Component
 *
 * Displays live real-time energy flow with Ampere (A) values alongside Wattage (W),
 * clearly indicating whether Grid, Solar, or Battery source is active using dynamic
 * animated path indicators (Canvas-based) to show current direction (Grid/Solar/Battery to Load)
 * with real-time Ampere and Wattage text overlays, properly indicating battery type (LFP/GEL/Acid) context.
 */
@Composable
fun RealtimeEnergyFlowCard(
    metrics: SolarMetrics,
    accentColor: Color,
    onToggleSolar: () -> Unit,
    onToggleBattery: () -> Unit,
    onToggleGrid: () -> Unit,
    batteryType: String = "Tall Tubular Lead-Acid",
    onSelectBatteryType: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var activeChemistry by remember(batteryType) {
        mutableStateOf(BatteryChemistry.fromString(batteryType))
    }

    val isSolarActive = metrics.isSolarOn && metrics.panelPower > 0f
    val isBatteryCharging = metrics.batteryCurrent > 0.05f
    val isBatteryDischarging = metrics.batteryCurrent < -0.05f
    val isBatteryActive = isBatteryCharging || isBatteryDischarging
    val isGridActive = metrics.isGridOn && metrics.gridPower > 0f
    var flowStyle by remember { mutableStateOf(0) }
    var showBatteryDetailsDialog by remember { mutableStateOf(false) }

    // Animated infinite transitions for dynamic status icons & flow conduits
    val infiniteTransition = rememberInfiniteTransition(label = "energy_flow_transition")
    val flowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
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
                    if (isBatteryActive) activeChemistry.tagColor.copy(alpha = 0.6f) else SolarBorder
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
                isSolarActive && isBatteryCharging && metrics.loadPower > 0 -> "SOLAR -> LOAD + BATTERY CHARGE"
                isSolarActive && isBatteryCharging -> "SOLAR -> BATTERY CHARGING"
                isSolarActive && isBatteryDischarging -> "SOLAR + BATTERY -> LOAD DUAL SUPPLY"
                isSolarActive -> "SOLAR -> LOAD SUPPLYING"
                isGridActive && isBatteryCharging -> "GRID -> LOAD BYPASS + CHARGE"
                isGridActive -> "AC GRID -> LOAD BYPASS ACTIVE"
                isBatteryDischarging -> "BATTERY -> LOAD BACKUP RUNNING"
                else -> "SYSTEM IDLE / STANDBY"
            }

            val statusColor = when {
                isSolarActive -> SolarGold
                isGridActive -> SolarGreen
                isBatteryActive -> activeChemistry.tagColor
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
                            text = "Energy Flow Dynamics",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Canvas Animated Direction & Ampere/Watt Matrix",
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

            // Sub-status banner showing specific active routing & active battery context
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
                            text = "Route: ",
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

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = activeChemistry.tagColor.copy(alpha = 0.15f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(activeChemistry.tagColor.copy(alpha = 0.5f), activeChemistry.tagColor.copy(alpha = 0.2f)))
                        )
                    ) {
                        Text(
                            text = activeChemistry.shortName,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = activeChemistry.tagColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Style Selector for Power Flow Architecture (Image 1 Power Station Hub vs Image 2/3 Distribution Net)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D0D12), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF1E1E28), RoundedCornerShape(12.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (flowStyle == 0) SolarGreen.copy(alpha = 0.18f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (flowStyle == 0) SolarGreen.copy(alpha = 0.6f) else Color.Transparent,
                            RoundedCornerShape(9.dp)
                        )
                        .clickable { flowStyle = 0 }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.BatteryChargingFull,
                            contentDescription = null,
                            tint = if (flowStyle == 0) SolarGreen else TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Power Station Hub",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (flowStyle == 0) SolarGreen else TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (flowStyle == 1) SolarCyan.copy(alpha = 0.18f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (flowStyle == 1) SolarCyan.copy(alpha = 0.6f) else Color.Transparent,
                            RoundedCornerShape(9.dp)
                        )
                        .clickable { flowStyle = 1 }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Hub,
                            contentDescription = null,
                            tint = if (flowStyle == 1) SolarCyan else TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Distribution Net",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (flowStyle == 1) SolarCyan else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. High-Fidelity Animated Canvas-Based Energy Flow Schematic
            if (flowStyle == 0) {
                PowerStationHubSchematic(
                    metrics = metrics,
                    flowOffset = flowOffset,
                    isSolarActive = isSolarActive,
                    isGridActive = isGridActive,
                    isBatteryCharging = isBatteryCharging,
                    isBatteryDischarging = isBatteryDischarging,
                    batteryChemistry = activeChemistry,
                    accentColor = accentColor,
                    onToggleSolar = onToggleSolar,
                    onToggleBattery = onToggleBattery,
                    onToggleGrid = onToggleGrid,
                    onShowDetails = { showBatteryDetailsDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                EnergyFlowSchematic(
                    metrics = metrics,
                    flowOffset = flowOffset,
                    isSolarActive = isSolarActive,
                    isGridActive = isGridActive,
                    isBatteryCharging = isBatteryCharging,
                    isBatteryDischarging = isBatteryDischarging,
                    batteryChemistry = activeChemistry,
                    accentColor = accentColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                )
            }

            if (showBatteryDetailsDialog) {
                BatteryCellDetailsDialog(
                    metrics = metrics,
                    chemistry = activeChemistry,
                    onDismiss = { showBatteryDetailsDialog = false }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2B. Battery Chemistry Context (LFP / GEL / Acid) Component
            BatteryChemistryContextCard(
                activeChemistry = activeChemistry,
                batteryVoltage = metrics.batteryVoltage,
                batteryCurrent = metrics.batteryCurrent,
                batteryPct = metrics.batteryPct,
                batteryCapacityAh = metrics.batteryCapacityAh,
                onSelectChemistry = { chem ->
                    activeChemistry = chem
                    onSelectBatteryType(chem.displayName)
                }
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
                    isBatteryDischarging -> activeChemistry.tagColor
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
                    subMetric = "${metrics.batteryPct}% (${activeChemistry.shortName})",
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
 * Power Station Ring Hub Schematic (Image 1 - Anker / EcoFlow Style)
 *
 * Features:
 * - Central High-Precision Circular Battery Ring with remaining time & SOC%
 * - Four Orthogonal Conduits with 90° smooth rounded elbows
 * - Glowing neon traveling energy pulses
 * - Four Peripheral Nodes: PV/DC, AC Grid, DC/USB, AC Load
 * - Bottom tactile direct output toggle pills (DC 12V, AC Inverter, Solar MPPT)
 */
@Composable
private fun PowerStationHubSchematic(
    metrics: SolarMetrics,
    flowOffset: Float,
    isSolarActive: Boolean,
    isGridActive: Boolean,
    isBatteryCharging: Boolean,
    isBatteryDischarging: Boolean,
    batteryChemistry: BatteryChemistry,
    accentColor: Color,
    onToggleSolar: () -> Unit,
    onToggleBattery: () -> Unit,
    onToggleGrid: () -> Unit,
    onShowDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoadActive = metrics.isLoadOn && metrics.loadPower > 0f

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SolarBgDark),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    SolarBorder.copy(alpha = 0.8f),
                    SolarBorder.copy(alpha = 0.4f)
                )
            )
        ),
        modifier = modifier.testTag("power_station_hub_schematic")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Top device header bar matching Image 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF14141A),
                    border = BorderStroke(1.dp, SolarBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clickable { onShowDetails() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Device Info",
                            tint = SolarCyan,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(SolarCyan, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Bluetooth Online ᛒ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolarCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ESP32-C6 • Solar IPS Hub",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFF14141A),
                    border = BorderStroke(1.dp, SolarBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clickable { onShowDetails() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Central Canvas Area with Battery Ring & 4 Orthogonal Nodes
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val centerX = w / 2f
                    val centerY = h / 2f
                    val ringRadius = 78.dp.toPx()
                    val cornerR = 24.dp.toPx()
                    val baseLineColor = Color(0xFF1C1C26)
                    val baseStroke = 3.dp.toPx()

                    val pvAnchor = Offset(w * 0.17f, h * 0.16f)
                    val acInAnchor = Offset(w * 0.83f, h * 0.16f)
                    val dcAnchor = Offset(w * 0.17f, h * 0.84f)
                    val acOutAnchor = Offset(w * 0.83f, h * 0.84f)

                    // Ambient radial glow behind central battery
                    val hubGlowColor = when {
                        isBatteryCharging -> SolarGreen
                        isSolarActive -> SolarGold
                        isLoadActive -> SolarOrange
                        else -> SolarCyan
                    }
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(hubGlowColor.copy(alpha = 0.14f), Color.Transparent),
                            center = Offset(centerX, centerY),
                            radius = ringRadius * 1.6f
                        ),
                        radius = ringRadius * 1.6f,
                        center = Offset(centerX, centerY)
                    )

                    // 1. PV/DC Orthogonal Conduit (Top-Left -> Central Ring)
                    val pvPath = Path().apply {
                        moveTo(pvAnchor.x, pvAnchor.y + 24.dp.toPx())
                        lineTo(pvAnchor.x, (centerY - 22.dp.toPx()) - cornerR)
                        quadraticTo(pvAnchor.x, centerY - 22.dp.toPx(), pvAnchor.x + cornerR, centerY - 22.dp.toPx())
                        lineTo(centerX - ringRadius, centerY - 22.dp.toPx())
                    }
                    drawPath(pvPath, color = baseLineColor, style = Stroke(width = baseStroke, cap = StrokeCap.Round))
                    if (isSolarActive) {
                        drawPath(pvPath, color = SolarGreen.copy(alpha = 0.35f), style = Stroke(width = baseStroke + 4.dp.toPx(), cap = StrokeCap.Round))
                        drawPath(pvPath, color = SolarGreen, style = Stroke(width = baseStroke, cap = StrokeCap.Round))
                        drawAnimatedPulseAlongOrthogonalPath(
                            start = Offset(pvAnchor.x, pvAnchor.y + 24.dp.toPx()),
                            corner = Offset(pvAnchor.x, centerY - 22.dp.toPx()),
                            end = Offset(centerX - ringRadius, centerY - 22.dp.toPx()),
                            cornerR = cornerR,
                            fraction = (flowOffset / 100f),
                            color = SolarGreen
                        )
                    }

                    // 2. AC Input Orthogonal Conduit (Top-Right -> Central Ring)
                    val acInPath = Path().apply {
                        moveTo(acInAnchor.x, acInAnchor.y + 24.dp.toPx())
                        lineTo(acInAnchor.x, (centerY - 22.dp.toPx()) - cornerR)
                        quadraticTo(acInAnchor.x, centerY - 22.dp.toPx(), acInAnchor.x - cornerR, centerY - 22.dp.toPx())
                        lineTo(centerX + ringRadius, centerY - 22.dp.toPx())
                    }
                    drawPath(acInPath, color = baseLineColor, style = Stroke(width = baseStroke, cap = StrokeCap.Round))
                    if (isGridActive) {
                        drawPath(acInPath, color = SolarCyan.copy(alpha = 0.35f), style = Stroke(width = baseStroke + 4.dp.toPx(), cap = StrokeCap.Round))
                        drawPath(acInPath, color = SolarCyan, style = Stroke(width = baseStroke, cap = StrokeCap.Round))
                        drawAnimatedPulseAlongOrthogonalPath(
                            start = Offset(acInAnchor.x, acInAnchor.y + 24.dp.toPx()),
                            corner = Offset(acInAnchor.x, centerY - 22.dp.toPx()),
                            end = Offset(centerX + ringRadius, centerY - 22.dp.toPx()),
                            cornerR = cornerR,
                            fraction = (flowOffset / 100f),
                            color = SolarCyan
                        )
                    }

                    // 3. DC/USB Orthogonal Conduit (Central Ring -> Bottom-Left)
                    val dcPath = Path().apply {
                        moveTo(centerX - ringRadius, centerY + 22.dp.toPx())
                        lineTo(dcAnchor.x + cornerR, centerY + 22.dp.toPx())
                        quadraticTo(dcAnchor.x, centerY + 22.dp.toPx(), dcAnchor.x, centerY + 22.dp.toPx() + cornerR)
                        lineTo(dcAnchor.x, dcAnchor.y - 24.dp.toPx())
                    }
                    drawPath(dcPath, color = baseLineColor, style = Stroke(width = baseStroke, cap = StrokeCap.Round))
                    if (metrics.isSolarOn) {
                        drawPath(dcPath, color = SolarCyan.copy(alpha = 0.2f), style = Stroke(width = baseStroke + 4.dp.toPx(), cap = StrokeCap.Round))
                        drawPath(dcPath, color = SolarCyan.copy(alpha = 0.7f), style = Stroke(width = baseStroke, cap = StrokeCap.Round))
                    }

                    // 4. AC Output Orthogonal Conduit (Central Ring -> Bottom-Right)
                    val acOutPath = Path().apply {
                        moveTo(centerX + ringRadius, centerY + 22.dp.toPx())
                        lineTo(acOutAnchor.x - cornerR, centerY + 22.dp.toPx())
                        quadraticTo(acOutAnchor.x, centerY + 22.dp.toPx(), acOutAnchor.x, centerY + 22.dp.toPx() + cornerR)
                        lineTo(acOutAnchor.x, acOutAnchor.y - 24.dp.toPx())
                    }
                    drawPath(acOutPath, color = baseLineColor, style = Stroke(width = baseStroke, cap = StrokeCap.Round))
                    if (isLoadActive) {
                        drawPath(acOutPath, color = SolarOrange.copy(alpha = 0.35f), style = Stroke(width = baseStroke + 4.dp.toPx(), cap = StrokeCap.Round))
                        drawPath(acOutPath, color = SolarOrange, style = Stroke(width = baseStroke, cap = StrokeCap.Round))
                        drawAnimatedPulseAlongOrthogonalPath(
                            start = Offset(centerX + ringRadius, centerY + 22.dp.toPx()),
                            corner = Offset(acOutAnchor.x, centerY + 22.dp.toPx()),
                            end = Offset(acOutAnchor.x, acOutAnchor.y - 24.dp.toPx()),
                            cornerR = cornerR,
                            fraction = (flowOffset / 100f),
                            color = SolarOrange
                        )
                    }

                    // Central Ring Battery Gauge
                    drawCircle(
                        color = Color(0xFF101016),
                        radius = ringRadius,
                        center = Offset(centerX, centerY)
                    )
                    // Inactive Track
                    drawCircle(
                        color = Color(0xFF1E1E28),
                        radius = ringRadius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Active Progress Arc
                    val pct = (metrics.batteryPct / 100f).coerceIn(0.04f, 1f)
                    val arcSweep = 360f * pct
                    val arcColor = when {
                        isBatteryCharging -> SolarGreen
                        metrics.batteryPct > 35 -> SolarGreen
                        metrics.batteryPct > 20 -> SolarGold
                        else -> SolarRed
                    }
                    drawArc(
                        color = arcColor,
                        startAngle = -90f,
                        sweepAngle = arcSweep,
                        useCenter = false,
                        topLeft = Offset(centerX - ringRadius, centerY - ringRadius),
                        size = Size(ringRadius * 2, ringRadius * 2),
                        style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Center Ring Overlaid Details & Percentage
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isBatteryCharging) "⚡ 2h 15m to Full" else "⏱ 3h02min",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${metrics.batteryPct}%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "⚡",
                            fontSize = 20.sp,
                            color = SolarGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF16161E),
                        border = BorderStroke(1.dp, Color(0xFF262634)),
                        modifier = Modifier.clickable { onShowDetails() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Details", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(11.dp))
                        }
                    }
                }

                // Four Peripheral Node Cards (Image 1)
                // Top-Left (PV/DC)
                Box(modifier = Modifier.align(Alignment.TopStart).padding(start = 12.dp, top = 6.dp)) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(if (isSolarActive) SolarGreen else Color(0xFF181820), CircleShape)
                                .border(1.dp, if (isSolarActive) SolarGreen else Color(0xFF282836), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.WbSunny,
                                contentDescription = "PV/DC",
                                tint = if (isSolarActive) Color.Black else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("PV/DC", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Text(
                            "${metrics.panelPower.toInt()} W",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSolarActive) SolarGreen else TextPrimary
                        )
                        Text("${metrics.panelVoltage}V • ${String.format("%.1f", metrics.panelCurrent)}A", fontSize = 9.sp, color = TextSecondary)
                    }
                }

                // Top-Right (AC Input)
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(end = 12.dp, top = 6.dp)) {
                    Column(horizontalAlignment = Alignment.End) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(if (isGridActive) SolarCyan.copy(alpha = 0.2f) else Color(0xFF181820), CircleShape)
                                .border(1.dp, if (isGridActive) SolarCyan else Color(0xFF282836), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Power,
                                contentDescription = "AC Grid",
                                tint = if (isGridActive) SolarCyan else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("AC", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Text(
                            "${metrics.gridPower.toInt()} W",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isGridActive) SolarCyan else TextPrimary
                        )
                        Text("${metrics.gridVoltage}V • 50Hz", fontSize = 9.sp, color = TextSecondary)
                    }
                }

                // Bottom-Left (DC/USB)
                Box(modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp, bottom = 6.dp)) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color(0xFF181820), CircleShape)
                                .border(1.dp, Color(0xFF282836), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ElectricMeter,
                                contentDescription = "DC/USB",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("DC/USB", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Text(
                            "${if (metrics.isSolarOn) 12 else 0} W",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text("12V Regulated", fontSize = 9.sp, color = TextSecondary)
                    }
                }

                // Bottom-Right (AC Output)
                Box(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 6.dp)) {
                    Column(horizontalAlignment = Alignment.End) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(if (isLoadActive) SolarOrange else Color(0xFF181820), CircleShape)
                                .border(1.dp, if (isLoadActive) SolarOrange else Color(0xFF282836), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ElectricBolt,
                                contentDescription = "AC Load",
                                tint = if (isLoadActive) Color.Black else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("AC", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Text(
                            "${metrics.loadPower.toInt()} W",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isLoadActive) SolarOrange else TextPrimary
                        )
                        Text("${metrics.loadVoltage}V • Pure Sine", fontSize = 9.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Tactile Toggle Control Pills (matching Image 1)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pill 1: DC Output
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF121218),
                    border = BorderStroke(1.dp, Color(0xFF20202A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⎓", fontSize = 18.sp, fontWeight = FontWeight.Black, color = SolarCyan)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("DC OUTPUT", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("12V DC Regulated Socket", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                        Switch(
                            checked = metrics.isSolarOn,
                            onCheckedChange = { onToggleSolar() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = SolarCyan,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = Color(0xFF1E1E28)
                            )
                        )
                    }
                }

                // Pill 2: AC Output (Inverter Pure Sine Wave)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF121218),
                    border = BorderStroke(1.dp, if (metrics.isLoadOn) SolarOrange.copy(alpha = 0.5f) else Color(0xFF20202A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("∿", fontSize = 18.sp, fontWeight = FontWeight.Black, color = SolarOrange)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("AC OUTPUT", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(
                                    text = if (metrics.isLoadOn) "220V Pure Sine Active • ${metrics.loadPower.toInt()}W" else "Inverter Inactive / Standby",
                                    fontSize = 10.sp,
                                    color = if (metrics.isLoadOn) SolarOrange else TextSecondary
                                )
                            }
                        }
                        Switch(
                            checked = metrics.isLoadOn,
                            onCheckedChange = { onToggleGrid() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = SolarOrange,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = Color(0xFF1E1E28)
                            )
                        )
                    }
                }

                // Pill 3: Solar MPPT Intake
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF121218),
                    border = BorderStroke(1.dp, if (metrics.isSolarOn) SolarGreen.copy(alpha = 0.5f) else Color(0xFF20202A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("☀️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("SOLAR CHARGE", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(
                                    text = if (metrics.isSolarOn) "MPPT Active • ${metrics.panelPower.toInt()}W Intake" else "Solar Charging Standby",
                                    fontSize = 10.sp,
                                    color = if (metrics.isSolarOn) SolarGreen else TextSecondary
                                )
                            }
                        }
                        Switch(
                            checked = metrics.isSolarOn,
                            onCheckedChange = { onToggleSolar() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = SolarGreen,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = Color(0xFF1E1E28)
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Canvas drawing helper to animate a traveling luminous pulse along an orthogonal 90° pipe path
 */
private fun DrawScope.drawAnimatedPulseAlongOrthogonalPath(
    start: Offset,
    corner: Offset,
    end: Offset,
    cornerR: Float,
    fraction: Float,
    color: Color
) {
    val vertDist = kotlin.math.abs(corner.y - start.y)
    val horizDist = kotlin.math.abs(end.x - corner.x)
    val totalDist = vertDist + horizDist
    if (totalDist <= 0f) return

    val currentPos = totalDist * (fraction.coerceIn(0f, 1f))
    val pt: Offset = if (currentPos <= vertDist) {
        val f = currentPos / vertDist
        Offset(start.x, start.y + (corner.y - start.y) * f)
    } else {
        val f = (currentPos - vertDist) / horizDist
        Offset(corner.x + (end.x - corner.x) * f, end.y)
    }

    drawCircle(
        color = color.copy(alpha = 0.35f),
        radius = 7.dp.toPx(),
        center = pt
    )
    drawCircle(
        color = color.copy(alpha = 0.85f),
        radius = 4.dp.toPx(),
        center = pt
    )
    drawCircle(
        color = Color.White,
        radius = 2.dp.toPx(),
        center = pt
    )
}

/**
 * Battery Cell Diagnostics and Telemetry Modal Dialog
 */
@Composable
private fun BatteryCellDetailsDialog(
    metrics: SolarMetrics,
    chemistry: BatteryChemistry,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF121218),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = SolarGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Battery Pack Telemetry", fontSize = 16.sp, fontWeight = FontWeight.Black, color = TextPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ESP32-C6 High-Precision ADC Channel Diagnostics",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                HorizontalDivider(color = Color(0xFF22222C))

                TelemetryRowItem("State of Charge (SOC)", "${metrics.batteryPct}%", SolarGreen)
                TelemetryRowItem("Pack Voltage", "${metrics.batteryVoltage} V", TextPrimary)
                TelemetryRowItem("Pack Current", "${String.format("%.2f", metrics.batteryCurrent)} A", if (metrics.batteryCurrent > 0) SolarGreen else SolarOrange)
                TelemetryRowItem("Flow Direction", if (metrics.batteryCurrent > 0) "CHARGING" else if (metrics.batteryCurrent < 0) "DISCHARGING" else "IDLE", if (metrics.batteryCurrent > 0) SolarGreen else SolarGold)
                TelemetryRowItem("State of Health (SOH)", "98.8%", SolarCyan)
                TelemetryRowItem("Probe Temp (DS18B20)", "${metrics.batteryTemp}°C (GPIO 18)", TextPrimary)
                TelemetryRowItem("Chemistry", "${chemistry.displayName} (${chemistry.nominalVoltage}V)", chemistry.tagColor)
                TelemetryRowItem("Cutoff Voltage", "${chemistry.cutoffVoltage} V", TextSecondary)
                TelemetryRowItem("Configured Capacity", "${metrics.batteryCapacityAh} Ah", TextSecondary)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = SolarCyan, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun TelemetryRowItem(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 11.sp, color = TextSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

/**
 * High-Fidelity Canvas-Based Energy Flow Schematic
 *
 * Connects Solar PV, AC Grid, Battery, IPS Inverter Hub, and Home Load with
 * dynamic animated directional indicators (chevrons pointing in the real direction of electrical flow),
 * traveling glowing energy particles, junction glow nodes, and floating Ampere & Wattage overlays.
 */
@Composable
private fun EnergyFlowSchematic(
    metrics: SolarMetrics,
    flowOffset: Float,
    isSolarActive: Boolean,
    isGridActive: Boolean,
    isBatteryCharging: Boolean,
    isBatteryDischarging: Boolean,
    batteryChemistry: BatteryChemistry,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val isLoadOn = metrics.isLoadOn && metrics.loadPower > 0f

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SolarBgDark),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    SolarBorder.copy(alpha = 0.7f),
                    accentColor.copy(alpha = 0.25f),
                    SolarBorder.copy(alpha = 0.5f)
                )
            )
        ),
        modifier = modifier.testTag("energy_flow_schematic_canvas")
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                // Key Geometric Anchor Coordinates
                val solarAnchor = Offset(width * 0.16f, height * 0.18f)
                val gridAnchor = Offset(width * 0.16f, height * 0.50f)
                val battAnchor = Offset(width * 0.16f, height * 0.82f)
                val hubAnchor = Offset(width * 0.53f, height * 0.50f)
                val loadAnchor = Offset(width * 0.86f, height * 0.50f)

                // Conduit Endpoints
                val solarStart = Offset(solarAnchor.x + 36.dp.toPx(), solarAnchor.y)
                val solarEnd = Offset(hubAnchor.x - 30.dp.toPx(), hubAnchor.y - 14.dp.toPx())

                val gridStart = Offset(gridAnchor.x + 36.dp.toPx(), gridAnchor.y)
                val gridEnd = Offset(hubAnchor.x - 30.dp.toPx(), hubAnchor.y)

                val battStart = Offset(battAnchor.x + 36.dp.toPx(), battAnchor.y)
                val battEnd = Offset(hubAnchor.x - 30.dp.toPx(), hubAnchor.y + 14.dp.toPx())

                val loadStart = Offset(hubAnchor.x + 30.dp.toPx(), hubAnchor.y)
                val loadEnd = Offset(loadAnchor.x - 34.dp.toPx(), loadAnchor.y)
                // Ambient background technical glow at junction
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = hubAnchor,
                        radius = 80.dp.toPx()
                    ),
                    radius = 80.dp.toPx(),
                    center = hubAnchor
                )

                // 1. Solar Conduit (Solar -> Hub)
                drawConduitSegment(
                    start = solarStart,
                    end = solarEnd,
                    isActive = isSolarActive,
                    activeColor = SolarGold,
                    flowOffset = flowOffset,
                    directionForward = true, // Solar flows into Hub
                    curvature = 16.dp.toPx()
                )

                // 2. AC Grid Conduit (Grid -> Hub)
                drawConduitSegment(
                    start = gridStart,
                    end = gridEnd,
                    isActive = isGridActive,
                    activeColor = SolarGreen,
                    flowOffset = flowOffset,
                    directionForward = true, // Grid flows into Hub
                    curvature = 0f
                )

                // 3. Battery Conduit (Battery <-> Hub)
                val isBattActive = isBatteryCharging || isBatteryDischarging
                val battConduitColor = when {
                    isBatteryCharging -> SolarGreen
                    isBatteryDischarging -> batteryChemistry.tagColor
                    else -> Color.Gray
                }

                // If battery is charging: energy flows from Hub DOWN into Battery
                // If battery is discharging: energy flows from Battery UP into Hub
                val isFlowForwardToHub = isBatteryDischarging
                drawConduitSegment(
                    start = if (isFlowForwardToHub) battStart else battEnd,
                    end = if (isFlowForwardToHub) battEnd else battStart,
                    isActive = isBattActive,
                    activeColor = battConduitColor,
                    flowOffset = flowOffset,
                    directionForward = true,
                    curvature = if (isFlowForwardToHub) -16.dp.toPx() else 16.dp.toPx()
                )

                // 4. Output Load Conduit (Hub -> Load)
                drawConduitSegment(
                    start = loadStart,
                    end = loadEnd,
                    isActive = isLoadOn,
                    activeColor = SolarCyan,
                    flowOffset = flowOffset,
                    directionForward = true, // Inverter flows into Load
                    curvature = 0f
                )

                // Hub Junction Ring Glow
                if (isSolarActive || isGridActive || isBattActive || isLoadOn) {
                    drawCircle(
                        color = accentColor.copy(alpha = 0.20f),
                        radius = 34.dp.toPx(),
                        center = hubAnchor,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // Real-Time Ampere & Wattage Overlay Badges positioned directly over conduit paths
            // 1. Solar Path Overlay (Midpoint of Solar -> Hub)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 18.dp, end = 70.dp)
            ) {
                ConduitMetricPill(
                    icon = "☀️",
                    ampere = metrics.panelCurrent,
                    watt = metrics.panelPower,
                    color = SolarGold,
                    isActive = isSolarActive,
                    flowLabel = "PV IN"
                )
            }

            // 2. Grid Path Overlay (Midpoint of Grid -> Hub)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(end = 85.dp, bottom = 28.dp)
            ) {
                ConduitMetricPill(
                    icon = "🔌",
                    ampere = metrics.gridCurrent,
                    watt = metrics.gridPower,
                    color = SolarGreen,
                    isActive = isGridActive,
                    flowLabel = "GRID IN"
                )
            }

            // 3. Battery Path Overlay (Midpoint of Battery <-> Hub)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp, end = 70.dp)
            ) {
                val battLabel = when {
                    isBatteryCharging -> "↓ CHG"
                    isBatteryDischarging -> "↑ DIS"
                    else -> "IDLE"
                }
                val battColor = when {
                    isBatteryCharging -> SolarGreen
                    isBatteryDischarging -> batteryChemistry.tagColor
                    else -> Color.Gray
                }
                ConduitMetricPill(
                    icon = "🔋",
                    ampere = kotlin.math.abs(metrics.batteryCurrent),
                    watt = kotlin.math.abs(metrics.batteryPower),
                    color = battColor,
                    isActive = isBatteryCharging || isBatteryDischarging,
                    flowLabel = "$battLabel (${batteryChemistry.code})"
                )
            }

            // 4. Output Load Path Overlay (Midpoint of Hub -> Load)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(start = 145.dp, bottom = 28.dp)
            ) {
                ConduitMetricPill(
                    icon = "🏠",
                    ampere = metrics.loadCurrent,
                    watt = metrics.loadPower,
                    color = SolarCyan,
                    isActive = isLoadOn,
                    flowLabel = "LOAD OUT"
                )
            }

            // Structural Schematic Terminal Node Containers
            // 1. Solar Source Node (Top Left)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 6.dp, top = 6.dp)
            ) {
                SchematicTerminalNode(
                    title = "SOLAR PV",
                    icon = Icons.Default.WbSunny,
                    primaryValue = "${metrics.panelPower.toInt()} W",
                    secondaryValue = "${String.format("%.2f", metrics.panelCurrent)} A • ${metrics.panelVoltage}V",
                    accentColor = SolarGold,
                    isActive = isSolarActive
                )
            }

            // 2. AC Grid Source Node (Middle Left)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 6.dp)
            ) {
                SchematicTerminalNode(
                    title = "AC GRID",
                    icon = if (isGridActive) Icons.Default.Power else Icons.Default.PowerOff,
                    primaryValue = "${metrics.gridPower.toInt()} W",
                    secondaryValue = "${String.format("%.2f", metrics.gridCurrent)} A • ${metrics.gridVoltage}V",
                    accentColor = SolarGreen,
                    isActive = isGridActive
                )
            }

            // 3. Battery Terminal Node with Chemistry Context (Bottom Left)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 6.dp, bottom = 6.dp)
            ) {
                val battIcon = when {
                    isBatteryCharging -> Icons.Default.BatteryChargingFull
                    isBatteryDischarging -> Icons.Default.BatteryAlert
                    else -> Icons.Default.BatteryFull
                }
                val battColor = when {
                    isBatteryCharging -> SolarGreen
                    isBatteryDischarging -> batteryChemistry.tagColor
                    else -> SolarCyan
                }
                val flowSign = if (isBatteryCharging) "+" else if (isBatteryDischarging) "-" else ""

                SchematicTerminalNode(
                    title = "BATTERY (${batteryChemistry.code})",
                    icon = battIcon,
                    primaryValue = "${kotlin.math.abs(metrics.batteryPower).toInt()} W",
                    secondaryValue = "$flowSign${String.format("%.2f", kotlin.math.abs(metrics.batteryCurrent))} A • ${metrics.batteryVoltage}V (${metrics.batteryPct}%)",
                    accentColor = battColor,
                    isActive = isBatteryCharging || isBatteryDischarging
                )
            }

            // 4. Central Inverter Hub Node (Center)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SolarCardBg,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        listOf(
                            accentColor,
                            if (isSolarActive) SolarGold else if (isGridActive) SolarGreen else SolarCyan
                        )
                    )
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(start = 22.dp)
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
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isGridActive && !isBatteryDischarging) "Grid Bypass" else "DSP Inverter",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Text(
                        text = "94% Eff",
                        fontSize = 7.sp,
                        color = TextSecondary
                    )
                }
            }

            // 5. Output Appliance Load Node (Far Right)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp)
            ) {
                SchematicTerminalNode(
                    title = "HOME LOAD",
                    icon = Icons.Default.Home,
                    primaryValue = "${metrics.loadPower.toInt()} W",
                    secondaryValue = "${String.format("%.2f", metrics.loadCurrent)} A • ${metrics.loadVoltage}V",
                    accentColor = SolarCyan,
                    isActive = isLoadOn
                )
            }
        }
    }
}

/**
 * Canvas drawing helper for energy conduit segments with:
 * - Deep metallic pipeline shadow base
 * - Glowing colored aura when active
 * - Moving luminous energy pulses
 * - Vector directional chevron arrowheads pointing in the physical flow direction
 */
private fun DrawScope.drawConduitSegment(
    start: Offset,
    end: Offset,
    isActive: Boolean,
    activeColor: Color,
    flowOffset: Float,
    directionForward: Boolean,
    curvature: Float = 0f
) {
    val baseLineColor = Color(0xFF1E293B)
    val baseStroke = 3.dp.toPx()

    // Create Path (either quadratic bezier or straight line)
    val path = Path().apply {
        moveTo(start.x, start.y)
        if (curvature != 0f) {
            val controlX = (start.x + end.x) / 2f
            val controlY = (start.y + end.y) / 2f + curvature
            quadraticTo(controlX, controlY, end.x, end.y)
        } else {
            lineTo(end.x, end.y)
        }
    }

    // 1. Draw Inactive/Base Conduit Pipe
    drawPath(
        path = path,
        color = baseLineColor,
        style = Stroke(width = baseStroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    if (isActive) {
        // 2. Draw Active Glowing Neon Aura behind conduit
        drawPath(
            path = path,
            color = activeColor.copy(alpha = 0.20f),
            style = Stroke(width = baseStroke * 2.8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 3. Draw Core Neon Wire
        drawPath(
            path = path,
            color = activeColor.copy(alpha = 0.85f),
            style = Stroke(width = baseStroke * 1.1f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 4. Draw Animated Directional Chevrons & Luminous Particles along the path
        val steps = 3
        for (i in 0 until steps) {
            // Fraction along the path (0..1)
            val baseFraction = ((flowOffset + i * (100f / steps)) % 100f) / 100f
            val fraction = if (directionForward) baseFraction else (1f - baseFraction)

            // Compute parametric point on curve
            val pt: Offset
            val tangent: Offset

            if (curvature != 0f) {
                val controlX = (start.x + end.x) / 2f
                val controlY = (start.y + end.y) / 2f + curvature
                val t = fraction
                val u = 1f - t

                // Point: (1-t)^2 * P0 + 2(1-t)t * P1 + t^2 * P2
                val px = u * u * start.x + 2f * u * t * controlX + t * t * end.x
                val py = u * u * start.y + 2f * u * t * controlY + t * t * end.y
                pt = Offset(px, py)

                // Tangent: 2(1-t)(P1 - P0) + 2t(P2 - P1)
                val tx = 2f * u * (controlX - start.x) + 2f * t * (end.x - controlX)
                val ty = 2f * u * (controlY - start.y) + 2f * t * (end.y - controlY)
                val len = sqrt(tx * tx + ty * ty).coerceAtLeast(0.001f)
                val dirSign = if (directionForward) 1f else -1f
                tangent = Offset((tx / len) * dirSign, (ty / len) * dirSign)
            } else {
                val px = start.x + (end.x - start.x) * fraction
                val py = start.y + (end.y - start.y) * fraction
                pt = Offset(px, py)

                val dx = end.x - start.x
                val dy = end.y - start.y
                val len = sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)
                val dirSign = if (directionForward) 1f else -1f
                tangent = Offset((dx / len) * dirSign, (dy / len) * dirSign)
            }

            // Draw Luminous Pulse Particle
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = 2.5.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = activeColor.copy(alpha = 0.6f),
                radius = 4.5.dp.toPx(),
                center = pt
            )

            // Draw Directional Chevron Arrowhead pointing along current flow
            val arrowSize = 6.dp.toPx()
            val normal = Offset(-tangent.y, tangent.x)
            val wing1 = Offset(
                pt.x - tangent.x * arrowSize + normal.x * (arrowSize * 0.55f),
                pt.y - tangent.y * arrowSize + normal.y * (arrowSize * 0.55f)
            )
            val wing2 = Offset(
                pt.x - tangent.x * arrowSize - normal.x * (arrowSize * 0.55f),
                pt.y - tangent.y * arrowSize - normal.y * (arrowSize * 0.55f)
            )

            drawLine(
                color = activeColor,
                start = wing1,
                end = pt,
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = activeColor,
                start = wing2,
                end = pt,
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Floating overlay pill for live Ampere and Wattage text overlays directly over conduit paths
 */
@Composable
private fun ConduitMetricPill(
    icon: String,
    ampere: Float,
    watt: Float,
    color: Color,
    isActive: Boolean,
    flowLabel: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SolarBgDark.copy(alpha = 0.92f),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    if (isActive) color.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.2f),
                    if (isActive) color.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.1f)
                )
            )
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 8.sp)
            Spacer(modifier = Modifier.width(3.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${String.format("%.2f", ampere)}A",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isActive) color else TextSecondary
                    )
                    Text(
                        text = " • ",
                        fontSize = 7.sp,
                        color = TextSecondary.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${watt.toInt()}W",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isActive) TextPrimary else TextSecondary
                    )
                }
                Text(
                    text = flowLabel,
                    fontSize = 6.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) color.copy(alpha = 0.9f) else Color.Gray
                )
            }
        }
    }
}

/**
 * Clean Terminal Node Card used for Solar, Grid, Battery, and Load anchors in the schematic
 */
@Composable
private fun SchematicTerminalNode(
    title: String,
    icon: ImageVector,
    primaryValue: String,
    secondaryValue: String,
    accentColor: Color,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = SolarCardBg.copy(alpha = 0.95f),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    if (isActive) accentColor.copy(alpha = 0.7f) else SolarBorder,
                    if (isActive) accentColor.copy(alpha = 0.2f) else SolarBorder.copy(alpha = 0.3f)
                )
            )
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(if (isActive) accentColor.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.04f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isActive) accentColor else Color.Gray,
                    modifier = Modifier.size(13.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) accentColor else TextSecondary
                )
                Text(
                    text = primaryValue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isActive) TextPrimary else TextSecondary
                )
                Text(
                    text = secondaryValue,
                    fontSize = 7.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

/**
 * Dedicated Battery Chemistry Context Component
 *
 * Explicitly presents the LFP / GEL / Acid context with interactive presets,
 * nominal voltage configurations, safe depth of discharge (DoD), and live C-rate
 * charging dynamics against safe chemical thresholds.
 */
@Composable
private fun BatteryChemistryContextCard(
    activeChemistry: BatteryChemistry,
    batteryVoltage: Float,
    batteryCurrent: Float,
    batteryPct: Int,
    batteryCapacityAh: Int,
    onSelectChemistry: (BatteryChemistry) -> Unit,
    modifier: Modifier = Modifier
) {
    val capacity = batteryCapacityAh.coerceAtLeast(10)
    val absCurrent = kotlin.math.abs(batteryCurrent)
    val currentCRate = absCurrent / capacity.toFloat()
    val isCharging = batteryCurrent > 0.05f
    val isDischarging = batteryCurrent < -0.05f

    val isWithinSafeCRate = currentCRate <= activeChemistry.cRateLimit
    val cRateEvaluationColor = when {
        !isCharging && !isDischarging -> SolarCyan
        isWithinSafeCRate -> SolarGreen
        else -> SolarRed
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SolarBgDark,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(
                    activeChemistry.tagColor.copy(alpha = 0.5f),
                    SolarBorder.copy(alpha = 0.5f)
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("battery_chemistry_context_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: Battery Chemistry Context & Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(activeChemistry.tagColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = "Battery Chemistry",
                            tint = activeChemistry.tagColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Battery Chemistry Context",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Real-time C-Rate, nominal voltage & DoD profile",
                            fontSize = 8.sp,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = activeChemistry.tagColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = activeChemistry.cellStructure,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = activeChemistry.tagColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3 Selectable Chemistry Chips: LFP | GEL | Acid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BatteryChemistry.entries.forEach { chem ->
                    val isSelected = chem == activeChemistry
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) chem.tagColor.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.03f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    if (isSelected) chem.tagColor else SolarBorder.copy(alpha = 0.4f),
                                    if (isSelected) chem.tagColor.copy(alpha = 0.5f) else SolarBorder.copy(alpha = 0.2f)
                                )
                            )
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectChemistry(chem) }
                            .testTag("battery_type_tab_${chem.code.lowercase()}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = chem.code,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) chem.tagColor else TextSecondary
                            )
                            Text(
                                text = chem.shortName,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) TextPrimary else TextSecondary.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chemistry Operational Parameters Matrix
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChemistryMetricItem(
                    label = "Nominal",
                    value = activeChemistry.nominalVoltage,
                    accentColor = activeChemistry.tagColor,
                    modifier = Modifier.weight(1f)
                )
                ChemistryMetricItem(
                    label = "Max Safe DoD",
                    value = "${activeChemistry.maxSafeDoD}%",
                    accentColor = SolarCyan,
                    modifier = Modifier.weight(1f)
                )
                ChemistryMetricItem(
                    label = "Absorption",
                    value = "${activeChemistry.absorptionVoltage}V",
                    accentColor = SolarGold,
                    modifier = Modifier.weight(1f)
                )
                ChemistryMetricItem(
                    label = "Float Target",
                    value = "${activeChemistry.floatVoltage}V",
                    accentColor = SolarGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Real-Time C-Rate & Live Current Assessment Banner
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.03f),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(cRateEvaluationColor.copy(alpha = 0.4f), SolarBorder.copy(alpha = 0.3f))
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = cRateEvaluationColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Active C-Rate Dynamics: ",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Text(
                                text = "${String.format("%.3f", currentCRate)} C",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = cRateEvaluationColor
                            )
                        }
                        Text(
                            text = if (isCharging) "Charging at ${String.format("%.2f", absCurrent)}A (Limit: ${activeChemistry.maxChargeCRate})"
                                   else if (isDischarging) "Discharging at ${String.format("%.2f", absCurrent)}A from ${capacity}Ah pack"
                                   else "Battery Standby • Ready for backup",
                            fontSize = 7.5.sp,
                            color = TextSecondary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = cRateEvaluationColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isWithinSafeCRate) "OPTIMAL ZONE" else "HIGH CURRENT",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = cRateEvaluationColor,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Maintenance & Chemistry Insight Text
            Text(
                text = "💡 ${activeChemistry.maintenanceTip}",
                fontSize = 8.sp,
                color = TextSecondary,
                lineHeight = 11.sp
            )
        }
    }
}

@Composable
private fun ChemistryMetricItem(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color.White.copy(alpha = 0.03f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 7.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
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
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) activeColor.copy(alpha = 0.08f) else SolarBgDark
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(
                    if (isActive) activeColor.copy(alpha = 0.6f) else SolarBorder,
                    if (isActive) activeColor.copy(alpha = 0.2f) else SolarBorder.copy(alpha = 0.3f)
                )
            )
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with icon and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(if (isActive) activeColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = sourceName,
                        tint = if (isActive) activeColor else Color.Gray,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(iconRotation)
                            .scale(iconScale)
                    )
                }

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
