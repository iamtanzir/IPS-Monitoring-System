package com.example

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
    val scrollState = rememberScrollState()

    var editedName by remember(settings.customSolarName) { mutableStateOf(settings.customSolarName) }
    var editedBroker by remember(settings.mqttBrokerUrl) { mutableStateOf(settings.mqttBrokerUrl) }
    var editedPort by remember(settings.mqttPort) { mutableStateOf(settings.mqttPort.toString()) }
    var editedTopic by remember(settings.mqttTopicPrefix) { mutableStateOf(settings.mqttTopicPrefix) }
    var editedClientId by remember(settings.mqttClientId) { mutableStateOf(settings.mqttClientId) }

    var limitVoltage by remember(settings.maxVoltageLimit) { mutableStateOf(settings.maxVoltageLimit) }
    var limitCurrent by remember(settings.maxCurrentLimit) { mutableStateOf(settings.maxCurrentLimit) }
    var limitLowBattery by remember(settings.lowBatteryAlertLimit) { mutableStateOf(settings.lowBatteryAlertLimit) }

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
                    label = "Show Battery Cell Voltages Status",
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

                // Max Voltage limit
                Text(
                    text = "Auto-Cut Over-Voltage Limit: ${String.format("%.1f", limitVoltage)} V",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = limitVoltage,
                    onValueChange = {
                        limitVoltage = it
                        viewModel.saveSettings(settings.copy(maxVoltageLimit = it))
                    },
                    valueRange = 14.0f..16.0f, // Narrower, realistic single-battery voltage ranges
                    colors = SliderDefaults.colors(thumbColor = SolarCyan, activeTrackColor = SolarCyan)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Max Current limit
                Text(
                    text = "Auto-Cut Over-Current Limit: ${String.format("%.1f", limitCurrent)} A",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Slider(
                    value = limitCurrent,
                    onValueChange = {
                        limitCurrent = it
                        viewModel.saveSettings(settings.copy(maxCurrentLimit = it))
                    },
                    valueRange = 2.0f..15.0f, // Better suited for simulated fault triggers
                    colors = SliderDefaults.colors(thumbColor = SolarGold, activeTrackColor = SolarGold)
                )

                Spacer(modifier = Modifier.height(12.dp))

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
