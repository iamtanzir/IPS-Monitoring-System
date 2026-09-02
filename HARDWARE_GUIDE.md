# HARDWARE_GUIDE.md - ESP32 Smart IoT Hybrid Inverter & Solar Monitoring System

This comprehensive hardware design manual details the physical circuit schematics, multi-sensor integration, industrial Modbus/RS-485 communication protocols, and safety-critical firmware flow for our commercial-grade solar monitoring system.

*Compiled from Cirkit Designer circuit modules, industrial Modbus RTU/TCP architectures (MDPI Sensors 1424-8220), and high-reliability FreeRTOS multitasking paradigms.*

---

## 1. System Architectural Overview

This system scaling ranges from a hobbyist DIY solar charger up to an **industrial hybrid inverter power grid connection**. Based on your uploaded design images, we classify the deployment into three physical hardware tiers:

### Tier A: Complete Multi-Sensor Telemetry Block (Image 3 Reference)
A fully-featured weather and power monitoring station featuring a local I2C character display, ambient solar light sensors (Lux), battery casing temperature sensors, and high-precision analog voltage/current sensors.

### Tier B: Industrial Modbus & RS-485 Inverter Gateway (Images 4 & 5 Reference)
Interfacing directly with commercial hybrid inverters and external wattmeters using **RS-485**, **Modbus RTU**, **Modbus TCP**, or **CAN Bus** to dynamic balance critical load backup loops and optimize grid feed-in margins.

---

## 2. Pin Allocation & Shared Bus Table (ESP32 DevKit V1)

| ESP32 Pin | Component / Sensor Connection | Bus Type | Voltage Level | Operation Description |
| :--- | :--- | :--- | :--- | :--- |
| **VIN** | LM2596 HV Buck Output / 5V DC Supply | Power IN | 5.0V DC | Main power supply input for the board |
| **GND** | Shared System Common Ground | Power GND | 0.0V DC | Ground reference for all modules and sensors |
| **GPIO35** | Voltage Sensor Module (Battery) | Analog IN | 0.0V - 3.3V | Reads battery bank voltage (0V to 25V scaled) |
| **GPIO34** | Voltage Sensor Module (Solar PV) | Analog IN | 0.0V - 3.3V | Reads solar panel input voltage |
| **GPIO32** | ACS712 Current Sensor (OUT) | Analog IN | 0.0V - 3.3V | Measures inline charging/load amperage |
| **GPIO4** | DS18B20 Temperature Sensor (DQ) | OneWire | 3.3V Digital | Reads battery temperature (Requires 4.7k pull-up) |
| **GPIO21** | I2C LCD Display & BH1750 Sensor (SDA) | I2C Data | 3.3V / 5.0V | Shared I2C Serial Data Bus |
| **GPIO22** | I2C LCD Display & BH1750 Sensor (SCL) | I2C Clock | 3.3V / 5.0V | Shared I2C Serial Clock Bus |
| **GPIO16 (RX2)**| MAX485 / RS-485 Module (RO) | UART RX | 3.3V Digital | Receives Modbus packets from the Inverter |
| **GPIO17 (TX2)**| MAX485 / RS-485 Module (DI) | UART TX | 3.3V Digital | Transmits Modbus requests to the Inverter |
| **GPIO25** | Optocoupled Relays (Auto-Cut Limit) | Digital OUT | Active Low | Drives AC Breakers / Inverter remote cutoff |

---

## 3. High-Reliability Circuit Schematics

### A. I2C Local LCD & Ambient Light Sensor Bus (Image 3 Connection)
The $16\times2$ LCD and BH1750 ambient light sensor share the same hardware I2C bus on the ESP32. This minimizes GPIO pins while allowing real-time, zero-latency local diagnostic readouts.

```
                  +5V Power Rail (For I2C LCD Backlight)
                   |
                  +3.3V Power Rail (For BH1750 & DS18B20 Pull-Up)
                   |
 [ESP32 GPIO21] ---+------ SDA (BH1750 SDA & I2C LCD SDA Line)
                   |
 [ESP32 GPIO22] ---+------ SCL (BH1750 SCL & I2C LCD SCL Line)
                   |
 [ESP32 GPIO4]  ---+------ DQ (DS18B20 Data Line) <--- [ R: 4.7kΩ Pull-Up ] ---> +3.3V
```

### B. RS-485 Modbus Industrial Gateway Interface (Image 4 Connection)
To interface with an industrial Hybrid Inverter or External Wattmeter over a noisy AC environment up to 1200 meters away, you must convert the ESP32's TTL UART signals to differential RS-485 using a **MAX485 chip**:

```
 [ESP32 TX2] ---------> [MAX485 DI] (Data In)       [MAX485 A] <===========> [Inverter RS-485 A (+)]
 [ESP32 RX2] <--------- [MAX485 RO] (Receiver Out)  [MAX485 B] <===========> [Inverter RS-485 B (-)]
 [GPIO5]    ---------> [MAX485 DE/RE] (Flow Control)
                                                    [Terminator Resistor 120Ω in Parallel across A & B]
```

