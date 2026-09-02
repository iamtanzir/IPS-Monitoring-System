# Project Custom Instructions & Hardware Configuration

## Official Hardware Controller: ESP32-C6 DevKit-C N16R8

All hardware schematics, Bill of Materials (BOM), pin assignments, and firmware telemetry configurations in this project are strictly designed around the **ESP32-C6 DevKit-C N16R8 Development Board**.

- **Procurement Source**: BDtronics
  - Product Page: [ESP32 C6 DevKit-C N16R8 ESP32 Development Board WiFi+Bluetooth](https://www.bdtronics.com/esp32-c6-devkit-c-n16r8-esp32-development-board-wifi-bluetooth.html)
  - Estimated Price: ~৳850 - ৳950 BDT

### Core Hardware Specifications
- **Microprocessor**: High-performance 32-bit RISC-V Single-Core processor running up to 160 MHz.
- **Memory**:
  - **16 MB Quad SPI Flash** (`N16`) for firmware, WebServer, OTA updates, and offline SPIFFS/LittleFS storage.
  - **8 MB PSRAM** (`R8`) for heavy buffers, MQTT message queues, and JSON telemetry parsing.
  - 512 KB internal SRAM and 320 KB ROM.
- **Wireless & IoT Connectivity**:
  - **Wi-Fi 6 (IEEE 802.11ax)** 2.4 GHz supporting Target Wake Time (TWT) for ultra-low power solar sleep modes, OFDMA, and legacy 802.11b/g/n.
  - **Bluetooth 5 (LE)** with Long Range (Coded PHY 125 Kbps / 500 Kbps) and 2 Mbps high speed for instant smartphone BLE provisioning.
  - **IEEE 802.15.4** radio supporting **Zigbee 3.0** and **Thread / Matter** home automation meshes.
- **I/O & Power**:
  - USB Type-C Port (integrated USB-to-UART / JTAG Serial).
  - 23 programmable GPIOs (GPIO 0 to 22/23).
  - 12-bit SAR ADC (ADC1 channels on GPIO 0 to 6).
  - 3.3V logic level (all analog/digital inputs MUST NOT exceed 3.3V).

---

### ESP32-C6 Pinout Matrix (Solar IPS Monitoring System)

| Component / Sensor | Function | ESP32-C6 Pin | Interface Type | Protective Circuitry |
| :--- | :--- | :--- | :--- | :--- |
| **ACS712 (20A/30A)** | Current Sensor (Panel/Load) | **GPIO 1** | Analog (ADC1_CH1) | 1kΩ / 2kΩ Voltage Divider (5V to 3.3V) |
| **DC 0-25V Sensor** | Voltage Divider (0-25V) | **GPIO 2** | Analog (ADC1_CH2) | 5:1 divider module (Max 25V in -> 3.3V out) |
| **DS18B20** | Temperature Probe | **GPIO 18** | Digital (1-Wire) | 4.7kΩ Pull-up resistor to 3.3V |
| **LCD1602 + PCF8574** | System LCD Display | **GPIO 6** (SDA) / **GPIO 7** (SCL) | I2C Bus | PCF8574 I2C Backpack |
| **BH1750** | Ambient Light / Lux Sensor | **GPIO 6** (SDA) / **GPIO 7** (SCL) | I2C Bus | Connected in parallel with LCD on I2C |
| **Onboard WS2812** | RGB Status Indicator | **GPIO 8** | Addressable RGB | Built-in on ESP32-C6 DevKit-C |
| **DSP Inverter UART** | RS485 / TTL Modbus | **GPIO 12** (RX) / **GPIO 13** (TX) | Hardware UART | Optocoupler or MAX485 transceiver |
| **LM2596 Buck Output** | 5V Regulated Power | **VIN (5V)** & **GND** | Power Rail | 5V input to onboard 3.3V LDO |

---

### Key Developer & Architecture Rules
1. **Always maintain ESP32-C6 references**: Do not revert to classic ESP32 (ESP-WROOM-32 / Xtensa dual core). All future UI views, BOM lists, connection guides, and telemetry tools must reflect the ESP32-C6 DevKit-C N16R8.
2. **Voltage Safety**: Remind users that ESP32-C6 GPIOs are 3.3V tolerant only. The ACS712 output requires the 1kΩ/2kΩ divider.
3. **Wi-Fi 6 Advantage**: Highlight Wi-Fi 6 (802.11ax) compatibility in network status cards and diagnostics.
