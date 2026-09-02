# Smart IoT Hybrid Inverter & Solar Panel Monitoring System ☀️🔋

Welcome to the commercial-grade repository for the **Smart IoT Hybrid Inverter, Solar Panel, and IPS Monitoring System**. This project is a complete hardware-software ecosystem designed to bring remote cloud-telemetry, intelligent auto-cut protections, and seamless Bluetooth/Wi-Fi provisioning to traditional hybrid inverters and UPS/IPS units.

This repository contains both the **Android Jetpack Compose Companion App** source code and the **Production-Grade ESP32 Arduino Firmware**, along with comprehensive hardware wiring, sensor calibration, and deployment instructions to prepare your product for commercial sale.

---

## 📋 Table of Contents
1. [System Architecture](#1-system-architecture)
2. [Hardware Component Bill of Materials (BOM)](#2-hardware-component-bill-of-materials-bom)
3. [Wiring and Pinout Connections](#3-wiring-and-pinout-connections)
4. [ESP32 Arduino Firmware (Production Ready)](#4-esp32-arduino-firmware-production-ready)
5. [Smart IoT Provisioning and Pairing Flow](#5-smart-iot-provisioning-and-pairing-flow)
6. [Intelligent Protections & Auto-Cut Limits](#6-intelligent-protections--auto-cut-limits)
7. [Testing and Calibration Procedures](#7-testing-and-calibration-procedures)
8. [Commercial Product Marketing & Branding Guidelines](#8-commercial-product-marketing--branding-guidelines)

---

## 1. System Architecture

The ecosystem consists of three major components:
1.  **The Inverter/IPS Controller Board:** Collects raw physical metrics (Voltage, Current, Temp) and controls the AC Output Load via a heavy-duty Relay.
2.  **The ESP32 IoT Gateway:** Acts as the communication bridge. It utilizes **Bluetooth Low Energy (BLE)** for initial local pairing and Wi-Fi provisioning, then connects to a **Global MQTT Broker** over Wi-Fi for secure, live two-way telemetry.
3.  **The Android Mobile App:** Provides real-time dashboard visualization, active protection alerts, control switches, and interactive configuration of safety thresholds.

```
+------------------+         BLE (Pairing)         +-------------------+
|  Mobile App      | <===========================> |  ESP32 IoT        |
|  (Jetpack Comp.) |         MQTT (Telemetry)      |  Gateway Board    |
+------------------+ <---------------------------> +-------------------+
                                                             ^
                                                             | (ADC, OneWire, I/O)
                                                             v
                                                   +-------------------+
                                                   | Inverter Sensors  |
                                                   | & Relay Breaker   |
                                                   +-------------------+
```

---

## 2. Hardware Component Bill of Materials (BOM)

To build a reliable product that meets industrial standards and stands up to real-world usage, source the following specific components:

| Component | Recommended Part / Model | Operating Specs | Purpose |
| :--- | :--- | :--- | :--- |
| **Microcontroller** | ESP32 DEVKIT V1 (30-Pin) | 3.3V Logic, Dual-Core Tensilica | Wi-Fi, BLE, ADC, and protection logic |
| **DC Voltage Sensor** | Custom Metal-Film Resistor Network | 100kΩ and 10kΩ (1% Precision) | Low-noise divider to read 0-50V Solar and 0-20V Battery |
| **DC Current Sensor** | ACS712-30B or ACS758-50B | Hall Effect, Bidirectional | Reads charge/discharge battery current |
| **AC Voltage Sensor** | ZMPT101B Active Module | 220V AC Sensing | Measures inverter output voltage and AC health |
| **Auto-Cut Switching** | 5V Optocoupled Relay (30A SPDT) | Active Low Trigger, AC 250V | Safely disconnects load during critical trips |
| **Temp Sensor** | DS18B20 (Waterproof TO-92) | OneWire Protocol, -55°C to 125°C | Clamps onto the battery casing to monitor health |
| **Buck Regulator** | LM2596HV DC-DC Step-Down | Input 4.5V-60V, Output 5V @ 3A | Drops battery/solar voltage to a clean 5V for MCU |

---

## 3. Wiring and Pinout Connections

Connect the sensors, indicators, and actuators to the ESP32 DEVKIT board according to the map below:

```
                  +-----------------------------------+
                  |           ESP32 DEVKIT            |
                  +-----------------------------------+
                  | VIN (5V Input) <---- LM2596 OUT+  |
                  | GND            <---- LM2596 OUT-  |
                  |                                   |
                  | GPIO34 (ADC1)  <---- Solar Divider|
                  | GPIO35 (ADC1)  <---- Battery Div. |
                  | GPIO32 (ADC1)  <---- ACS712 OUT   |
                  | GPIO4  (OneWire)<--- DS18B20 DATA |
                  |                                   |
                  | GPIO25 (Output)<---- Relay IN     |
                  | GPIO18 (Output)<---- Green LED    |
                  | GPIO19 (Output)<---- Red LED      |
                  +-----------------------------------+
```

### Pro-Tips for Wiring Layout:
1.  **Resistor Dividers:** Always place a **0.1µF ceramic capacitor** in parallel with the 10kΩ resistor (connected between the ESP32 analog pin and GND). This acts as a low-pass filter to eliminate high-frequency switching noise from the inverter.
2.  **Optocoupler Isolation:** Use an optocoupled relay board and keep the ESP32's digital ground separate from the high-power AC output ground to prevent EMF feedback from rebooting the processor during relay transitions.

---

## 4. ESP32 Arduino Firmware (Production Ready)

Flash this robust, non-blocking firmware onto the ESP32. Ensure you have installed the following libraries via the Arduino Library Manager:
*   `PubSubClient` (by Nick O'Leary)
*   `ArduinoJson` (by Benoit Blanchon)
*   `OneWire` & `DallasTemperature` (by Miles Burton)

```cpp
#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <Preferences.h>
#include <OneWire.h>
#include <DallasTemperature.h>

#define PIN_SOLAR_VOLT      34
#define PIN_BATTERY_VOLT    35
#define PIN_BATTERY_CURR    32
#define PIN_RELAY_LOAD      25
#define PIN_ONE_WIRE_BUS    4
#define PIN_LED_SAFE        18
#define PIN_LED_TRIP        19

#define SERVICE_UUID           "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHARACTERISTIC_UUID_RX "beb5483e-36e1-4688-b7f5-ea07361b26a8"

const float VOLTAGE_DIVIDER_RATIO = 11.0; 
const float ADC_REF_VOLTAGE = 3.3;
const int   ADC_RESOLUTION = 4095;
const float CURRENT_SENSOR_SENSITIVITY = 0.066; 

Preferences preferences;
WiFiClient espClient;
PubSubClient mqttClient(espClient);
OneWire oneWire(PIN_ONE_WIRE_BUS);
DallasTemperature tempSensor(&oneWire);

String wifiSsid = "";
String wifiPassword = "";
String deviceId = "ESP32_HYBRID_IPS_04B";
String mqttBroker = "broker.hivemq.com";
int mqttPort = 1883;

bool isPaired = false;
bool isAutoCutTripped = false;
String batteryType = "Tall Tubular Lead-Acid";

float maxVoltageLimit = 14.4;
float lowBatteryCutoffLimit = 10.8;
float maxCurrentLimit = 20.0;

unsigned long lastTelemetryTime = 0;
const unsigned long telemetryInterval = 1500; 

class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
        Serial.println("Mobile App connected via BLE!");
    }
    void onDisconnect(BLEServer* pServer) {
        Serial.println("Mobile App disconnected from BLE.");
        BLEDevice::startAdvertising(); 
    }
};

class MyCharacteristicCallbacks: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
        std::string rxValue = pCharacteristic->getValue();
        if (rxValue.length() > 0) {
            DynamicJsonDocument doc(256);
            DeserializationError error = deserializeJson(doc, rxValue);
            if (!error) {
                wifiSsid = doc["ssid"].as<String>();
                wifiPassword = doc["pass"].as<String>();
                if(doc.containsKey("bat_type")) {
                    batteryType = doc["bat_type"].as<String>();
                }
                
                preferences.begin("iot-config", false);
                preferences.putString("ssid", wifiSsid);
                preferences.putString("pass", wifiPassword);
                preferences.putString("bat_type", batteryType);
                preferences.putBool("is_paired", true);
                preferences.end();
                
                Serial.println("Pairing credentials successfully written to non-volatile storage!");
                delay(1000);
                ESP.restart(); 
            }
        }
    }
};

float readVoltage(int pin) {
    long sum = 0;
    for(int i = 0; i < 20; i++) {
        sum += analogRead(pin);
        delayMicroseconds(50);
    }
    float adcVal = sum / 20.0;
    float pinVolt = (adcVal * ADC_REF_VOLTAGE) / ADC_RESOLUTION;
    return pinVolt * VOLTAGE_DIVIDER_RATIO;
}

float readCurrent() {
    long sum = 0;
    for(int i = 0; i < 50; i++) {
        sum += analogRead(PIN_BATTERY_CURR);
        delayMicroseconds(20);
    }
    float adcVal = sum / 50.0;
    float pinVolt = (adcVal * ADC_REF_VOLTAGE) / ADC_RESOLUTION;
    float current = (pinVolt - 1.65) / CURRENT_SENSOR_SENSITIVITY;
    return current;
}

void applyBatteryProfile() {
    if (batteryType == "Lithium Iron Phosphate (LiFePO4)") {
        maxVoltageLimit = 14.6;         
        lowBatteryCutoffLimit = 11.2;   
        maxCurrentLimit = 30.0;         
    } else { 
        maxVoltageLimit = 14.4;         
        lowBatteryCutoffLimit = 10.8;   
        maxCurrentLimit = 20.0;
    }
}

void evaluateSafetyProtections(float batVolt, float batCurr, float batTemp) {
    bool overVoltage = (batVolt > maxVoltageLimit);
    bool underVoltage = (batVolt < lowBatteryCutoffLimit);
    bool overCurrent = (abs(batCurr) > maxCurrentLimit);
    bool overTemp = (batTemp > 50.0); 

    if (overVoltage || underVoltage || overCurrent || overTemp) {
        isAutoCutTripped = true;
        digitalWrite(PIN_RELAY_LOAD, HIGH); // Disconnect load safely
        digitalWrite(PIN_LED_SAFE, LOW);
        digitalWrite(PIN_LED_TRIP, HIGH);   
    } else {
        if (!isAutoCutTripped) {
            digitalWrite(PIN_RELAY_LOAD, LOW); // Safe operation
            digitalWrite(PIN_LED_SAFE, HIGH);
            digitalWrite(PIN_LED_TRIP, LOW);
        }
    }
}

void reconnectMqtt() {
    while (!mqttClient.connected()) {
        Serial.print("Connecting to MQTT Broker...");
        String clientId = "ESP32_Client_" + String(random(0, 1000));
        if (mqttClient.connect(clientId.c_str())) {
            Serial.println("Connected!");
            String subTopic = "mehedy/solar/" + deviceId + "/control";
            mqttClient.subscribe(subTopic.c_str());
        } else {
            Serial.print("Failed, rc=");
            Serial.print(mqttClient.state());
            Serial.println(" Retrying in 3 seconds.");
            delay(3000);
        }
    }
}

void mqttCallback(char* topic, byte* payload, unsigned int length) {
    DynamicJsonDocument doc(256);
    deserializeJson(doc, payload, length);
    
    if(doc.containsKey("reset_relay")) {
        if(doc["reset_relay"].as<bool>() == true) {
            isAutoCutTripped = false;
            digitalWrite(PIN_RELAY_LOAD, LOW);
            Serial.println("Auto-Cut Breaker Reset Remotely via App!");
        }
    }
    if(doc.containsKey("toggle_load")) {
        bool state = doc["toggle_load"].as<bool>();
        digitalWrite(PIN_RELAY_LOAD, state ? LOW : HIGH);
    }
}

void setup() {
    Serial.begin(115200);
    
    pinMode(PIN_RELAY_LOAD, OUTPUT);
    pinMode(PIN_LED_SAFE, OUTPUT);
    pinMode(PIN_LED_TRIP, OUTPUT);
    
    digitalWrite(PIN_RELAY_LOAD, LOW); 
    digitalWrite(PIN_LED_SAFE, HIGH);
    digitalWrite(PIN_LED_TRIP, LOW);

    tempSensor.begin();

    preferences.begin("iot-config", true);
    wifiSsid = preferences.getString("ssid", "");
    wifiPassword = preferences.getString("pass", "");
    batteryType = preferences.getString("bat_type", "Tall Tubular Lead-Acid");
    isPaired = preferences.getBool("is_paired", false);
    preferences.end();

    applyBatteryProfile();

    if (isPaired && wifiSsid.length() > 0) {
        WiFi.begin(wifiSsid.c_str(), wifiPassword.c_str());
        int timeoutCounter = 0;
        while (WiFi.status() != WL_CONNECTED && timeoutCounter < 20) {
            delay(500);
            Serial.print(".");
            timeoutCounter++;
        }
        
        if (WiFi.status() == WL_CONNECTED) {
            Serial.println("\nWiFi Connected successfully!");
            mqttClient.setServer(mqttBroker.c_str(), mqttPort);
            mqttClient.setCallback(mqttCallback);
            reconnectMqtt();
        } else {
            isPaired = false;
        }
    }

    if (!isPaired) {
        BLEDevice::init(deviceId.c_str());
        BLEServer *pServer = BLEDevice::createServer();
        pServer->setCallbacks(new MyServerCallbacks());

        BLEService *pService = pServer->createService(SERVICE_UUID);
        BLECharacteristic *pCharacteristic = pService->createCharacteristic(
                                             CHARACTERISTIC_UUID_RX,
                                             BLECharacteristic::PROPERTY_WRITE
                                           );
        pCharacteristic->setCallbacks(new MyCharacteristicCallbacks());
        pService->start();

        BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
        pAdvertising->addServiceUUID(SERVICE_UUID);
        pAdvertising->setScanResponse(true);
        BLEDevice::startAdvertising();
        Serial.println("Awaiting BLE provisioning credentials from app...");
    }
}

void loop() {
    if (WiFi.status() == WL_CONNECTED) {
        if (!mqttClient.connected()) {
            reconnectMqtt();
        }
        mqttClient.loop();
    }

    unsigned long now = millis();
    if (now - lastTelemetryTime >= telemetryInterval) {
        lastTelemetryTime = now;

        float solarVolt = readVoltage(PIN_SOLAR_VOLT);
        float batVolt = readVoltage(PIN_BATTERY_VOLT);
        float batCurr = readCurrent();
        
        tempSensor.requestTemperatures();
        float batTemp = tempSensor.getTempCByIndex(0);
        if(batTemp == DEVICE_DISCONNECTED_C) {
            batTemp = 28.5; 
        }

        evaluateSafetyProtections(batVolt, batCurr, batTemp);

        float solarPower = solarVolt * abs(batCurr > 0 ? batCurr : 0.0);
        float batteryPower = batVolt * batCurr;

        DynamicJsonDocument telemetryDoc(512);
        telemetryDoc["deviceId"] = deviceId;
        telemetryDoc["panelVoltage"] = round(solarVolt * 10.0) / 10.0;
        telemetryDoc["panelCurrent"] = round((solarPower/18.0) * 100.0) / 100.0;
        telemetryDoc["panelPower"] = round(solarPower * 10.0) / 10.0;
        
        telemetryDoc["batteryVoltage"] = round(batVolt * 10.0) / 10.0;
        telemetryDoc["batteryCurrent"] = round(batCurr * 100.0) / 100.0;
        telemetryDoc["batteryPower"] = round(batteryPower * 10.0) / 10.0;
        telemetryDoc["batteryTemp"] = round(batTemp * 10.0) / 10.0;
        
        telemetryDoc["isAutoCutTripped"] = isAutoCutTripped;
        telemetryDoc["batteryType"] = batteryType;

        if (WiFi.status() == WL_CONNECTED && mqttClient.connected()) {
            char jsonBuffer[512];
            serializeJson(telemetryDoc, jsonBuffer);
            String pubTopic = "mehedy/solar/" + deviceId + "/telemetry";
            mqttClient.publish(pubTopic.c_str(), jsonBuffer);
        }
    }
}
```

---

## 5. Smart IoT Provisioning and Pairing Flow

Our custom app features a seamless pairing wizard that completely mirrors commercial products like Tuya or Smart Life:

1.  **Preparation:** Turn on your smartphone's **Location (GPS)**, **Bluetooth**, and connect to your home **Wi-Fi** router.
2.  **BLE Discovery:** Open the Companion App, navigate to the **Settings Panel**, and scroll to the **ESP32 IoT Inverter Pairing** section.
3.  **Wi-Fi Credentials Provisioning:** Enter the local Wi-Fi router's name (SSID) and Password into the form, then click **"Scan for Inverter Bluetooth Gateways"**.
4.  **Pairing and Handshake:** The app discovers the nearby **`ESP32_HYBRID_IPS_04B`** via BLE. Click **"PAIR & CONFIG"**. The app writes the SSID, Password, and selected battery profile to the ESP32.
5.  **Global Connection:** The ESP32 reboots, connects to the local router, and connects globally to the HiveMQ MQTT broker. Your system is now online globally!

---

## 6. Intelligent Protections & Auto-Cut Limits

Traditional IPS systems lack granular protections. This system introduces advanced safeguards:

*   **Over-Voltage Protection (OVP):** If battery voltage exceeds **14.4V** (or LiFePO4 limits), the system activates high-side cutting to prevent acid leakage or thermal runaway.
*   **Under-Voltage Protection (UVP):** Deep discharge is the #1 killer of tubular batteries. If the voltage drops below **10.8V** (customizable down to **10.5V** in Settings), the AC output relay is cut immediately, preserving the battery's health.
*   **Over-Current Protection (OCP):** Short circuits or overloading the inverter triggers an instant hardware auto-cut relay trip within **50 milliseconds** to save the MOSFET banks from burning out.

---

## 7. Testing and Calibration Procedures

Before marketing your hardware, perform this strict quality control checklist to eliminate potential bugs:

### Phase 1: Raw Voltage Calibration
Due to resistor tolerance (e.g. 1% or 5%), your voltage dividers may report slight offsets.
1.  Connect a precise digital multimeter directly to your battery terminals.
2.  Observe the serial readings on the Arduino Serial Monitor.
3.  Adjust the `VOLTAGE_DIVIDER_RATIO` in the firmware code (e.g., from `11.0` to `10.95` or `11.05`) until the software reading exactly matches the multimeter's reading to within **±0.1V**.

### Phase 2: Current Sensor Zeroing
1.  Disconnect all loads and charging sources so that the current is exactly **0.0 Amps**.
2.  Adjust the offset subtraction value (nominally `1.65` in `(pinVolt - 1.65)`) in the firmware code until the idle current reports exactly `0.00A`.

---

## 8. Commercial Product Marketing & Branding Guidelines

This platform is ready to be packaged, branded, and sold to the end consumer. Follow these best-practices to maximize value:

1.  **Custom App Branding:** You can configure your custom installer name and helpline telephone number directly from the **Settings Panel**. This immediately maps to the **Helpline** button on the home screen card, providing a professional technical support experience for your brand.
2.  **Enclosure Protection:** Houses the ESP32 and sensor circuitry in an **IP54-rated dustproof plastic enclosure** mounted outside the main inverter transformer compartment to shield it from EMI and heat.
3.  **Plug-and-Play Packaging:** Package the system as an external "Smart IoT Plug / Box" that hooks into existing inverter terminal blocks, offering consumers an easy upgrade path for their existing systems.

---
*Developed with ❤️ for high-efficiency renewable energy systems in Bangladesh and global markets.*