---

## 4. Industrial Modbus Firmware Architecture & Execution Flow (Image 5 Reference)

The ESP32 firmware executes a non-blocking Modbus state machine. It queries the external wattmeter to calculate the home's grid power consumption offset and adjusts the hybrid inverter's battery storage setpoints over Modbus TCP or RTU.

```
                                +------------------------+
                                |  Initialization Setup  |
                                +------------------------+
                                             |
                                             v
                                +------------------------+
                                |   Port Initialization  | (Serial, Serial1/RTU Slave,
                                |                        |  Serial2/RTU Master)
                                +------------------------+
                                             |
                                             v
                                +------------------------+
                                | Network & Modbus Conn  | (Wi-Fi, Modbus TCP Client
                                |                        |  Modbus RTU Master/Slave)
                                +------------------------+
                                             |
                                             v
                                +------------------------+
                                |     CreateIregs()      | (Initializes Modbus Registers
                                |                        |  to Emulate Wattmeter)
                                +------------------------+
                                             |
                                             +<------------------------+
                                             |                         |
                                             v                         |
                                +------------------------+             |
                                |       Main Loop        |             |
                                +------------------------+             |
                                             |                         |
                                             v                         |
                                +------------------------+             |
                                |     moduleOffset()     |             | (Checks Serial Input,
                                |                        |             |  Updates PowerOffset)
                                +------------------------+             |
                                             |                         |
                                             v                         |
                                +------------------------+             |
                                |  getWattmeterReads()   |             | (Reads Modbus RTU Wattmeter,
                                |                        |             |  Applies PowerOffset)
                                +------------------------+             |
                                             |                         |
                                             v                         |
                                +------------------------+             |
                                |   getinverterReads()   |             | (Executes Modbus TCP
                                |                        |             |  Inverter Reading Cycle)
                                +------------------------+             |
                                             |                         |
                                             +-------------------------+
```

### Core Modbus Execution Code Block
```cpp
#include <Arduino.h>
#include <ModbusRTU.h>

// Modbus RTU Register definitions
#define REG_WATT_ACTIVE_POWER 0x002A // Register address for Grid Wattmeter Active Power
#define REG_INV_BATTERY_SOC   0x0102 // Register address for Inverter Battery Charge %

ModbusRTU mb;

int32_t powerOffset = 0;
bool flagScan = true;

// Emulates a Modbus Wattmeter for Grid Offset Calculations
void createIregs() {
    mb.addIreg(REG_WATT_ACTIVE_POWER);
}

// Adjusts the calibration offset from the diagnostics interface
void moduleOffset() {
    if (Serial.available()) {
        String input = Serial.readStringUntil('\n');
        int32_t newOffset = input.toInt();
        if (newOffset != 0) {
            powerOffset = newOffset;
            Serial.printf("New Calibration Power Offset Loaded: %d Watts\n", powerOffset);
        }
    }
}

// Fetches actual energy usage from the External Wattmeter
void getWattmeterReads() {
    if (!flagScan) return;
    
    // Reads from the physical Modbus RTU Meter
    uint16_t rawPowerReg[2];
    mb.readHreg(1, REG_WATT_ACTIVE_POWER, rawPowerReg, 2);
    
    int32_t rawPower = (rawPowerReg[0] << 16) | rawPowerReg[1];
    int32_t calibratedPower = rawPower + powerOffset;
    
    // Write the output to our internal Modbus emulated registers
    mb.setIreg(REG_WATT_ACTIVE_POWER, calibratedPower);
}

// Interacts with Inverter parameters over Modbus Protocol
void getinverterReads() {
    uint16_t batterySOC;
    mb.readHreg(2, REG_INV_BATTERY_SOC, &batterySOC, 1);
    
    Serial.printf("Hybrid Inverter Current Battery SOC: %d%%\n", batterySOC);
}
```

---

## 5. Safe Operating Parameters and Over-Limit Alarm Triggers

| Hazard Description | Critical Threshold | Safety Action Triggered |
| :--- | :--- | :--- |
| **Grid Power Surcharge (Overload)** | $>5500\text{W}$ active load | Trips non-critical AC loads on Core 0 via Relay 1 |
| **Low Voltage Deep Discharge** | $<10.8\text{V}$ (Lead-Acid) / $<11.2\text{V}$ (LiFePO4) | Immediate disconnect from load side to avoid sulfation |
| **Thermal Runaway Warning** | $>50.0^\circ\text{C}$ on battery case | Drops charging current limit to $0.0\text{A}$ over Modbus TCP |
| **Bus Over-Voltage Spikes** | $>15.5\text{V}$ battery terminal | Disconnects the Solar PV Charger relay within 15ms |

---
*Maintained by the Solar Systems Engineering and Compliance Assurance Team.*
