package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SolarHomeView(
    viewModel: SolarViewModel,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.metrics.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val accentColor = viewModel.getAccentColor()
    val notifications by viewModel.notifications.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
    ) {
        // 0. Live Material3 Notification Alerts Banner
        if (notifications.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    notifications.forEach { notification ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SolarRed.copy(alpha = 0.12f)),
                            border = BorderStroke(1.dp, SolarRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Alert",
                                    tint = SolarRed,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = notification.title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SolarRed
                                    )
                                    Text(
                                        text = notification.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Triggered: ${notification.timestamp}",
                                        fontSize = 9.sp,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(
                                    onClick = { viewModel.dismissNotification(notification.id) }
                                ) {
                                    Text("DISMISS", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // App Title Banner
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(listOf(SolarBorder, SolarHeroBg)),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    SolarHeroBg,
                                    SolarBgDark
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WbSunny,
                                        contentDescription = "Solar",
                                        tint = SolarGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Universal IPS, Inverter & Solar Monitoring",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = settings.customSolarName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SolarHeroText
                                )
                            }
                            // Visual Connection Status Pill
                            val isMqttOnline = settings.isMqttConnected && viewModel.isPaired.collectAsState().value
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isMqttOnline) Color(0xFF2E7D32).copy(alpha = 0.12f) else SolarRed.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isMqttOnline) Color(0xFF2E7D32) else SolarRed,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val infiniteTransition = rememberInfiniteTransition(label = "beacon")
                                    val beaconAlpha by infiniteTransition.animateFloat(
                                        initialValue = 0.4f,
                                        targetValue = 1.0f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(800, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                        ),
                                        label = "beacon"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(
                                                color = if (isMqttOnline) Color(0xFF2E7D32).copy(alpha = beaconAlpha) else SolarRed.copy(alpha = beaconAlpha),
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isMqttOnline) "ESP32 ONLINE" else "ESP32 OFFLINE",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isMqttOnline) Color(0xFF2E7D32) else SolarRed
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dynamic Row of badges representing system metadata
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                    .border(1.dp, SolarBorder, RoundedCornerShape(12.dp))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("DAILY YIELD", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                    Text("3.8 kWh", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1.5f)
                                    .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                    .border(1.dp, SolarBorder, RoundedCornerShape(12.dp))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("WEATHER", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                    Text("☀️ 32°C Sunny", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                    .border(1.dp, SolarBorder, RoundedCornerShape(12.dp))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("STATION EFF.", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                    Text("94.2%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        LivePowerBalanceDialGauge(metrics = metrics)
                        Spacer(modifier = Modifier.height(14.dp))

                        // MQTT and Connection Pulse Status Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulseAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.4f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulse"
                            )
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        color = if (metrics.isSolarOn) Color(0xFF2E7D32).copy(alpha = pulseAlpha) else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .border(1.dp, Color.White, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (metrics.isSolarOn) "MQTT: HiveMQ Cloud Connected" else "MQTT: Offline / No Signal",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = SolarHeroText.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "Updated: Just Now",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Customer Unit Binding & Manufacturer Support Card for DSP/IPS Global Monitoring
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SolarCardBg),
                border = CardBorder(SolarBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudQueue,
                                contentDescription = "Global Connection",
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "IPS Device IoT Gateway",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "GLOBAL LIVE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = accentColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Serial Number / Model Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Device Serial ID",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = settings.deviceSerialNumber,
                                fontSize = 14.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "System Capacity",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${settings.ipsOutputRatingVa} VA Rating",
                                fontSize = 14.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = SolarBorder, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Manufacturer Details & Quick Helpline Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Built & Installed By",
                                    fontSize = 9.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = settings.installerName,
                                fontSize = 12.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Support Phone helpline trigger
                        Button(
                            onClick = { /* Simulated helpline trigger */ },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Call support",
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Helpline",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        // Live Interactive Flow Diagram
        if (settings.isFlowDiagramVisible) {
            item {
                Text(
                    text = "System Power Flow Model",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                LiveFlowDiagramCard(metrics = metrics, accentColor = accentColor)
            }
        }

        // Controls (Direct Control Actions)
        item {
            Text(
                text = "Tactical Appliance Outputs",
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondary,
                modifier = Modifier.padding(start = 4.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SolarBgDark),
                border = CardBorder(SolarBorder),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ControlButton(
                        title = "Solar Power Input",
                        subtitle = "PV DC Input",
                        watts = if (metrics.isSolarOn) "${metrics.panelPower.toInt()} W" else "0 W",
                        isOn = metrics.isSolarOn,
                        icon = Icons.Default.WbSunny,
                        activeColor = SolarGold,
                        onClick = { viewModel.toggleSolarPower() },
                        testTag = "solar_power_toggle"
                    )
                    ControlButton(
                        title = "Battery Charger",
                        subtitle = "BMS Bypass",
                        watts = if (metrics.isChargingOn && metrics.isSolarOn) "${Math.abs(metrics.batteryPower).toInt()} W" else "0 W",
                        isOn = metrics.isChargingOn,
                        icon = Icons.Default.BatteryChargingFull,
                        activeColor = SolarGreen,
                        onClick = { viewModel.toggleBatteryCharging() },
                        testTag = "battery_charging_toggle"
                    )
                    ControlButton(
                        title = "AC Inverter Load",
                        subtitle = "Output AC Relay",
                        watts = if (metrics.isLoadOn && !metrics.isAutoCutTripped) "${metrics.loadPower.toInt()} W" else "0 W",
                        isOn = metrics.isLoadOn && !metrics.isAutoCutTripped,
                        icon = Icons.Default.FlashOn,
                        activeColor = SolarRed,
                        onClick = { viewModel.toggleLoad() },
                        testTag = "load_outlet_toggle"
                    )
                }
            }
        }

        // Integrated Intelligent Safety Protection & Auto-Cut Card
        item {
            ProtectionPanelCard(
                metrics = metrics,
                viewModel = viewModel,
                accentColor = accentColor
            )
        }

        // Solar Panel Details Card
        item {
            SolarDetailsCard(metrics = metrics, accentColor = accentColor)
        }

        // Battery Status & Cells Card
        item {
            BatteryDetailsCard(
                metrics = metrics,
                accentColor = accentColor,
                isCellVisible = settings.isCellVoltagesVisible,
                lowBatteryAlert = settings.lowBatteryAlertLimit
            )
        }

        // Load Status Details Card
        item {
            LoadDetailsCard(metrics = metrics, accentColor = accentColor)
        }

        // ESP32 Hardware Diagnostics Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SolarCardBg),
                border = CardBorder(SolarBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Dns, contentDescription = "Diagnostics", tint = SolarCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ESP32 Hardware Diagnostics",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(SolarCyan.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .border(1.dp, SolarCyan.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "v1.4.2-stable",
                                color = SolarCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = SolarBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("LAST SUCCESSFUL SYNC", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Text(lastSyncTime, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = SolarGreen)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("WIFI SIGNAL STRENGTH", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Text("-58 dBm (Excellent)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("MQTT SUBSCRIPTION TOPIC", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Text("${settings.mqttTopicPrefix}/metrics", fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("BROKER PORT", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Text("${settings.mqttPort} (Standard TCP)", fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveFlowDiagramCard(
    metrics: SolarMetrics,
    accentColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SolarCardBg),
        border = CardBorder(SolarBorder),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
    ) {
        val transition = rememberInfiniteTransition(label = "energy_flow")
        val flowOffset by transition.animateFloat(
            initialValue = 0f,
            targetValue = 100f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "offset"
        )

        // Wave animation offset for battery fluid container
        val wavePhase by transition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = LinearEasing)
            ),
            label = "wave_phase"
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            val w = constraints.maxWidth.toFloat()
            val h = constraints.maxHeight.toFloat()

            // Draw Background flow pipes & Conduits using custom Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val nodeWidth = 105.dp.toPx()
                val nodeHeight = 72.dp.toPx()

                // Center coordinates of each block
                val pvX = nodeWidth / 2f
                val pvY = nodeHeight / 2f

                val gridX = w - nodeWidth / 2f
                val gridY = pvY

                val centralInverterX = w / 2f
                val centralInverterY = h / 2f

                val batteryX = pvX
                val batteryY = h - nodeHeight / 2f

                val loadX = gridX
                val loadY = batteryY

                // Static / Flow line styles
                val lineStroke = 3.dp.toPx()
                val activeGlow = 5.dp.toPx()

                // 1. Solar PV (Top Left) -> Center (Inverter)
                drawLine(
                    color = if (metrics.isSolarOn) SolarGold.copy(alpha = 0.25f) else Color.Gray.copy(alpha = 0.15f),
                    start = Offset(pvX + nodeWidth / 2f, pvY),
                    end = Offset(centralInverterX - 35.dp.toPx(), pvY),
                    strokeWidth = lineStroke
                )
                drawLine(
                    color = if (metrics.isSolarOn) SolarGold.copy(alpha = 0.25f) else Color.Gray.copy(alpha = 0.15f),
                    start = Offset(centralInverterX - 35.dp.toPx(), pvY),
                    end = Offset(centralInverterX - 35.dp.toPx(), centralInverterY - 20.dp.toPx()),
                    strokeWidth = lineStroke
                )
                drawLine(
                    color = if (metrics.isSolarOn) SolarGold.copy(alpha = 0.25f) else Color.Gray.copy(alpha = 0.15f),
                    start = Offset(centralInverterX - 35.dp.toPx(), centralInverterY - 20.dp.toPx()),
                    end = Offset(centralInverterX, centralInverterY - 20.dp.toPx()),
                    strokeWidth = lineStroke
                )

                if (metrics.isSolarOn) {
                    for (i in 0 until 3) {
                        val phaseOffset = i * 33.3f
                        val currentFlow = (flowOffset + phaseOffset) % 100f

                        val d1 = (centralInverterX - 35.dp.toPx()) - (pvX + nodeWidth / 2f)
                        val d2 = (centralInverterY - 20.dp.toPx()) - pvY
                        val d3 = 35.dp.toPx()
                        val totalD = d1 + d2 + d3
                        val currentPos = totalD * (currentFlow / 100f)

                        val dotX: Float
                        val dotY: Float
                        if (currentPos < d1) {
                            dotX = (pvX + nodeWidth / 2f) + currentPos
                            dotY = pvY
                        } else if (currentPos < d1 + d2) {
                            dotX = centralInverterX - 35.dp.toPx()
                            dotY = pvY + (currentPos - d1)
                        } else {
                            dotX = (centralInverterX - 35.dp.toPx()) + (currentPos - d1 - d2)
                            dotY = centralInverterY - 20.dp.toPx()
                        }

                        val trailingAlpha = 1.0f - (i * 0.3f)
                        drawCircle(color = SolarGold.copy(alpha = trailingAlpha), radius = activeGlow, center = Offset(dotX, dotY))
                        drawCircle(color = SolarGold.copy(alpha = trailingAlpha * 0.4f), radius = activeGlow * 2.0f, center = Offset(dotX, dotY))
                    }
                }

                // 2. Battery (Bottom Left) <-> Inverter (Center)
                val hasBatteryCurrent = Math.abs(metrics.batteryCurrent) > 0.05f
                val isCharging = metrics.batteryCurrent > 0f
                drawLine(
                    color = if (hasBatteryCurrent) SolarGreen.copy(alpha = 0.25f) else Color.Gray.copy(alpha = 0.15f),
                    start = Offset(batteryX + nodeWidth / 2f, batteryY),
                    end = Offset(centralInverterX - 35.dp.toPx(), batteryY),
                    strokeWidth = lineStroke
                )
                drawLine(
                    color = if (hasBatteryCurrent) SolarGreen.copy(alpha = 0.25f) else Color.Gray.copy(alpha = 0.15f),
                    start = Offset(centralInverterX - 35.dp.toPx(), batteryY),
                    end = Offset(centralInverterX - 35.dp.toPx(), centralInverterY + 20.dp.toPx()),
                    strokeWidth = lineStroke
                )
                drawLine(
                    color = if (hasBatteryCurrent) SolarGreen.copy(alpha = 0.25f) else Color.Gray.copy(alpha = 0.15f),
                    start = Offset(centralInverterX - 35.dp.toPx(), centralInverterY + 20.dp.toPx()),
                    end = Offset(centralInverterX, centralInverterY + 20.dp.toPx()),
                    strokeWidth = lineStroke
                )

                if (hasBatteryCurrent) {
                    for (i in 0 until 3) {
                        val phaseOffset = i * 33.3f
                        val currentFlow = (flowOffset + phaseOffset) % 100f

                        val d1 = (centralInverterX - 35.dp.toPx()) - (batteryX + nodeWidth / 2f)
                        val d2 = batteryY - (centralInverterY + 20.dp.toPx())
                        val d3 = 35.dp.toPx()
                        val totalD = d1 + d2 + d3

                        val progress = if (isCharging) (currentFlow / 100f) else (1f - (currentFlow / 100f))
                        val currentPos = totalD * progress

                        val dotX: Float
                        val dotY: Float
                        if (currentPos < d1) {
                            dotX = (batteryX + nodeWidth / 2f) + currentPos
                            dotY = batteryY
                        } else if (currentPos < d1 + d2) {
                            dotX = centralInverterX - 35.dp.toPx()
                            dotY = batteryY - (currentPos - d1)
                        } else {
                            dotX = (centralInverterX - 35.dp.toPx()) + (currentPos - d1 - d2)
                            dotY = centralInverterY + 20.dp.toPx()
                        }

                        val trailingAlpha = 1.0f - (i * 0.3f)
                        drawCircle(color = SolarGreen.copy(alpha = trailingAlpha), radius = activeGlow, center = Offset(dotX, dotY))
                        drawCircle(color = SolarGreen.copy(alpha = trailingAlpha * 0.4f), radius = activeGlow * 2.0f, center = Offset(dotX, dotY))
                    }
                }

                // 3. Grid / Bypass (Top Right) -> Inverter (Center)
                drawLine(
                    color = Color.Gray.copy(alpha = 0.1f),
                    start = Offset(gridX - nodeWidth / 2f, gridY),
                    end = Offset(centralInverterX + 35.dp.toPx(), gridY),
                    strokeWidth = lineStroke
                )
                drawLine(
                    color = Color.Gray.copy(alpha = 0.1f),
                    start = Offset(centralInverterX + 35.dp.toPx(), gridY),
                    end = Offset(centralInverterX + 35.dp.toPx(), centralInverterY),
                    strokeWidth = lineStroke
                )

                // 4. Inverter (Center) -> Load (Bottom Right)
                drawLine(
                    color = if (metrics.isLoadOn) SolarRed.copy(alpha = 0.25f) else Color.Gray.copy(alpha = 0.15f),
                    start = Offset(centralInverterX, centralInverterY + 20.dp.toPx()),
                    end = Offset(centralInverterX + 35.dp.toPx(), centralInverterY + 20.dp.toPx()),
                    strokeWidth = lineStroke
                )
                drawLine(
                    color = if (metrics.isLoadOn) SolarRed.copy(alpha = 0.25f) else Color.Gray.copy(alpha = 0.15f),
                    start = Offset(centralInverterX + 35.dp.toPx(), centralInverterY + 20.dp.toPx()),
                    end = Offset(centralInverterX + 35.dp.toPx(), loadY),
                    strokeWidth = lineStroke
                )
                drawLine(
                    color = if (metrics.isLoadOn) SolarRed.copy(alpha = 0.25f) else Color.Gray.copy(alpha = 0.15f),
                    start = Offset(centralInverterX + 35.dp.toPx(), loadY),
                    end = Offset(loadX - nodeWidth / 2f, loadY),
                    strokeWidth = lineStroke
                )

                if (metrics.isLoadOn) {
                    for (i in 0 until 3) {
                        val phaseOffset = i * 33.3f
                        val currentFlow = (flowOffset + phaseOffset) % 100f

                        val d1 = 35.dp.toPx()
                        val d2 = loadY - (centralInverterY + 20.dp.toPx())
                        val d3 = (loadX - nodeWidth / 2f) - (centralInverterX + 35.dp.toPx())
                        val totalD = d1 + d2 + d3
                        val currentPos = totalD * (currentFlow / 100f)

                        val dotX: Float
                        val dotY: Float
                        if (currentPos < d1) {
                            dotX = centralInverterX + currentPos
                            dotY = centralInverterY + 20.dp.toPx()
                        } else if (currentPos < d1 + d2) {
                            dotX = centralInverterX + 35.dp.toPx()
                            dotY = (centralInverterY + 20.dp.toPx()) + (currentPos - d1)
                        } else {
                            dotX = (centralInverterX + 35.dp.toPx()) + (currentPos - d1 - d2)
                            dotY = loadY
                        }

                        val trailingAlpha = 1.0f - (i * 0.3f)
                        drawCircle(color = SolarRed.copy(alpha = trailingAlpha), radius = activeGlow, center = Offset(dotX, dotY))
                        drawCircle(color = SolarRed.copy(alpha = trailingAlpha * 0.4f), radius = activeGlow * 2.0f, center = Offset(dotX, dotY))
                    }
                }
            }

            // --- LAYOUT NODE COMPONENTS OVERLAYING THE FLOW DIAGRAM ---
            val cardW = 110.dp
            val cardH = 75.dp

            // 1. PV SOLAR PRODUCTION NODE (Top Left)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = CardBorder(if (metrics.isSolarOn) SolarGold.copy(alpha = 0.5f) else SolarBorder),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(cardW, cardH)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WbSunny, contentDescription = null, tint = SolarGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PV SOLAR", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (metrics.isSolarOn) "${metrics.panelPower.toInt()} W" else "0 W",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (metrics.isSolarOn) SolarGold else TextSecondary
                    )
                    Text("DC Input", fontSize = 8.sp, color = TextSecondary)
                }
            }

            // 2. GRID / BYPASS NODE (Top Right)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = CardBorder(SolarBorder),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(cardW, cardH)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Power, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("GRID LINE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("0.0 kW", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.Gray)
                    Text("Off Grid", fontSize = 8.sp, color = SolarRed, fontWeight = FontWeight.Bold)
                }
            }

            // 3. SMART CENTRAL INVERTER / GATEWAY NODE (Center)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp)
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(listOf(accentColor, SolarHeroBg)),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(accentColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "INVERTER",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Text(
                        text = if (metrics.isSolarOn) "Smart" else "Standby",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (metrics.isSolarOn) Color(0xFF2E7D32) else Color.Gray
                    )
                }
            }

            // 4. BATTERY GRAPHIC LIQUID CELL NODE (Bottom Left)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(18.dp),
                border = CardBorder(if (metrics.batteryVoltage < 11.5f) SolarRed else SolarBorder),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(cardW, 115.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("BATTERY CELLS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Physical fluid screen layout
                    Box(
                        modifier = Modifier
                            .size(cardW - 32.dp, 50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE3F2FD))
                            .border(1.dp, Color(0xFF90CAF9), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val fillPercentage = metrics.batteryPct / 100f
                            val fillHeight = h * (1f - fillPercentage)

                            val path = androidx.compose.ui.graphics.Path()
                            path.moveTo(0f, h)
                            path.lineTo(0f, fillHeight)

                            // Fluid waves drawing
                            val waveAmp = 3.dp.toPx()
                            val waveFreq = (2 * Math.PI / w).toFloat()
                            for (x in 0..w.toInt()) {
                                val y = fillHeight + waveAmp * kotlin.math.sin(waveFreq * x + wavePhase)
                                path.lineTo(x.toFloat(), y)
                            }
                            path.lineTo(w, h)
                            path.close()

                            drawPath(
                                path = path,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF29B6F6),
                                        Color(0xFF0288D1),
                                        Color(0xFF01579B)
                                    )
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${metrics.batteryPct}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (metrics.batteryPct < 55) TextPrimary else Color.White
                            )
                            Text(
                                text = "${String.format("%.1f", metrics.batteryVoltage)}V",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (metrics.batteryPct < 55) TextSecondary else Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Temp", fontSize = 7.sp, color = TextSecondary)
                            Text("${metrics.batteryTemp.toInt()}°C", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Cur", fontSize = 7.sp, color = TextSecondary)
                            Text(
                                text = "${if (metrics.batteryCurrent >= 0) "+" else ""}${metrics.batteryCurrent}A",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (metrics.batteryCurrent > 0) SolarGreen else if (metrics.batteryCurrent < 0) SolarRed else TextPrimary
                            )
                        }
                    }
                }
            }

            // 5. AC LOAD APPLIANCE OUTLETS NODE (Bottom Right)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = CardBorder(if (metrics.isLoadOn) SolarRed.copy(alpha = 0.5f) else SolarBorder),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(cardW, cardH)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = SolarRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AC LOADS", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (metrics.isLoadOn) "${metrics.loadPower.toInt()} W" else "0 W",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (metrics.isLoadOn) SolarRed else TextSecondary
                    )
                    Text("Total Load", fontSize = 8.sp, color = TextSecondary)
                }
            }

            // --- PIPELINE OVERLAY LABELS SHOWING CURRENT WATT FLOWS ---
            // PV Input Power Badge overlayed on top conduit
            if (metrics.isSolarOn) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = (-38).dp, y = 45.dp)
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .border(1.dp, SolarGold.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Input ${metrics.panelPower.toInt()}W",
                        color = SolarGold,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Output Load Badge overlayed on bottom conduit
            if (metrics.isLoadOn) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(x = 38.dp, y = (-42).dp)
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .border(1.dp, SolarRed.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Output ${metrics.loadPower.toInt()}W",
                        color = SolarRed,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ControlButton(
    title: String,
    subtitle: String,
    watts: String,
    isOn: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardBorder(if (isOn) activeColor.copy(alpha = 0.4f) else SolarBorder),
        modifier = Modifier
            .width(105.dp)
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = watts,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isOn) activeColor else TextSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))
            // Glossy tactical power toggle indicator
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (isOn) activeColor.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (isOn) activeColor else Color.Gray.copy(alpha = 0.4f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isOn) activeColor else Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun SolarDetailsCard(
    metrics: SolarMetrics,
    accentColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SolarCardBg),
        border = CardBorder(SolarBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WbSunny, contentDescription = "Solar Power", tint = SolarGold, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Solar Power: ${if (metrics.isSolarOn) "ON" else "OFF"}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Text("Battery Charging: ${if (metrics.isChargingOn && metrics.isSolarOn) "ON" else "OFF"}", style = MaterialTheme.typography.bodySmall, color = if (metrics.isChargingOn && metrics.isSolarOn) SolarGreen else Color.Gray, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MetricSmallItem("Voltage", "${metrics.panelVoltage} V", SolarCyan, Modifier.weight(1f))
                MetricSmallItem("Current", "${metrics.panelCurrent} A", SolarCyan, Modifier.weight(1f))
                MetricSmallItem("Power", "${metrics.panelPower.toInt()} W", SolarCyan, Modifier.weight(1f))
                MetricSmallItem("Output %", "${metrics.panelOutputPct}%", SolarCyan, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun BatteryDetailsCard(
    metrics: SolarMetrics,
    accentColor: Color,
    isCellVisible: Boolean,
    lowBatteryAlert: Float
) {
    val isAlert = metrics.batteryVoltage < lowBatteryAlert

    Card(
        colors = CardDefaults.cardColors(containerColor = SolarCardBg),
        border = CardBorder(if (isAlert) SolarRed else SolarBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = "Battery Status",
                        tint = if (isAlert) SolarRed else SolarGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Battery Status: ${if (metrics.batteryCurrent > 0.05f) "Charging" else if (metrics.batteryCurrent < -0.05f) "Discharging" else "Idle"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                if (isAlert) {
                    Text(
                        text = "LOW VOLTAGE!",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolarRed
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Large stats row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MetricSmallItem("Voltage", "${metrics.batteryVoltage} V", if (isAlert) SolarRed else SolarGreen, Modifier.weight(1f))
                MetricSmallItem("Current", "${String.format("%.2f", metrics.batteryCurrent)} A", if (isAlert) SolarRed else SolarGreen, Modifier.weight(1f))
                MetricSmallItem("Power", "${metrics.batteryPower.toInt()} W", if (isAlert) SolarRed else SolarGreen, Modifier.weight(1f))
                MetricSmallItem("Temp", "${metrics.batteryTemp}°C", if (isAlert) SolarRed else SolarGreen, Modifier.weight(1f))
            }

            // Visual linear capacity progress bar
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Capacity: ",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .background(SolarBgDark, RoundedCornerShape(5.dp))
                        .border(1.dp, SolarBorder, RoundedCornerShape(5.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth((metrics.batteryPct / 100f).coerceIn(0f, 1f))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = if (isAlert) listOf(SolarRed, SolarRed.copy(alpha = 0.6f))
                                            else listOf(SolarGreen, Color(0xFF00E676))
                                ),
                                shape = RoundedCornerShape(5.dp)
                            )
                    )
                }
                Text(
                    text = "${metrics.batteryPct}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isAlert) SolarRed else SolarGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = SolarBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Capacity details row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Remaining Capacity", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("${metrics.batteryPct}% (${metrics.batteryRemainingAh} Ah)", style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Cycle Count", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("${metrics.batteryCycleCount}", style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }

            // 2-Wire Terminal Connection Monitoring Section (Physically accurate for red and black terminals)
            if (isCellVisible) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Terminal Connection Information", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SolarBgDark, RoundedCornerShape(10.dp))
                        .border(1.dp, SolarBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.Red, RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Standard 2-Wire Terminal Tracking Active",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "শুধু লাল (+) এবং কালো (-) মেইন ব্যাটারি কেবল সংযোগ ব্যবহার করে মাইক্রোকন্ট্রোলার থেকে রিয়েল-টাইম এম্পিয়ার, মোট ভোল্টেজ, চার্জিং প্রটেকশন এবং থার্মাল অ্যালার্ম ট্র্যাক করা হচ্ছে। কোনো বাহ্যিক জটিল সেল ওয়্যারিং বা জটিল মাল্টি-ওয়ার BMS কানেকশনের প্রয়োজন নেই।",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Safety State: Active & Secure", fontSize = 11.sp, color = SolarGreen, fontWeight = FontWeight.Bold)
                    Text("Sensor Route: Direct Terminal ADC", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun LoadDetailsCard(
    metrics: SolarMetrics,
    accentColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SolarCardBg),
        border = CardBorder(SolarBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lightbulb, contentDescription = "Load Outlet", tint = SolarRed, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Load Outlet: ${if (metrics.isLoadOn) "ON" else "OFF"}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Text("Source: ${metrics.loadSource}", style = MaterialTheme.typography.bodySmall, color = SolarCyan, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MetricSmallItem("Voltage", "${metrics.loadVoltage} V", SolarRed, Modifier.weight(1.0f))
                MetricSmallItem("Current", "${metrics.loadCurrent} A", SolarRed, Modifier.weight(1.0f))
                MetricSmallItem("Power", "${metrics.loadPower.toInt()} W", SolarRed, Modifier.weight(1.0f))
                MetricSmallItem("Panel share", "${metrics.loadPanelShareW.toInt()} W", SolarRed, Modifier.weight(1.0f))
            }
        }
    }
}

@Composable
fun MetricSmallItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 15.sp, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CardBorder(color: Color): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(1.dp, color)
}

@Composable
fun ProtectionPanelCard(
    metrics: SolarMetrics,
    viewModel: SolarViewModel,
    accentColor: Color
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SolarCardBg),
        border = CardBorder(SolarBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Protections",
                        tint = if (metrics.isAutoCutTripped) SolarRed else SolarGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Intelligent Auto-Cut Safeguards",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                
                // Active status badge
                Box(
                    modifier = Modifier
                        .background(
                            color = if (metrics.isAutoCutTripped) SolarRed.copy(alpha = 0.15f) else SolarGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (metrics.isAutoCutTripped) SolarRed else SolarGreen,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (metrics.isAutoCutTripped) "SYSTEM TRIPPED" else "ALL SAFE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (metrics.isAutoCutTripped) SolarRed else SolarGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // State alert box
            if (metrics.isAutoCutTripped) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SolarRed.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .border(1.dp, SolarRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = "Trip", tint = SolarRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("IPS Safety Breaker Tripped!", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("The output load has been auto-cut to protect system battery and components. Review the indicators below.", color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Text(
                    text = "The integrated hybrid microcontroller monitors voltage and current parameters to guarantee 100% active safety.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Grid of 6 guards
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProtectionIndicatorItem(
                        title = "Over-Voltage Guard",
                        statusText = if (metrics.isOverVoltage) "TRIPPED (HIGH)" else "Active (Safe)",
                        isTripped = metrics.isOverVoltage,
                        modifier = Modifier.weight(1f)
                    )
                    ProtectionIndicatorItem(
                        title = "Low-Voltage Guard",
                        statusText = if (metrics.isLowVoltage) "TRIPPED (LOW)" else "Active (Safe)",
                        isTripped = metrics.isLowVoltage,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProtectionIndicatorItem(
                        title = "Over-Current Guard",
                        statusText = if (metrics.isOverCurrent) "TRIPPED (HIGH)" else "Active (Safe)",
                        isTripped = metrics.isOverCurrent,
                        modifier = Modifier.weight(1f)
                    )
                    ProtectionIndicatorItem(
                        title = "Low-Current Guard",
                        statusText = if (metrics.isLowCurrent) "LOW CURRENT" else "Normal",
                        isTripped = false,
                        isWarning = metrics.isLowCurrent,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ProtectionIndicatorItem(
                        title = "Short-Circuit Guard",
                        statusText = "Safe / Active",
                        isTripped = false,
                        modifier = Modifier.weight(1f)
                    )
                    ProtectionIndicatorItem(
                        title = "Surge & Temp Protection",
                        statusText = "Safe / Active",
                        isTripped = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = SolarBorder, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Fault injection and reset buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Demonstrate Protections:",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (metrics.isAutoCutTripped) {
                        Button(
                            onClick = { viewModel.resetProtections() },
                            colors = ButtonDefaults.buttonColors(containerColor = SolarGreen),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.Black, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Relay", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.triggerOverVoltageFault() },
                            border = BorderStroke(1.dp, SolarRed.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SolarRed),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Test OVP", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.triggerOverCurrentFault() },
                            border = BorderStroke(1.dp, SolarRed.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SolarRed),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Test OCP", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProtectionIndicatorItem(
    title: String,
    statusText: String,
    isTripped: Boolean,
    isWarning: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(SolarBgDark, RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = if (isTripped) SolarRed else if (isWarning) SolarGold else SolarBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(text = title, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isTripped) SolarRed else if (isWarning) SolarGold else SolarGreen
                )
            }
            
            // LED Light circle
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (isTripped) SolarRed else if (isWarning) SolarGold else SolarGreen,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun LivePowerBalanceDialGauge(
    metrics: SolarMetrics,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gauge_glow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.03f)),
        border = BorderStroke(1.dp, SolarBorder.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Left: The Custom Circular Gauge Canvas
            Box(
                modifier = Modifier
                    .size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidthValue = 10.dp.toPx()
                    val centerOffset = Offset(size.width / 2, size.height / 2)
                    val radius = (size.width - strokeWidthValue) / 2f

                    // 1. Draw background neutral tracks
                    drawCircle(
                        color = Color.Gray.copy(alpha = 0.1f),
                        radius = radius,
                        center = centerOffset,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidthValue)
                    )

                    // 2. Solar Generation Arc (Solar power out of 805W maximum)
                    val pvRatio = (metrics.panelPower / 805f).coerceIn(0f, 1f)
                    val pvSweepAngle = pvRatio * 180f
                    drawArc(
                        color = SolarGold,
                        startAngle = 180f,
                        sweepAngle = pvSweepAngle,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = strokeWidthValue,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        ),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        topLeft = Offset(strokeWidthValue / 2, strokeWidthValue / 2)
                    )

                    // 3. Load Consumption Arc (Load power out of 1500W maximum)
                    val loadRatio = (metrics.loadPower / 1500f).coerceIn(0f, 1f)
                    val loadSweepAngle = loadRatio * 180f
                    drawArc(
                        color = SolarRed,
                        startAngle = 360f,
                        sweepAngle = -loadSweepAngle,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = strokeWidthValue,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        ),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        topLeft = Offset(strokeWidthValue / 2, strokeWidthValue / 2)
                    )
                }

                // Center Digital Readout
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val netPower = metrics.panelPower - metrics.loadPower
                    val isSurplus = netPower >= 0
                    Text(
                        text = if (isSurplus) "SURPLUS" else "DEFICIT",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSurplus) SolarGreen else SolarRed,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${if (isSurplus) "+" else ""}${netPower.toInt()} W",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = (if (isSurplus) SolarGreen else SolarRed).copy(alpha = pulseGlow),
                                shape = CircleShape
                            )
                    )
                }
            }

            // Right: Legend and detailed readouts
            Column(
                modifier = Modifier.padding(start = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(SolarGold, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Solar Input", fontSize = 8.sp, color = TextSecondary)
                        Text("${metrics.panelPower.toInt()} W / 805W", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(SolarRed, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Grid Load", fontSize = 8.sp, color = TextSecondary)
                        Text("${metrics.loadPower.toInt()} W / 1500W", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(SolarGreen, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Battery Pct", fontSize = 8.sp, color = TextSecondary)
                        Text("${metrics.batteryPct}% (${metrics.batteryVoltage} V)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }
        }
    }
}
