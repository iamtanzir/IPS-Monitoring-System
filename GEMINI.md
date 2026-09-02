# Project Instructions & Hardware Specifications

Official Microcontroller Platform: **ESP32-C6 DevKit-C N16R8 (WiFi 6 + Bluetooth 5 + Zigbee/Thread)**
Source: [BDtronics ESP32-C6 DevKit-C N16R8](https://www.bdtronics.com/esp32-c6-devkit-c-n16r8-esp32-development-board-wifi-bluetooth.html)

Hardware Specs:
- RISC-V 32-bit Single-Core @ 160MHz
- 16MB Flash (N16) + 8MB PSRAM (R8)
- Wi-Fi 6 (802.11ax), Bluetooth 5 LE, Zigbee 3.0 & Thread
- USB Type-C interface

Pin Assignment for Solar IPS Project:
- Current Sensor (ACS712): `GPIO1` (ADC1_CH1 with 1k/2k divider)
- Voltage Sensor (0-25V): `GPIO2` (ADC1_CH2)
- Temperature Sensor (DS18B20): `GPIO18` (1-Wire with 4.7k pull-up)
- I2C Bus (LCD1602 + BH1750): `GPIO6` (SDA) / `GPIO7` (SCL)
- Status RGB LED: `GPIO8` (Onboard WS2812)
- Power Input: `VIN (5V)` and `GND` from LM2596 Buck Converter
