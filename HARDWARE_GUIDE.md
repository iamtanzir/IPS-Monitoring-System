# HARDWARE_GUIDE.md - ESP32 Smart IoT Hybrid Inverter Circuit Design & Calibration Guide

This guide details the physical hardware design, circuit schematics, calibration equations, and safety margins for building your commercial-grade **Smart IoT Hybrid Inverter & Solar Monitoring System** using the ESP32 and standard 2-wire battery terminals (Red/Positive and Black/Negative).

---

## 1. System Block Diagram (2-Wire Safe Architecture)

Unlike complex BMS units that require multi-wire balancing harnesses, this commercial-grade design operates purely off the standard high-power **Red (+)** and **Black (-)** power cables. This makes it universal, extremely safe, and easy to package as a retrofitted plug-and-play accessory for any existing inverter or IPS.

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
  +----+--------------------------------------------------------------------------+
       |                                                     |
       v (GPIO25 Signal OUT)                                 v (GPIO18/19 Output)
  +----+-------------------------+                      +----+--------------------+
  | Optocoupled Relay AC Breaker|                      | System Status LEDs      |
  | (Safe Auto-Cut Active Low)   |                      | (Green = Safe, Red=Trip)|
  +------------------------------+                      +-------------------------+
```

---

## 2. Pin Allocation Table (ESP32 DevKit V1)

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

## 3. Circuit Schematics & Sensor Signal Conditioning

### A. Precise Voltage Sensor Circuit (Resistor Divider)
To measure a battery voltage (up to 20V) or solar panel voltage (up to 50V) safely with the ESP32 (which only accepts 0V - 3.3V on its ADC pins), you must scale down the voltage using a high-precision resistor network:

```
[Main Terminal Red (+)] ------[ R1: 100kΩ (1%) ]------+------ (To ESP32 ADC Pin)
                                                      |
                                               [ R2: 10kΩ (1%) ]
                                                      |
                                                      +------[ C1: 0.1µF Ceramic ]
                                                      |
[Main Terminal Black (-)] ----------------------------+------ (To ESP32 GND Pin)
```
*   **Voltage Division Ratio:** $V_{out} = V_{in} \times \frac{R_2}{R_1 + R_2} = V_{in} \times \frac{10}{100 + 10} = \frac{V_{in}}{11}$
*   **Filter Capacitor (C1):** A `0.1µF` ceramic capacitor placed in parallel with $R_2$ is **strictly mandatory** to filter out high-frequency EMI generated by the inverter's power MOSFETs. Without this, your ADC readings will fluctuate erratically.

---

## 4. Calibration Math and ADC Equations

To translate the raw values read by the ESP32’s 12-bit ADC (values ranging from 0 to 4095) into physical volts and amperes, use these equations in your firmware:

### 1. Voltage Calculation
$$V_{terminal} = \left( \frac{\text{ADC\_Read}}{4095} \times 3.3\,\text{V} \right) \times 11.0$$
*   **Software Tuning:** Due to physical tolerances in resistor values, measure the terminal with a digital multimeter and adjust the multiplier `11.0` slightly (e.g., to `11.04` or `10.96`) in code to match the physical meter.

### 2. ACS712-30B Current Sensor Calibration
The sensor output voltage rests at $1.65\,\text{V}$ (exactly half of its $3.3\,\text{V}$ logic scale) when zero current is flowing.
*   **Current Sensitivity:** $66\,\text{mV}$ per Ampere ($0.066\,\text{V/A}$).
*   **Calibration Formula:**
$$\text{Current (Amps)} = \frac{V_{sensor\_out} - 1.65\,\text{V}}{0.066}$$

---

## 5. Safe Operating Parameters and Alarm Limits

Configure your protective threshold settings to match these industrial limits depending on the battery type:

| Parameter | Tall Tubular Lead-Acid | Lithium Iron Phosphate (LiFePO4) | Protection Trigger Action |
| :--- | :--- | :--- | :--- |
| **Bulk Charge Voltage Limit** | 14.4V | 14.6V | High-volt cutout, stops solar charger |
| **Low-Battery Alert Trigger** | 11.2V | 11.8V | Sends low-battery alert notification to the app |
| **Low-Battery Disconnect Cutout** | 10.8V | 11.2V | Tripping Relay opens, isolates battery from discharge |
| **Over-Current Safety Margin** | 20.0A | 30.0A | Trips relay breaker within 50ms to prevent fire |
| **Battery Over-Temperature Trip** | 50.0°C | 55.0°C | Shuts down inverter to protect cell chemistry |

---
*Prepared for commercial production and quality-assured compliance testing.*
