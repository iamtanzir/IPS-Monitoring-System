package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ui.theme.SolarCyan
import com.example.ui.theme.SolarGold
import com.example.ui.theme.SolarGreen
import com.example.ui.theme.SolarRed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

// Notification data class
data class SystemNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val severity: String // "info", "warning", "critical"
)

// Data classes for Solar metrics
data class SolarMetrics(
    val isSolarOn: Boolean = true,
    val isChargingOn: Boolean = true,
    val isLoadOn: Boolean = true,
    val isGridOn: Boolean = true,
    val gridVoltage: Float = 220.0f,
    val gridCurrent: Float = 0.55f,
    val gridPower: Float = 120.0f,
    val panelVoltage: Float = 18.3f,
    val panelCurrent: Float = 1.53f,
    val panelPower: Float = 28f,
    val panelOutputPct: Int = 12,
    
    val batteryVoltage: Float = 12.6f,
    val batteryCurrent: Float = 1.2f,
    val batteryPower: Float = 15f,
    val batteryCapacityAh: Int = 100,
    val batteryRemainingAh: Float = 85f,
    val batteryPct: Int = 85,
    val batteryTemp: Float = 38.5f,
    val batteryCycleCount: Int = 13,
    val isBalancerOn: Boolean = true,
    val cellVoltages: List<Float> = listOf(3.152f, 3.154f, 3.150f, 3.148f),
    
    val loadVoltage: Float = 12.0f,
    val loadCurrent: Float = 1.93f,
    val loadPower: Float = 23f,
    val loadPanelShareW: Float = 15f,
    val loadBatteryShareW: Float = 8f,
    val loadGridShareW: Float = 0f,
    val loadSource: String = "Panel + Battery", // Panel, Battery, Grid, Panel + Battery
    
    // System protection and auto-cut statuses
    val isOverCurrent: Boolean = false,
    val isLowCurrent: Boolean = false,
    val isOverVoltage: Boolean = false,
    val isLowVoltage: Boolean = false,
    val isAutoCutTripped: Boolean = false,
    val isShortCircuitSafe: Boolean = true,
    val isSurgeProtected: Boolean = true,
    val isOverTemperature: Boolean = false
)

data class GridHistoricalSummary(
    val dateLabel: String, // Today, Yesterday, Before yesterday
    val maxPvPower: Float,
    val minPvPower: Float,
    val avgPvPower: Float,
    val maxPvVoltage: Float,
    val minPvVoltage: Float,
    val avgPvVoltage: Float,
    val maxPvAmpere: Float,
    val minPvAmpere: Float,
    val avgPvAmpere: Float,
    val totalBatteryChargeWh: Float,
    val totalBatteryChargeAh: Float,
    val maxBatteryChargeAmp: Float,
    val minBatteryChargeAmp: Float,
    val avgBatteryChargeAmp: Float,
    val maxDischargeW: Float,
    val minDischargeW: Float,
    val totalDischargeWh: Float,
    val loadFromPanelWh: Float,
    val loadFromBatteryWh: Float,
    val maxDischargeAmp: Float,
    val minDischargeAmp: Float,
    val avgDischargeAmp: Float
)

data class CustomizerSettings(
    val customSolarName: String = "Universal IPS & Solar System",
    val selectedAccentHex: String = "#00E5FF", // Cyan default
    val isCellVoltagesVisible: Boolean = true,
    val isFlowDiagramVisible: Boolean = true,
    val isSummaryGridVisible: Boolean = true,
    val maxVoltageLimit: Float = 24.0f,
    val maxCurrentLimit: Float = 20.0f,
    val lowBatteryAlertLimit: Float = 11.5f,
    val maxBatteryChargeCurrent: Float = 40.0f,
    val inverterCutOffVoltage: Float = 10.8f,
    
    // MQTT connection simulation
    val mqttBrokerUrl: String = "broker.hivemq.com",
    val mqttPort: Int = 1883,
    val mqttTopicPrefix: String = "smart/solar/solar01",
    val mqttClientId: String = "solar_monitor_mobile_01",
    val isMqttConnected: Boolean = true,

    // DSP/IPS Manufacturer & Builder Integration Settings
    val deviceSerialNumber: String = "IPS-DSP-2026-X88",
    val installerName: String = "Dacca IPS Technologies",
    val installerPhone: String = "+880 1712-345678",
    val dspBaudRate: Int = 9600,
    val dspModbusAddress: Int = 1,
    val ipsOutputRatingVa: Int = 1000,
    val batteryTypeSelected: String = "Tall Tubular Lead-Acid"
)

class SolarViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("solar_prefs", Context.MODE_PRIVATE)

    // Current metrics flow
    private val _metrics = MutableStateFlow(SolarMetrics())
    val metrics: StateFlow<SolarMetrics> = _metrics.asStateFlow()

    // Notifications StateFlow
    private val _notifications = MutableStateFlow<List<SystemNotification>>(emptyList())
    val notifications: StateFlow<List<SystemNotification>> = _notifications.asStateFlow()

    // Last Sync timestamp
    private val _lastSyncTime = MutableStateFlow(SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date()))
    val lastSyncTime: StateFlow<String> = _lastSyncTime.asStateFlow()

    fun triggerNotification(title: String, message: String, severity: String) {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val timeStr = sdf.format(Date())
        val id = System.currentTimeMillis().toString()
        val newNotif = SystemNotification(id, title, message, timeStr, severity)
        _notifications.value = listOf(newNotif) + _notifications.value.take(29) // Keep last 30
    }

    fun dismissNotification(id: String) {
        _notifications.value = _notifications.value.filter { it.id != id }
    }

    // IoT Pairing variables
    private val _isPaired = MutableStateFlow(prefs.getBoolean("is_paired", true)) // Default to true so existing demo works out-of-the-box
    val isPaired: StateFlow<Boolean> = _isPaired.asStateFlow()

    private val _pairedDeviceId = MutableStateFlow(prefs.getString("paired_device_id", "ESP32_C6_HYBRID_IPS_N16R8") ?: "ESP32_C6_HYBRID_IPS_N16R8")
    val pairedDeviceId: StateFlow<String> = _pairedDeviceId.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<String>>(emptyList())
    val scannedDevices: StateFlow<List<String>> = _scannedDevices.asStateFlow()

    fun startBleScan() {
        viewModelScope.launch {
            _isScanning.value = true
            _scannedDevices.value = emptyList()
            kotlinx.coroutines.delay(1000)
            _scannedDevices.value = listOf(
                "ESP32_C6_HYBRID_IPS_N16R8",
                "ESP32_C6_SOLAR_GATEWAY_02",
                "SMART_SOLAR_PLUG_X8"
            )
            kotlinx.coroutines.delay(1500)
            _isScanning.value = false
        }
    }

    fun pairDevice(deviceId: String, wifiSsid: String, wifiPass: String) {
        prefs.edit().apply {
            putBoolean("is_paired", true)
            putString("paired_device_id", deviceId)
            putString("wifi_ssid", wifiSsid)
            putString("wifi_pass", wifiPass)
            apply()
        }
        _isPaired.value = true
        _pairedDeviceId.value = deviceId
        // Update custom name automatically based on pairing
        saveSettings(_settings.value.copy(
            customSolarName = "Smart IoT ($deviceId)",
            deviceSerialNumber = "IPS-DSP-2026-" + deviceId.takeLast(3).uppercase()
        ))
    }

    fun unpairDevice() {
        prefs.edit().apply {
            putBoolean("is_paired", false)
            putString("paired_device_id", "")
            putString("wifi_ssid", "")
            putString("wifi_pass", "")
            apply()
        }
        _isPaired.value = false
        _pairedDeviceId.value = ""
        saveSettings(_settings.value.copy(customSolarName = "Unpaired Monitor Mode"))
    }

    // Configuration/Customization settings flow
    private val _settings = MutableStateFlow(CustomizerSettings())
    val settings: StateFlow<CustomizerSettings> = _settings.asStateFlow()

    // AI Chat State
    private val _aiResponse = MutableStateFlow("")
    val aiResponse: StateFlow<String> = _aiResponse.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _chatHistory = MutableStateFlow<List<Pair<String, Boolean>>>(emptyList()) // Pair of (Message, isUserMessage)
    val chatHistory: StateFlow<List<Pair<String, Boolean>>> = _chatHistory.asStateFlow()

    // Historical list
    val historicalSummaries = listOf(
        GridHistoricalSummary(
            dateLabel = "Today",
            maxPvPower = 211.0f,
            minPvPower = 0.0f,
            avgPvPower = 102.41f,
            maxPvVoltage = 18.80f,
            minPvVoltage = 0.0f,
            avgPvVoltage = 14.92f,
            maxPvAmpere = 14.06f,
            minPvAmpere = 0.0f,
            avgPvAmpere = 6.57f,
            totalBatteryChargeWh = 223.65f,
            totalBatteryChargeAh = 17.47f,
            maxBatteryChargeAmp = 12.42f,
            minBatteryChargeAmp = 0.0f,
            avgBatteryChargeAmp = 4.46f,
            maxDischargeW = 72.0f,
            minDischargeW = 25.0f,
            totalDischargeWh = 157.15f,
            loadFromPanelWh = 139.67f,
            loadFromBatteryWh = 17.48f,
            maxDischargeAmp = 5.21f,
            minDischargeAmp = 1.78f,
            avgDischargeAmp = 2.86f
        ),
        GridHistoricalSummary(
            dateLabel = "Yesterday",
            maxPvPower = 198.0f,
            minPvPower = 0.0f,
            avgPvPower = 109.48f,
            maxPvVoltage = 18.80f,
            minPvVoltage = 0.0f,
            avgPvVoltage = 14.26f,
            maxPvAmpere = 13.28f,
            minPvAmpere = 0.0f,
            avgPvAmpere = 7.25f,
            totalBatteryChargeWh = 328.73f,
            totalBatteryChargeAh = 25.68f,
            maxBatteryChargeAmp = 9.93f,
            minBatteryChargeAmp = 0.0f,
            avgBatteryChargeAmp = 4.77f,
            maxDischargeW = 70.0f,
            minDischargeW = 24.0f,
            totalDischargeWh = 242.82f,
            loadFromPanelWh = 206.77f,
            loadFromBatteryWh = 36.05f,
            maxDischargeAmp = 5.28f,
            minDischargeAmp = 1.80f,
            avgDischargeAmp = 3.27f
        ),
        GridHistoricalSummary(
            dateLabel = "Before yesterday",
            maxPvPower = 272.0f,
            minPvPower = 0.0f,
            avgPvPower = 51.92f,
            maxPvVoltage = 20.40f,
            minPvVoltage = 0.0f,
            avgPvVoltage = 3.80f,
            maxPvAmpere = 13.89f,
            minPvAmpere = 0.0f,
            avgPvAmpere = 2.68f,
            totalBatteryChargeWh = 103.38f,
            totalBatteryChargeAh = 8.08f,
            maxBatteryChargeAmp = 15.15f,
            minBatteryChargeAmp = 0.0f,
            avgBatteryChargeAmp = 2.88f,
            maxDischargeW = 60.0f,
            minDischargeW = 18.0f,
            totalDischargeWh = 166.67f,
            loadFromPanelWh = 66.12f,
            loadFromBatteryWh = 100.55f,
            maxDischargeAmp = 4.63f,
            minDischargeAmp = 1.37f,
            avgDischargeAmp = 3.49f
        )
    )

    init {
        loadSettings()
        resetMetrics()
        startPeriodicUpdate()
    }

    private fun startPeriodicUpdate() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(2500)
                val current = _metrics.value
                
                // Update last successful ESP32 data sync timestamp
                _lastSyncTime.value = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())

                // Slightly fluctuate parameters if not tripped
                if (!current.isAutoCutTripped) {
                    val voltShift = ((-2..2).random() * 0.1f)
                    val currShift = ((-2..2).random() * 0.05f)
                    
                    var newPanelVolt = (current.panelVoltage + voltShift).coerceIn(12f, 26f)
                    var newBatteryVolt = (current.batteryVoltage + voltShift * 0.3f).coerceIn(10.5f, 15.5f)
                    var newPanelCurr = (current.panelCurrent + currShift).coerceIn(0.2f, 15f)
                    
                    // Round values
                    newPanelVolt = ((newPanelVolt * 10).roundToInt() / 10.0f)
                    newBatteryVolt = ((newBatteryVolt * 10).roundToInt() / 10.0f)
                    newPanelCurr = ((newPanelCurr * 100).roundToInt() / 100.0f)
                    
                    val updated = current.copy(
                        panelVoltage = newPanelVolt,
                        panelCurrent = newPanelCurr,
                        batteryVoltage = newBatteryVolt
                    )
                    updateMetrics(updated)

                    // Check for low battery threshold to trigger Material3 alert system
                    if (newBatteryVolt < _settings.value.lowBatteryAlertLimit) {
                        val activeAlerts = _notifications.value
                        val hasLowBatteryAlert = activeAlerts.any { it.title.contains("Low Battery", ignoreCase = true) }
                        if (!hasLowBatteryAlert) {
                            triggerNotification(
                                title = "⚠️ Low Battery Alert",
                                message = "Battery voltage has dropped to ${newBatteryVolt}V, below your custom alert limit of ${_settings.value.lowBatteryAlertLimit}V. Connect charger immediately.",
                                severity = "critical"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun loadSettings() {
        var customSolarName = prefs.getString("solar_name", "Universal IPS & Solar System") ?: "Universal IPS & Solar System"
        if (customSolarName.contains("Mehedy", ignoreCase = true) || customSolarName.contains("Hasan", ignoreCase = true)) {
            customSolarName = "Universal IPS & Solar System"
            prefs.edit().putString("solar_name", customSolarName).apply()
        }
        val selectedAccentHex = prefs.getString("accent_hex", "#00E5FF") ?: "#00E5FF"
        val isCellVoltagesVisible = prefs.getBoolean("is_cell_visible", true)
        val isFlowDiagramVisible = prefs.getBoolean("is_flow_visible", true)
        val isSummaryGridVisible = prefs.getBoolean("is_summary_visible", true)
        val maxVoltageLimit = prefs.getFloat("max_voltage_limit", 24.0f)
        val maxCurrentLimit = prefs.getFloat("max_current_limit", 20.0f)
        val lowBatteryAlertLimit = prefs.getFloat("low_battery_limit", 11.5f)
        val maxBatteryChargeCurrent = prefs.getFloat("max_battery_charge_current", 40.0f)
        val inverterCutOffVoltage = prefs.getFloat("inverter_cut_off_voltage", 10.8f)
        
        val mqttBroker = prefs.getString("mqtt_broker", "broker.hivemq.com") ?: "broker.hivemq.com"
        val mqttPort = prefs.getInt("mqtt_port", 1883)
        var mqttPrefix = prefs.getString("mqtt_prefix", "smart/solar/solar01") ?: "smart/solar/solar01"
        if (mqttPrefix.contains("mehedy", ignoreCase = true)) {
            mqttPrefix = "smart/solar/solar01"
            prefs.edit().putString("mqtt_prefix", mqttPrefix).apply()
        }
        val mqttClient = prefs.getString("mqtt_client", "solar_monitor_mobile_01") ?: "solar_monitor_mobile_01"

        // DSP/IPS custom properties
        val deviceSerialNumber = prefs.getString("device_sn", "IPS-DSP-2026-X88") ?: "IPS-DSP-2026-X88"
        val installerName = prefs.getString("installer_name", "Dacca IPS Technologies") ?: "Dacca IPS Technologies"
        val installerPhone = prefs.getString("installer_phone", "+880 1712-345678") ?: "+880 1712-345678"
        val dspBaudRate = prefs.getInt("dsp_baud_rate", 9600)
        val dspModbusAddress = prefs.getInt("dsp_modbus_addr", 1)
        val ipsOutputRatingVa = prefs.getInt("ips_output_va", 1000)
        val batteryTypeSelected = prefs.getString("battery_type_selected", "Tall Tubular Lead-Acid") ?: "Tall Tubular Lead-Acid"

        _settings.value = CustomizerSettings(
            customSolarName = customSolarName,
            selectedAccentHex = selectedAccentHex,
            isCellVoltagesVisible = isCellVoltagesVisible,
            isFlowDiagramVisible = isFlowDiagramVisible,
            isSummaryGridVisible = isSummaryGridVisible,
            maxVoltageLimit = maxVoltageLimit,
            maxCurrentLimit = maxCurrentLimit,
            lowBatteryAlertLimit = lowBatteryAlertLimit,
            maxBatteryChargeCurrent = maxBatteryChargeCurrent,
            inverterCutOffVoltage = inverterCutOffVoltage,
            mqttBrokerUrl = mqttBroker,
            mqttPort = mqttPort,
            mqttTopicPrefix = mqttPrefix,
            mqttClientId = mqttClient,
            isMqttConnected = true,
            deviceSerialNumber = deviceSerialNumber,
            installerName = installerName,
            installerPhone = installerPhone,
            dspBaudRate = dspBaudRate,
            dspModbusAddress = dspModbusAddress,
            ipsOutputRatingVa = ipsOutputRatingVa,
            batteryTypeSelected = batteryTypeSelected
        )
    }

    fun saveSettings(updated: CustomizerSettings) {
        _settings.value = updated
        prefs.edit().apply {
            putString("solar_name", updated.customSolarName)
            putString("accent_hex", updated.selectedAccentHex)
            putBoolean("is_cell_visible", updated.isCellVoltagesVisible)
            putBoolean("is_flow_visible", updated.isFlowDiagramVisible)
            putBoolean("is_summary_visible", updated.isSummaryGridVisible)
            putFloat("max_voltage_limit", updated.maxVoltageLimit)
            putFloat("max_current_limit", updated.maxCurrentLimit)
            putFloat("low_battery_limit", updated.lowBatteryAlertLimit)
            putFloat("max_battery_charge_current", updated.maxBatteryChargeCurrent)
            putFloat("inverter_cut_off_voltage", updated.inverterCutOffVoltage)
            putString("mqtt_broker", updated.mqttBrokerUrl)
            putInt("mqtt_port", updated.mqttPort)
            putString("mqtt_prefix", updated.mqttTopicPrefix)
            putString("mqtt_client", updated.mqttClientId)
            
            putString("device_sn", updated.deviceSerialNumber)
            putString("installer_name", updated.installerName)
            putString("installer_phone", updated.installerPhone)
            putInt("dsp_baud_rate", updated.dspBaudRate)
            putInt("dsp_modbus_addr", updated.dspModbusAddress)
            putInt("ips_output_va", updated.ipsOutputRatingVa)
            putString("battery_type_selected", updated.batteryTypeSelected)
        }.apply()
    }

    // Resolves accent color to compose Color object
    fun getAccentColor(): Color {
        return when (_settings.value.selectedAccentHex.uppercase()) {
            "#FFA000" -> SolarGold
            "#00E676" -> SolarGreen
            "#FF1744" -> SolarRed
            else -> SolarCyan
        }
    }

    // Toggles for Home Dashboard Control Action
    fun toggleSolarPower() {
        val current = _metrics.value
        val newState = !current.isSolarOn
        updateMetrics(current.copy(isSolarOn = newState))
    }

    fun toggleBatteryCharging() {
        val current = _metrics.value
        val newState = !current.isChargingOn
        updateMetrics(current.copy(isChargingOn = newState))
    }

    fun toggleLoad() {
        val current = _metrics.value
        val newState = !current.isLoadOn
        updateMetrics(current.copy(isLoadOn = newState))
    }

    fun toggleGridPower() {
        val current = _metrics.value
        val newState = !current.isGridOn
        updateMetrics(current.copy(isGridOn = newState))
    }

    // Manual test injection triggers
    fun triggerOverVoltageFault() {
        val current = _metrics.value
        val faultVoltage = _settings.value.maxVoltageLimit + 1.5f
        updateMetrics(current.copy(batteryVoltage = faultVoltage, isOverVoltage = true))
    }

    fun triggerOverCurrentFault() {
        val current = _metrics.value
        val faultCurrent = _settings.value.maxCurrentLimit + 3.0f
        updateMetrics(current.copy(batteryCurrent = faultCurrent, isOverCurrent = true))
    }

    fun resetProtections() {
        val current = _metrics.value
        updateMetrics(current.copy(
            batteryVoltage = 12.6f,
            batteryCurrent = 1.2f,
            panelVoltage = 18.3f,
            panelCurrent = 1.53f,
            isOverVoltage = false,
            isLowVoltage = false,
            isOverCurrent = false,
            isLowCurrent = false,
            isAutoCutTripped = false,
            isLoadOn = true
        ))
    }

    /**
     * Adaptively recalculate metrics based on control states.
     * This creates a live-updating reactive simulation of the solar system's behavior.
     */
    private fun updateMetrics(newMetrics: SolarMetrics) {
        // Calculate interactive solar power
        val solarPower = if (newMetrics.isSolarOn) {
            newMetrics.panelVoltage * newMetrics.panelCurrent
        } else {
            0.0f
        }

        // Calculate load power requirements (If tripped, load is cut off!)
        val isLoadActuallyOn = newMetrics.isLoadOn && !newMetrics.isAutoCutTripped
        val loadPower = if (isLoadActuallyOn) {
            newMetrics.loadVoltage * newMetrics.loadCurrent
        } else {
            0.0f
        }

        // Determine battery state & flow and source shares
        var batteryCurrent = 0.0f
        var loadSource = "Solar"
        var loadPanelW = 0.0f
        var loadBatteryW = 0.0f
        var loadGridW = 0.0f

        if (newMetrics.isSolarOn && solarPower >= loadPower) {
            // Solar powers the load entirely and charges battery with leftover
            loadSource = "Solar"
            loadPanelW = loadPower
            loadBatteryW = 0.0f
            loadGridW = 0.0f

            if (newMetrics.isChargingOn) {
                val chargePower = solarPower - loadPower
                // Charge battery (positive charge current)
                batteryCurrent = chargePower / newMetrics.batteryVoltage
            }
        } else if (newMetrics.isSolarOn && solarPower > 0f) {
            // Solar is generating, but not enough for full load
            loadPanelW = solarPower
            val deficit = loadPower - solarPower

            if (newMetrics.batteryVoltage > 11.2f) {
                loadBatteryW = deficit
                loadSource = "Solar + Battery"
                batteryCurrent = -deficit / newMetrics.batteryVoltage
            } else if (newMetrics.isGridOn) {
                loadGridW = deficit
                loadSource = "Solar + Grid Bypass"
                batteryCurrent = 0.0f
            } else {
                loadBatteryW = deficit
                loadSource = "Solar + Battery (Low)"
                batteryCurrent = -deficit / newMetrics.batteryVoltage
            }
        } else {
            // Solar is OFF or 0W (night / disabled)
            loadPanelW = 0.0f
            if (newMetrics.isGridOn) {
                loadGridW = loadPower
                loadBatteryW = 0.0f
                loadSource = "AC Grid Bypass"
                if (newMetrics.isChargingOn && newMetrics.batteryPct < 95) {
                    batteryCurrent = 2.5f
                } else {
                    batteryCurrent = 0.0f
                }
            } else {
                loadBatteryW = loadPower
                loadGridW = 0.0f
                loadSource = "Battery Inverter"
                batteryCurrent = if (newMetrics.batteryVoltage > 0) -loadPower / newMetrics.batteryVoltage else 0f
            }
        }

        // Overwrite battery current if manually forced in a test fault
        val finalBatteryCurrent = if (newMetrics.isOverCurrent) newMetrics.batteryCurrent else batteryCurrent

        val batteryPower = finalBatteryCurrent * newMetrics.batteryVoltage

        // Calculate AC Grid real-time wattage and ampere
        val gridBaseStandby = if (newMetrics.isGridOn) 25.0f else 0.0f
        val gridChargeDrawW = if (newMetrics.isGridOn && batteryCurrent > 0 && !newMetrics.isSolarOn) (batteryCurrent * newMetrics.batteryVoltage * 1.15f) else 0.0f
        val calculatedGridPower = if (newMetrics.isGridOn) (gridBaseStandby + loadGridW + gridChargeDrawW) else 0.0f
        val calculatedGridCurrent = if (newMetrics.isGridOn && newMetrics.gridVoltage > 0) {
            calculatedGridPower / newMetrics.gridVoltage
        } else {
            0.0f
        }
        val batteryStatus = when {
            finalBatteryCurrent > 0.05f -> "Charging"
            finalBatteryCurrent < -0.05f -> "Discharging"
            else -> "Idle"
        }

        // Recalculate cell voltages slightly based on battery status
        val cellDiff = if (batteryStatus == "Charging") 0.05f else if (batteryStatus == "Discharging") -0.03f else 0.0f
        val calculatedCells = listOf(
            (3.152f + cellDiff * 0.9f).coerceIn(3.0f, 3.65f),
            (3.154f + cellDiff * 0.95f).coerceIn(3.0f, 3.65f),
            (3.150f + cellDiff * 1.05f).coerceIn(3.0f, 3.65f),
            (3.148f + cellDiff * 1.02f).coerceIn(3.0f, 3.65f)
        )

        // Run protective threshold check logic
        val isOverVolt = newMetrics.batteryVoltage > _settings.value.maxVoltageLimit || newMetrics.isOverVoltage
        val isLowVolt = newMetrics.batteryVoltage < _settings.value.lowBatteryAlertLimit || newMetrics.isLowVoltage
        val isOverCurr = kotlin.math.abs(finalBatteryCurrent) > _settings.value.maxCurrentLimit || newMetrics.isOverCurrent
        
        // Low Current state (e.g. if load is active but load draws less than 0.2A)
        val isLowCurr = isLoadActuallyOn && newMetrics.loadCurrent < 0.35f

        // System trips (Auto-Cut!) if Over-voltage, Low-voltage, or Over-current is true
        val autoCutTriggered = isOverVolt || isLowVolt || isOverCurr

        _metrics.value = newMetrics.copy(
            panelPower = ((solarPower * 100).roundToInt() / 100.0f),
            panelOutputPct = if (newMetrics.isSolarOn) 12 else 0,
            batteryCurrent = ((finalBatteryCurrent * 100).roundToInt() / 100.0f),
            batteryPower = ((batteryPower * 100).roundToInt() / 100.0f),
            loadPower = ((loadPower * 100).roundToInt() / 100.0f),
            loadPanelShareW = ((loadPanelW * 100).roundToInt() / 100.0f),
            loadBatteryShareW = ((loadBatteryW * 100).roundToInt() / 100.0f),
            loadGridShareW = ((loadGridW * 100).roundToInt() / 100.0f),
            gridPower = ((calculatedGridPower * 10).roundToInt() / 10.0f),
            gridCurrent = ((calculatedGridCurrent * 100).roundToInt() / 100.0f),
            loadSource = loadSource,
            cellVoltages = calculatedCells,
            
            // Set protections
            isOverVoltage = isOverVolt,
            isLowVoltage = isLowVolt,
            isOverCurrent = isOverCurr,
            isLowCurrent = isLowCurr,
            isAutoCutTripped = autoCutTriggered
        )
    }

    fun resetMetrics() {
        updateMetrics(SolarMetrics())
    }
}
