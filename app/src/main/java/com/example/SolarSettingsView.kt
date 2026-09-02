package com.example

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SolarSettingsView(
    viewModel: SolarViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val isPaired by viewModel.isPaired.collectAsState()
    val pairedDeviceId by viewModel.pairedDeviceId.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val scannedDevices by viewModel.scannedDevices.collectAsState()

    val scrollState = rememberScrollState()

    var editedName by remember(settings.customSolarName) { mutableStateOf(settings.customSolarName) }
    var editedBroker by remember(settings.mqttBrokerUrl) { mutableStateOf(settings.mqttBrokerUrl) }
    var editedPort by remember(settings.mqttPort) { mutableStateOf(settings.mqttPort.toString()) }
    var editedTopic by remember(settings.mqttTopicPrefix) { mutableStateOf(settings.mqttTopicPrefix) }
    var editedClientId by remember(settings.mqttClientId) { mutableStateOf(settings.mqttClientId) }

    var limitVoltage by remember(settings.maxVoltageLimit) { mutableStateOf(settings.maxVoltageLimit) }
    var limitCurrent by remember(settings.maxCurrentLimit) { mutableStateOf(settings.maxCurrentLimit) }
    var limitLowBattery by remember(settings.lowBatteryAlertLimit) { mutableStateOf(settings.lowBatteryAlertLimit) }
    var limitMaxBatteryChargeCurrent by remember(settings.maxBatteryChargeCurrent) { mutableStateOf(settings.maxBatteryChargeCurrent) }
    var limitInverterCutOffVoltage by remember(settings.inverterCutOffVoltage) { mutableStateOf(settings.inverterCutOffVoltage) }

    // Provisioning states
    var provisionSsid by remember { mutableStateOf("IPS_Smart_WiFi") }
    var provisionPass by remember { mutableStateOf("bangladesh2026") }

    // DSP/IPS state variables
    var editedSerialId by remember(settings.deviceSerialNumber) { mutableStateOf(settings.deviceSerialNumber) }
    var editedInstallerName by remember(settings.installerName) { mutableStateOf(settings.installerName) }
    var editedInstallerPhone by remember(settings.installerPhone) { mutableStateOf(settings.installerPhone) }
    var editedBaudRate by remember(settings.dspBaudRate) { mutableStateOf(settings.dspBaudRate.toString()) }
    var editedModbusAddr by remember(settings.dspModbusAddress) { mutableStateOf(settings.dspModbusAddress.toString()) }
    var editedVaRating by remember(settings.ipsOutputRatingVa) { mutableStateOf(settings.ipsOutputRatingVa.toString()) }
    var editedBatteryType by remember(settings.batteryTypeSelected) { mutableStateOf(settings.batteryTypeSelected) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "DASHBOARD CUSTOMIZER & SETTINGS",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SolarCyan,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(top = 16.dp)
        )

        // Collapsible Help & ESP32 Pairing Guide
        HelpPairingGuideCard()

        // 1. Theme Color Selector Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SolarCardBg),
            border = CardBorder(SolarBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Accent Design Theme",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Choose the system-wide dynamic highlight accent color.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ColorAccentButton(color = SolarCyan, label = "Electric Blue", isSelected = settings.selectedAccentHex == "#00E5FF") {
                        viewModel.saveSettings(settings.copy(selectedAccentHex = "#00E5FF"))
                    }
                    ColorAccentButton(color = SolarGold, label = "Amber Gold", isSelected = settings.selectedAccentHex == "#FFA000") {
                        viewModel.saveSettings(settings.copy(selectedAccentHex = "#FFA000"))
                    }
                    ColorAccentButton(color = SolarGreen, label = "Eco Green", isSelected = settings.selectedAccentHex == "#00E676") {
                        viewModel.saveSettings(settings.copy(selectedAccentHex = "#00E676"))
                    }
                    ColorAccentButton(color = SolarRed, label = "Cyber Red", isSelected = settings.selectedAccentHex == "#FF1744") {
                        viewModel.saveSettings(settings.copy(selectedAccentHex = "#FF1744"))
                    }
                }
            }
        }

        // 1.5 Real IoT Gateway Bluetooth/Wi-Fi Provisioning & Pairing Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SolarCardBg),
            border = CardBorder(if (isPaired) SolarGreen.copy(alpha = 0.5f) else SolarGold.copy(alpha = 0.5f)),
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
                            imageVector = if (isPaired) Icons.Default.BluetoothConnected else Icons.Default.BluetoothSearching,
                            contentDescription = "IoT Pair",
                            tint = if (isPaired) SolarGreen else SolarGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ESP32 IoT Inverter Pairing",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isPaired) SolarGreen.copy(alpha = 0.12f) else SolarGold.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isPaired) "PAIRED & ONLINE" else "OFFLINE / DISCONNECTED",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isPaired) SolarGreen else SolarGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "স্মার্ট আইওটি প্লাগের মতো আপনার হাইব্রিড ইনভার্টার বা আইপিএস-এর সাথে ব্লুটুথ ও লোকাল ওয়াই-ফাই দিয়ে প্রথমবার পেয়ারিং সম্পন্ন করুন। একবার কানেক্ট করলে যেকোনো জায়গা থেকে ক্লাউড মনিটরিং করা যাবে।",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (isPaired) {
                    // Paired view
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SolarBgDark, RoundedCornerShape(12.dp))
                            .border(1.dp, SolarBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Devices, contentDescription = "Device", tint = SolarGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Connected Device: $pairedDeviceId",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Telemetry Route: MQTT Broker -> $editedBroker -> smart/solar/$pairedDeviceId",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.unpairDevice() },
                        colors = ButtonDefaults.buttonColors(containerColor = SolarRed.copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Unpair", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unpair & Disconnect System", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    // Unpaired view: scan and input credentials
                    Text(
                        text = "Step 1: Enter local Router Wi-Fi Credentials",
                        fontWeight = FontWeight.Bold,
                        color = SolarCyan,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = provisionSsid,
                        onValueChange = { provisionSsid = it },
                        label = { Text("Local Wi-Fi SSID (Router Name)", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = SolarCyan,
                            unfocusedBorderColor = SolarBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = provisionPass,
                        onValueChange = { provisionPass = it },
                        label = { Text("Local Wi-Fi WPA2 Password", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = SolarCyan,
                            unfocusedBorderColor = SolarBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Step 2: Scan & Select ESP32 Inverter over BLE",
                        fontWeight = FontWeight.Bold,
                        color = SolarCyan,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isScanning) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = SolarGold, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Scanning for Hybrid Inverters via BLE...", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    } else if (scannedDevices.isEmpty()) {
                        Button(
                            onClick = { viewModel.startBleScan() },
                            colors = ButtonDefaults.buttonColors(containerColor = SolarGold),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Scan", tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan for Inverter Bluetooth Gateways", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            scannedDevices.forEach { devId ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SolarBgDark, RoundedCornerShape(10.dp))
                                        .border(1.dp, SolarBorder, RoundedCornerShape(10.dp))
                                        .clickable {
                                            viewModel.pairDevice(
                                                deviceId = devId,
                                                wifiSsid = provisionSsid,
                                                wifiPass = provisionPass
                                            )
                                        }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Bluetooth, contentDescription = "BLE", tint = SolarGold, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(devId, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("Ready to provision & bind", color = TextSecondary, fontSize = 9.sp)
                                        }
                                    }
                                    Text(
                                        text = "PAIR & CONFIG",
                                        color = SolarCyan,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedButton(
                                onClick = { viewModel.startBleScan() },
                                border = BorderStroke(1.dp, SolarBorder),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Rescan BLE Gateways", fontSize = 10.sp, color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }

        // 2. Custom Dashboard Metadata
        Card(
            colors = CardDefaults.cardColors(containerColor = SolarCardBg),
            border = CardBorder(SolarBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Dashboard Custom Metadata",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editedName,
                    onValueChange = {
                        editedName = it
                        viewModel.saveSettings(settings.copy(customSolarName = it))
                    },
                    label = { Text("Solar Station Name", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = SolarCyan,
                        unfocusedBorderColor = SolarBorder,
                        cursorColor = SolarCyan
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 2B. IPS Device Registration & Global Binding
        Card(
            colors = CardDefaults.cardColors(containerColor = SolarCardBg),
            border = CardBorder(SolarBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "IPS Customer Device Binding",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "যেকোনো জায়গা থেকে লাইভ মনিটর করার জন্য আপনার IPS এর গায়ে থাকা ইউনিক সিরিয়াল আইডি ও ব্যাটারি প্রোফাইল যুক্ত করুন।",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editedSerialId,
                    onValueChange = {
                        editedSerialId = it
                        // Automatically map Root Topic to ensure seamless plug-and-play global IoT sync!
                        val sanitized = it.trim().replace(" ", "_").lowercase()
                        viewModel.saveSettings(settings.copy(
                            deviceSerialNumber = it,
                            mqttTopicPrefix = "ips/global/$sanitized"
                        ))
                    },
                    label = { Text("Device Serial Number (Unique ID)", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = SolarCyan,
                        unfocusedBorderColor = SolarBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editedVaRating,
                        onValueChange = {
                            editedVaRating = it
                            val rating = it.toIntOrNull() ?: 1000
                            viewModel.saveSettings(settings.copy(ipsOutputRatingVa = rating))
                        },
                        label = { Text("IPS Capacity (VA)", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = SolarCyan,
                            unfocusedBorderColor = SolarBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = editedBatteryType,
                        onValueChange = {
                            editedBatteryType = it
                            viewModel.saveSettings(settings.copy(batteryTypeSelected = it))
                        },
                        label = { Text("Battery Type Preset", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = SolarCyan,
                            unfocusedBorderColor = SolarBorder
                        ),
                        modifier = Modifier.weight(1.5f)
                    )
                }
            }
        }

        // 2C. DSP Controller Serial Protocol (Installer/Builder Only)
        Card(
            colors = CardDefaults.cardColors(containerColor = SolarCardBg),
            border = CardBorder(SolarBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DSP / IPS Installer Hardware Config",
                    style = MaterialTheme.typography.titleSmall,
                    color = SolarCyan,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "DSP কন্ট্রোল বোর্ড এবং ESP32 এর মধ্যকার সিরিয়াল কানেক্টিভিটি ও ব্রান্ডিং সেটিংস (শুধুমাত্র ম্যানুফ্যাকচারারদের জন্য)।",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editedBaudRate,
                        onValueChange = {
                            editedBaudRate = it
                            val baud = it.toIntOrNull() ?: 9600
                            viewModel.saveSettings(settings.copy(dspBaudRate = baud))
                        },
                        label = { Text("UART Baud Rate", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = SolarCyan,
                            unfocusedBorderColor = SolarBorder
                        ),
                        modifier = Modifier.weight(1.2f)
                    )

                    OutlinedTextField(
                        value = editedModbusAddr,
                        onValueChange = {
                            editedModbusAddr = it
                            val addr = it.toIntOrNull() ?: 1
                            viewModel.saveSettings(settings.copy(dspModbusAddress = addr))
                        },
                        label = { Text("Modbus ID", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = SolarCyan,
                            unfocusedBorderColor = SolarBorder
                        ),
                        modifier = Modifier.weight(0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editedInstallerName,
                    onValueChange = {
                        editedInstallerName = it
                        viewModel.saveSettings(settings.copy(installerName = it))
                    },
                    label = { Text("Installer / Brand Company Name", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = SolarCyan,
                        unfocusedBorderColor = SolarBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editedInstallerPhone,
                    onValueChange = {
                        editedInstallerPhone = it
                        viewModel.saveSettings(settings.copy(installerPhone = it))
                    },
                    label = { Text("Helpline Phone Number", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = SolarCyan,
                        unfocusedBorderColor = SolarBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 3. Card Layout Visibility Options
        Card(
            colors = CardDefaults.cardColors(containerColor = SolarCardBg),
            border = CardBorder(SolarBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Dashboard Layout Visibility",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                VisibilityToggleRow(
                    label = "Show Energy Flow Animation Diagram",
                    isChecked = settings.isFlowDiagramVisible
                ) {
                    viewModel.saveSettings(settings.copy(isFlowDiagramVisible = !settings.isFlowDiagramVisible))
                }

                VisibilityToggleRow(
                    label = "Show Terminal Connection Diagnostics",
                    isChecked = settings.isCellVoltagesVisible
                ) {
                    viewModel.saveSettings(settings.copy(isCellVoltagesVisible = !settings.isCellVoltagesVisible))
                }
            }
        }

        // 4. MQTT Connection Broker Config (Direct Cloud Deployment Connection Details!)
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
                    Text(
                        text = "Cloud MQTT Broker Configuration",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .background(SolarGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .border(1.dp, SolarGreen, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Active Mock Server", color = SolarGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ESP32 থেকে সরাসরি ক্লাউড ডাটাবেসে কানেক্ট করার জন্য MQTT Broker সেটআপ প্যারামিটার সেট করুন।",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                 OutlinedTextField(
                    value = editedBroker,
                    onValueChange = {
                        editedBroker = it
                        viewModel.saveSettings(settings.copy(mqttBrokerUrl = it))
                    },
                    label = { Text("MQTT Broker Address (e.g. HiveMQ, EMQX)", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = SolarCyan,
                        unfocusedBorderColor = SolarBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editedPort,
                        onValueChange = {
                            editedPort = it
                            val portInt = it.toIntOrNull() ?: 1883
                            viewModel.saveSettings(settings.copy(mqttPort = portInt))
                        },
                        label = { Text("Port", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = SolarCyan,
                            unfocusedBorderColor = SolarBorder
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = editedClientId,
                        onValueChange = {
                            editedClientId = it
                            viewModel.saveSettings(settings.copy(mqttClientId = it))
                        },
                        label = { Text("Client ID", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = SolarCyan,
                            unfocusedBorderColor = SolarBorder
                        ),
                        modifier = Modifier.weight(2f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = editedTopic,
                    onValueChange = {
                        editedTopic = it
                        viewModel.saveSettings(settings.copy(mqttTopicPrefix = it))
                    },
                    label = { Text("MQTT Root Topic", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = SolarCyan,
                        unfocusedBorderColor = SolarBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 5. Custom Safety Threshold Protection Sliders
        Card(
            colors = CardDefaults.cardColors(containerColor = SolarCardBg),
            border = CardBorder(SolarBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Custom Safety Limit Controls",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Hardware Programmed Limits Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SolarRed.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .border(1.dp, SolarRed.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "🔒 Device Programmed Protection (হার্ডওয়্যার প্রোগ্রামড সুরক্ষা)",
                            style = MaterialTheme.typography.labelMedium,
                            color = SolarRed,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "নিরাপত্তা রক্ষার সুবিধার্থে নিম্নলিখিত থ্রেশহোল্ড ভ্যালুগুলো ডিভাইসে সরাসরি প্রোগ্রাম করা থাকে, যাহা সুরক্ষার স্বার্থে মোবাইল অ্যাপ থেকে পরিবর্তনযোগ্য নয়।",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            lineHeight = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Auto-Cut Over-Voltage", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                Text("${String.format("%.1f", settings.maxVoltageLimit)} V", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Auto-Cut Over-Current", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                Text("${String.format("%.1f", settings.maxCurrentLimit)} A", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Low Battery Alarm
                Text(
                    text = "Auto-Cut Under-Voltage Limit: ${String.format("%.1f", limitLowBattery)} V",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = limitLowBattery,
                    onValueChange = {
                        limitLowBattery = it
                        viewModel.saveSettings(settings.copy(lowBatteryAlertLimit = it))
                    },
                    valueRange = 10.5f..12.2f, // Accurate low voltage cutoff parameters
                    colors = SliderDefaults.colors(thumbColor = SolarRed, activeTrackColor = SolarRed)
                )

                // Precise Input Threshold text editor
                OutlinedTextField(
                    value = if (limitLowBattery == 0f) "" else String.format("%.2f", limitLowBattery),
                    onValueChange = { newValue ->
                        val parsed = newValue.toFloatOrNull()
                        if (parsed != null && parsed >= 10.0f && parsed <= 13.0f) {
                            limitLowBattery = parsed
                            viewModel.saveSettings(settings.copy(lowBatteryAlertLimit = parsed))
                        }
                    },
                    label = { Text("Exact Low Voltage Threshold Input (V)", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = SolarRed,
                        unfocusedBorderColor = SolarBorder
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = SolarBorder.copy(alpha = 0.3f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Custom Setup Threshold Parameters",
                    style = MaterialTheme.typography.titleSmall,
                    color = SolarCyan,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Max Battery Charge Current
                Text(
                    text = "Max Battery Charge Current: ${String.format("%.1f", limitMaxBatteryChargeCurrent)} A",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = limitMaxBatteryChargeCurrent,
                    onValueChange = {
                        limitMaxBatteryChargeCurrent = it
                        viewModel.saveSettings(settings.copy(maxBatteryChargeCurrent = it))
                    },
                    valueRange = 10.0f..80.0f,
                    colors = SliderDefaults.colors(thumbColor = SolarGold, activeTrackColor = SolarGold)
                )

                OutlinedTextField(
                    value = if (limitMaxBatteryChargeCurrent == 0f) "" else String.format("%.1f", limitMaxBatteryChargeCurrent),
                    onValueChange = { newValue ->
                        val parsed = newValue.toFloatOrNull()
                        if (parsed != null && parsed >= 10.0f && parsed <= 80.0f) {
                            limitMaxBatteryChargeCurrent = parsed
                            viewModel.saveSettings(settings.copy(maxBatteryChargeCurrent = parsed))
                        }
                    },
                    label = { Text("Exact Max Battery Charge Current (A)", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = SolarGold,
                        unfocusedBorderColor = SolarBorder
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Inverter Cut-off Voltage
                Text(
                    text = "Inverter Cut-off Under-Voltage: ${String.format("%.1f", limitInverterCutOffVoltage)} V",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = limitInverterCutOffVoltage,
                    onValueChange = {
                        limitInverterCutOffVoltage = it
                        viewModel.saveSettings(settings.copy(inverterCutOffVoltage = it))
                    },
                    valueRange = 9.5f..13.0f,
                    colors = SliderDefaults.colors(thumbColor = SolarRed, activeTrackColor = SolarRed)
                )

                OutlinedTextField(
                    value = if (limitInverterCutOffVoltage == 0f) "" else String.format("%.2f", limitInverterCutOffVoltage),
                    onValueChange = { newValue ->
                        val parsed = newValue.toFloatOrNull()
                        if (parsed != null && parsed >= 9.5f && parsed <= 13.0f) {
                            limitInverterCutOffVoltage = parsed
                            viewModel.saveSettings(settings.copy(inverterCutOffVoltage = parsed))
                        }
                    },
                    label = { Text("Exact Inverter Cut-off Voltage (V)", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = SolarRed,
                        unfocusedBorderColor = SolarBorder
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ColorAccentButton(
    color: Color,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(color, CircleShape)
                .border(2.dp, if (isSelected) TextPrimary else Color.Transparent, CircleShape)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 9.sp, color = if (isSelected) TextPrimary else TextSecondary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun VisibilityToggleRow(
    label: String,
    isChecked: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = TextPrimary)
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onCheckedChange() },
            colors = CheckboxDefaults.colors(
                checkedColor = SolarCyan,
                checkmarkColor = Color.White
            )
        )
    }
}

@Composable
fun HelpPairingGuideCard() {
    var isExpanded by remember { mutableStateOf(false) }
    Card(
        colors = CardDefaults.cardColors(containerColor = SolarCardBg),
        border = CardBorder(SolarCyan.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "Help Guide",
                        tint = SolarCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "ESP32 Pairing & Connection Guide",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "How to connect your ESP32 Solar System",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Toggle Expand",
                    tint = TextSecondary
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = SolarBorder.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                GuideStepItem(
                    step = "1",
                    title = "Enable Bluetooth (BLE)",
                    desc = "Turn on Bluetooth on your smartphone. The ESP32 broadcasts a BLE signal with the name starting with 'ESP32_HYBRID_IPS_...' when it is ready to pair."
                )

                GuideStepItem(
                    step = "2",
                    title = "Scan & Find Dev",
                    desc = "Go to 'ESP32 DevKit V1' section in the settings tab below. Click on 'Rescan BLE Gateways'. Locate your serial ID and click on 'PAIR & CONFIG'."
                )

                GuideStepItem(
                    step = "3",
                    title = "Configure Local Wi-Fi",
                    desc = "Enter your local home router Wi-Fi SSID and Password. The app securely transfers these credentials via an encrypted Bluetooth handshake."
                )

                GuideStepItem(
                    step = "4",
                    title = "Confirm MQTT Telemetry",
                    desc = "Once the ESP32 logs into Wi-Fi, it connects to HiveMQ Cloud. The main dashboard connection status will turn to 'MQTT ONLINE' and stream live metrics!"
                )

                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SolarCyan.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .border(1.dp, SolarCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "💡 Pro Tip: Ensure your Wi-Fi is 2.4GHz. The ESP32 does not support 5GHz dual-band router bands.",
                        fontSize = 11.sp,
                        color = SolarCyan,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun GuideStepItem(
    step: String,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(SolarCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}
