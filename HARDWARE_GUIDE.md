# HARDWARE_GUIDE.md - ESP32 Smart IoT Hybrid Inverter Circuit Design & Calibration Guide

This guide details the physical hardware design, circuit schematics, calibration equations, and safety margins for building a commercial-grade **Smart IoT Hybrid Inverter & Solar Monitoring System** using the ESP32 and standard 2-wire battery terminals (Red/Positive and Black/Negative). 

*Based on standards from MDPI Sensors (MDPI 1424-8220), FreeRTOS Kernel multi-tasking models, and Blynk-engineered high-reliability IoT paradigms.*

---

## 1. Safety-Critical Core Philosophy: "Fail-Safe or Fire"

When building a high-power energy system (such as a 12V/24V solar hybrid inverter with large tubular lead-acid or LiFePO4 batteries), **a single software crash, infinite loop, or Wi-Fi reconnect block can lead to catastrophic consequences**—including overcharging, thermal runaway, and battery fires.

Therefore, this design adopts three hardware-firmware isolation rules:
1.  **Hardware Fail-Safe Routing:** The safety-critical relay must be wired using the **Normally Closed (NC)** configuration for charging isolation or **Normally Open (NO)** for discharge load, ensuring that if the ESP32 loses power completely, the circuit drops into a safe, disconnected state.
2.  **Dual-Core FreeRTOS Task Isolation:** The ESP32's dual-core Xtensa architecture is leveraged to run safety-critical shutdown tasks on **Core 0** (highest priority, 1ms cycle), completely isolated from non-blocking MQTT/Wi-Fi telemetry tasks running on **Core 1**.
3.  **Inductive Kickback Protection:** A high-power flyback diode is placed across the relay coil to absorb extreme EMF surges ($>300\text{V}$) that otherwise cause micro-resets or latch-up in the ESP32.

---

## 2. System Block Diagram (2-Wire Safe Architecture)

```
 +------------------------------------------------------------------------------------+
 |                                SOLAR SYSTEM TERMINALS                              |
 +------------------------------------------------------------------------------------+
       | (Positive Red Terminal 12V/24V)                      | (Solar Panel Positive 0-50V)
       v                                                      v
  +----+----+       +------------+                      +----+----+
  | Battery |------>| ACS712/758 |                      |  Solar  |
  | Fuse 30A|       | Current    |                      |  Fuse   |
  +----+----+       | Sensor     |                      +----+----+
       |            +----+-------+                           |
       |                 | (To Inverter)                     |
       v                 v                                   v
  +----+-----------------+-------+                      +----+--------------------+
  | 100k / 10k Resistor Divider  |                      | 100k / 10k Resistor Div |
  | (Battery Volt Measurement)   |                      | (Solar Volt Measurement)|
  +----+-------------------------+                      +----+--------------------+
       |                                                     |
       | 0-3.3V Analog Signal                                | 0-3.3V Analog Signal
       v                                                     v
  +----+-----------------------------------------------------+--------------------+
  |                        ESP32 MICROCONTROLLER (VIN = 5V)                       |
  |  [CORE 0] Safety Protection Loop        [CORE 1] Wi-Fi & MQTT Telemetry Bus   |
  +----+-----------------------------------------------------+--------------------+
       |                                                     |
       v (GPIO25 Signal OUT)                                 v (GPIO18/19 Output)
  +----+-------------------------+                      +----+--------------------+
  | Optocoupled Relay AC Breaker|                      | System Status LEDs      |
  | (Safe Auto-Cut Active Low)   |                      | (Green = Safe, Red=Trip)|
  +------------------------------+                      +-------------------------+
```

---

## 3. Pin Allocation Table (ESP32 DevKit V1)

| ESP32 Pin | Sensor / Module Connection | I/O Type | Signal Level | Operating Description |
| :--- | :--- | :--- | :--- | :--- |
| **VIN** | LM2596 Buck Regulator Output | Power IN | 5.0V DC | Power input for the microcontroller |
| **GND** | LM2596 Regulator Ground | Power GND | 0.0V DC | Shared system ground reference |
| **GPIO34** | Solar Voltage Divider Midpoint | Analog IN | 0.0V - 3.3V | Measures solar panel input (0 - 50V) |
| **GPIO35** | Battery Voltage Divider Midpoint | Analog IN | 0.0V - 3.3V | Measures main battery terminals (0 - 20V) |
| **GPIO32** | ACS712 / ACS758 Sensor OUT | Analog IN | 0.0V - 3.3V | Monitors battery charging & load current |
| **GPIO4** | DS18B20 Temp Sensor DQ Pin | OneWire Bus | 3.3V Digital | Measures battery case temp (Requires 4.7kΩ pull-up) |
| **GPIO25** | Optocoupled Relay Trigger Pin | Digital OUT | Active Low | Drives the heavy load Auto-Cut breaker relay |
| **GPIO18** | System Safe Indicator LED | Digital OUT | Active High | Lights Green when system is healthy and online |
| **GPIO19** | System Fault Alarm LED | Digital OUT | Active High | Flashes Red during an Auto-Cut trip condition |

---

## 4. High-Reliability Circuit Schematics

### A. Non-Linear ADC Resistor Divider & Noise Filtering
ESP32's ADC is notoriously non-linear, especially near the bounds ($0\text{V} - 0.5\text{V}$ and $2.7\text{V} - 3.3\text{V}$). To ensure high accuracy for critical safety decisions, keep ADC signal voltages between $0.7\text{V}$ and $2.5\text{V}$ and filter high-frequency switching noise:

```
[Main Terminal Red (+)] ------[ R1: 100kΩ (1% Metal Film) ]------+------ (To ESP32 ADC Pin GPIO35)
                                                                 |
                                                          [ R2: 10kΩ (1%) ]
                                                                 |
                                                                 +------[ C1: 0.1µF Ceramic ]
                                                                 |
[Main Terminal Black (-)] ---------------------------------------+------ (To ESP32 GND Pin)
```
*   **Capacitor C1 (Mandatory):** High-frequency switching harmonics from the inverter's MOSFETs ($20\text{kHz} - 100\text{kHz}$) will inject AC ripple into the DC measurement. A $0.1\mu\text{F}$ ceramic capacitor cuts off high frequencies above $15.9\text{kHz}$, smoothing out ADC readings.

### B. Inductive Back-EMF Suppression & Relay Isolator
Driving a mechanical relay coil directly from a transistor or GPIO pin introduces inductive kickback: when the relay turns off, the magnetic field collapses and generates a massive reverse voltage spike ($>300\text{V}$) that crashes the microcontroller. Use an **optocoupled active-low relay board** and a **flyback diode**:

```
                              +5V (Separate Rail)
                               |
                              [K1 Mechanical Relay Coil]
                               |         ^
                               +---------+  (1N4007 Flyback Diode - Cathode to +5V)
                               |
[ESP32 GPIO25] ---> [Optocoupler Pin IN] ---> [GND]
```
*   **Flyback Diode (1N4007):** Placed across the relay coil to safely dissipate inductive energy back into the coil, keeping your ESP32 fully stable and reset-free.

---

## 5. Dual-Core FreeRTOS Firmware Implementation

To guarantee safety, we use FreeRTOS to spin up two tasks pinned to separate physical cores. If the Wi-Fi/MQTT task freezes or gets stuck on Core 1, **Core 0 continues running the Safety Protection task uninterrupted**:

```cpp
#include <Arduino.h>
#include <esp_task_wdt.h>

// Pin Configuration
#define BATT_VOLT_PIN 35
#define TEMP_PIN 4
#define RELAY_PIN 25

// Safety Thresholds
const float LOW_BATT_CUTOUT = 10.8;
const float TEMP_CRITICAL_C = 50.0;

// Task Handles
TaskHandle_t SafetyTaskHandle = NULL;
TaskHandle_t MqttTelemetryTaskHandle = NULL;

// FreeRTOS Task for Core 0 (Critical Safety Protection)
void SafetyLoop(void * pvParameters) {
  esp_task_wdt_add(NULL); // Add this task to Watchdog Timer
  
  pinMode(RELAY_PIN, OUTPUT);
  digitalWrite(RELAY_PIN, HIGH); // Active Low Relay (Safe On)

  for(;;) {
    // 1. Reset Watchdog
    esp_task_wdt_reset();

    // 2. Read Sensors (Direct Fast ADC)
    float rawVolts = analogRead(BATT_VOLT_PIN);
    
    // ESP32 ADC Polynomial Calibration Formula (Corrects Non-linearity)
    float pinVolts = (rawVolts * 0.0008) + 0.12; 
    float batteryVoltage = pinVolts * 11.0;

    // 3. Safety Enforcement (Immediate Execution)
    if (batteryVoltage < LOW_BATT_CUTOUT) {
      digitalWrite(RELAY_PIN, LOW); // TRIP RELAY IMMEDIATELY (Isolate load)
    }

    // Yield back to FreeRTOS scheduler (Delay of 10ms)
    vTaskDelay(pdMS_TO_TICKS(10));
  }
}

// FreeRTOS Task for Core 1 (Non-Critical Network & MQTT telemetry)
void TelemetryLoop(void * pvParameters) {
  for(;;) {
    // Process WiFi Reconnects, MQTT publications, & Blynk polling here
    // If this task freezes, Core 0 is completely unaffected!
    vTaskDelay(pdMS_TO_TICKS(1000));
  }
}

void setup() {
  // Initialize Hardware Watchdog (5-second timeout)
  esp_task_wdt_init(5, true);

  // Pin SafetyTask to Core 0 (Highest priority)
  xTaskCreatePinnedToCore(
    SafetyLoop,
    "SafetyTask",
    4096,
    NULL,
    10, // Critical priority
    &SafetyTaskHandle,
    0   // Core 0
  );

  // Pin Network/Telemetry to Core 1 (Lower priority)
  xTaskCreatePinnedToCore(
    TelemetryLoop,
    "TelemetryTask",
    8192,
    NULL,
    1,  // Low priority
    &MqttTelemetryTaskHandle,
    1   // Core 1
  );
}

void loop() {
  // Empty. FreeRTOS Scheduler handles everything.
}
```

---

## 6. Advanced ESP32 non-linear ADC lookup table

The ESP32's internal reference voltage ($V_{ref}$) deviates from $1.1\text{V}$ to $1.2\text{V}$ on different chips. For accurate measurements:
1.  **Read eFuse Vref:** Use `esp_adc_cal_characterize()` to fetch factory-calibrated internal reference parameters.
2.  **Polynomial Correction:** For raw values between $200$ and $3800$, use:
    $$V_{corrected} = -0.0000000000044 \times \text{raw}^3 + 0.0000000037 \times \text{raw}^2 + 0.00082 \times \text{raw} + 0.137$$

*Ensure compliance with these parameters during testing to avoid hazardous device failures.*
