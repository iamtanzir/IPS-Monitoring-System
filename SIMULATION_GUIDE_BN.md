# অনলাইন সিমুলেশন ও ভার্চুয়াল টেস্টবেঞ্চ গাইড: জিরো থেকে রানিং টেস্ট গাইডলাইন
## প্রজেক্ট: ESP32 Smart IoT Hybrid Inverter Simulation (Wokwi & MQTT Gateways)

বাস্তবে উচ্চ ক্ষমতার সোলার প্যানেল (যেমন ৮০৫ ওয়াট, ৫১ ভোল্ট), এমপিপিটি হাইব্রিড ইনভার্টার (৪০এ), বড় এলএফপি ব্যাটারি (১২ভি, ৩১৪এএইচ) এবং এসি লোড (১৫০০ ওয়াট, ২২০ভি) নিয়ে সরাসরি কাজ করা অত্যন্ত ব্যয়বহুল এবং ঝুঁকিপূর্ণ। 

যেকোনো অনাকাঙ্ক্ষিত শর্ট-সার্কিট বা ভুল কানেকশন এড়াতে অনলাইনে সম্পূর্ণ বিনামূল্যে **১০০% ফাংশনাল সিমুলেশন** চালানোর জন্য বিশ্বের সবচেয়ে জনপ্রিয় এবং নির্ভরযোগ্য ক্লাউড টেস্টবেঞ্চ প্ল্যাটফর্ম হলো **Wokwi (wokwi.com)**।

নিচে কীভাবে এই ভার্চুয়াল উইন্ডো তৈরি করবেন, কোড সাজাবেন এবং সরাসরি এই অ্যান্ড্রয়েড অ্যাপের সাথে সিঙ্ক করবেন তার সম্পূর্ণ বিবরণ দেওয়া হলো।

---

## ১. সিমুলেশন আর্কিটেকচার এবং সিঙ্কিং ফ্লোচার্ট

```
 +-----------------------------------------------------------------------------------+
 |                             WOKWI ONLINE SIMULATOR (FREE)                         |
 |  [ভার্চুয়াল ESP32] <--- [পটেনশিওমিটার] (সোলার প্যানেল ও ব্যাটারি লেভেল সিমুলেটর)      |
 +-----------------------------------------------------------------------------------+
                                           |
                                           | (ভার্চুয়াল ওয়াই-ফাই গেটওয়ে)
                                           v
 +-----------------------------------------------------------------------------------+
 |                             MQTT BROKER (HIVE MQ / FREE)                         |
 |  ব্রোকার ইউআরএল: broker.hivemq.com (Port: 1883 TCP)                               |
 |  পাবলিশ টপিক: mehedy/solar/solar01/metrics                                        |
 +-----------------------------------------------------------------------------------+
                                           |
                                           | (লাইভ ইন্টারনেট সিঙ্কিং)
                                           v
 +-----------------------------------------------------------------------------------+
 |                         ANDROID MONITORING APP (YOUR APP)                         |
 |  সেটিংস থেকে MQTT ব্রোকার এবং প্রিফিক্স সেট করলেই লাইভ অ্যানিমেশন ও গ্রাফ চালু হবে!   |
 +-----------------------------------------------------------------------------------+
```

---

## ২. রিয়েল স্পেসিফিকেশন বনাম ভার্চুয়াল এডিসি সিগন্যাল ম্যাপিং

ESP32-র এনালগ পিন (ADC) সর্বোচ্চ ৩.৩ ভোল্ট এবং ১০০ মিলি-অ্যাম্পিয়ার সিগন্যাল রিড করতে পারে। তাই আমাদের বড় লোডগুলোকে পটেনশিওমিটার (স্লাইডার বা নব) ব্যবহার করে ভোল্টেজ ডিভাইডারের মাধ্যমে নিচের স্কেল অনুযায়ী ম্যাপ করতে হবে:

### ক. সোলার প্যানেল (805W, 51V @ 15.7A)
*   **বাস্তব সীমা:** ০ভোল্ট থেকে সর্বোচ্চ ৫১ভোল্ট এবং ০ থেকে ১৫.৭অ্যাম্পিয়ার।
*   **ভোল্টেজ ডিভাইডার:** $100\,\text{k}\Omega$ এবং $10\,\text{k}\Omega$ রেজিস্টর। ৫১ ভোল্ট ইনপুটের সময় ESP32-র পিনে প্রবেশ করবে $4.63\,\text{V}$ (যা ৩.৩ভি পিনের জন্য বেশি, তাই সিমুলেশনে স্কেল ডাউন রেশিও বাড়িয়ে **১৬.০** গুণ ব্যবহার করব)।
*   **ADC কোড ক্যালকুলেশন:**
    $$\text{Solar Voltage} = \text{ADC Pin Read} \times 16.0$$
    $$\text{Solar Power (W)} = \text{Solar Voltage} \times \text{Solar Current (A)}$$

### খ. এলএফপি ব্যাটারি (LFP 12V 314Ah)
*   **বাস্তব সীমা:** লিথিয়াম আয়রন ফসফেট (LiFePO4 বা LFP) ৪-সেল ব্যাটারির নমিনাল ভোল্টেজ ১২.৮ ভোল্ট এবং ফুল চার্জিং ১৪.৪ ভোল্ট, কাট-অফ ১০.৮ ভোল্ট।
*   **ADC কোড ক্যালকুলেশন:** পটেনশিওমিটারের ৩.৩ ভোল্টকে অ্যাপের ১২ ভোল্ট রেঞ্জে রূপান্তর করতে আরডুইনো কোডে ভোল্টেজ মাল্টিপ্লায়ার সেট করতে হবে **৫.০**।

### গ. এসি লোড (1500W @ 220V AC)
*   রিলের নরমালি ক্লোজড (NC) কন্টাক্টের সাথে রিয়েল-টাইমে একটি ভার্চুয়াল এসি ল্যাম্প বা ইন্ডিকেটর বাল্ব লাগাবেন, যা চার্জ কাট-আউট হওয়ার সাথে সাথে নিভে যাবে।

---

## ৩. Wokwi-তে যেভাবে সিমুলেশন সার্কিট সংযোগ দেবেন

Wokwi-তে একটি ফ্রি অ্যাকাউন্ট তৈরি করে একটি **ESP32** প্রজেক্ট নিন এবং নিচের পার্টসগুলো যুক্ত করে কানেক্ট করুন:

1.  **ভোল্টেজ রিডিং সিমুলেটর:** দুটি পটেনশিওমিটার (Slide Potentiometer) নিন।
    *   **পটেনশিওমিটার ১ (Solar):** মাঝের পিনটি কানেক্ট করুন ESP32-র **GPIO34** পিনে। এক মাথা VCC (3.3V) এবং অন্য মাথা GND-তে দিন।
    *   **পটেনশিওমিটার ২ (Battery):** মাঝের পিনটি কানেক্ট করুন ESP32-র **GPIO35** পিনে। এক মাথা VCC (3.3V) এবং অন্য মাথা GND-তে দিন।
2.  **DS18B20 টেম্পারেচার সেন্সর:** তাপমাত্রা রিড করার জন্য DS18B20 মডিউলের ডাটা পিনটি ESP32-র **GPIO4**-এ দিন।
3.  **Mechanical Relay:** একটি রিলে মডিউল নিন। রিলে ইনপুট (IN) দিন **GPIO25**-এ। COM এবং NO বা NC লাইনে একটি ভার্চুয়াল **AC Bulb** ও ২২০ভি সোর্স ডামি হিসেবে যুক্ত করুন।
4.  **I2C LCD 16x2:** এর SDA পিনটি **GPIO21** এবং SCL পিনটি **GPIO22**-এ কানেক্ট করুন।

---

## ৪. Wokwi-র জন্য ১০০% রেডি সিমুলেশন কোড

Wokwi-র আরডুইনো ফাইলের (`sketch.ino`) কোড উইন্ডোতে নিচের কোডটি হুবহু পেস্ট করুন। এটি আপনার সিস্টেম ডাটা প্রসেস করে প্রতি ২.৫ সেকেন্ড পরপর সোলার সিস্টেমের ডাটা এই অ্যাপে পাবলিশ করবে:

```cpp
#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <LiquidCrystal_I2C.h>

// ওয়াই-ফাই এবং ক্লাউড ব্রোকার সেটিং (Wokwi-র নিজস্ব ফ্রি ওয়াই-ফাই গেটওয়ে)
const char* ssid = "Wokwi-GUEST";
const char* password = "";
const char* mqtt_server = "broker.hivemq.com";
const int mqtt_port = 1883;

// অ্যাপ সিঙ্ক টপিক
const char* pub_topic = "mehedy/solar/solar01/metrics";

WiFiClient espClient;
PubSubClient client(espClient);
LiquidCrystal_I2C lcd(0x27, 16, 2);

void setup_wifi() {
  delay(10);
  Serial.println();
  Serial.print("Connecting to WiFi ");
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi Connected Successfully!");
}

void reconnect() {
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    String clientId = "ESP32_WOKWI_SIM_";
    clientId += String(random(0xffff), HEX);
    if (client.connect(clientId.c_str())) {
      Serial.println("Connected to HiveMQ Broker!");
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      delay(5000);
    }
  }
}

void setup() {
  Serial.begin(115200);
  lcd.init();
  lcd.backlight();
  lcd.setCursor(0, 0);
  lcd.print("Solar Simulator");
  lcd.setCursor(0, 1);
  lcd.print("Wokwi Online Run");
  
  setup_wifi();
  client.setServer(mqtt_server, mqtt_port);
}

void loop() {
  if (!client.connected()) {
    reconnect();
  }
  client.loop();

  // ১. পটেনশিওমিটার এবং ডামি রিডিং
  int rawSolar = analogRead(34);
  int rawBattery = analogRead(35);

  // ২. রিয়েল-ওয়ার্ল্ড স্কেলিং ম্যাপিং
  // সোলার প্যানেল ৫১ভোল্ট রেঞ্জে ম্যাপিং
  float panelVoltage = (rawSolar / 4095.0) * 51.0;
  float panelCurrent = (rawSolar / 4095.0) * 15.7; // সর্বোচ্চ ১৫.৭অ্যাম্পিয়ার
  float panelPower = panelVoltage * panelCurrent;

  // LFP ব্যাটারি ১২.৮ভি ও ৩১৪Ah রেঞ্জ ম্যাপিং
  float batteryVoltage = 10.5 + ((rawBattery / 4095.0) * 4.5); // ১০.৫ভি থেকে ১৫.০ভোল্ট পর্যন্ত
  float batteryCurrent = (rawBattery / 4095.0) * 40.0; // চার্জিং ৪০ অ্যাম্পিয়ার MPPT
  float batteryPower = batteryVoltage * batteryCurrent;
  
  int batteryPct = map(rawBattery, 0, 4095, 0, 100);
  float batteryRemainingAh = (batteryPct / 100.0) * 314.0; // ৩১৪Ah ব্যাটারির অবশিষ্ট চার্জ

  // ৩. এলসিডি স্ক্রিন ডাটা
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Bat: " + String(batteryVoltage, 1) + "V " + String(batteryPct) + "%");
  lcd.setCursor(0, 1);
  lcd.print("PV: " + String(panelPower, 0) + "W Out");

  // ৪. অ্যাপের জন্য JSON পে-লোড তৈরি
  StaticJsonDocument<512> doc;
  doc["isSolarOn"] = (panelVoltage > 5.0);
  doc["isChargingOn"] = (panelVoltage > batteryVoltage);
  doc["isLoadOn"] = true;
  doc["panelVoltage"] = panelVoltage;
  doc["panelCurrent"] = panelCurrent;
  doc["panelPower"] = panelPower;
  doc["panelOutputPct"] = map(panelPower, 0, 805, 0, 100);

  doc["batteryVoltage"] = batteryVoltage;
  doc["batteryCurrent"] = batteryCurrent;
  doc["batteryPower"] = batteryPower;
  doc["batteryCapacityAh"] = 314;
  doc["batteryRemainingAh"] = batteryRemainingAh;
  doc["batteryPct"] = batteryPct;
  doc["batteryTemp"] = 35.5;
  doc["batteryCycleCount"] = 45;
  
  // LFP ৪-সেল ব্যালেন্সিং ডাটা
  JsonArray cells = doc.createNestedArray("cellVoltages");
  float cVolt = batteryVoltage / 4.0;
  cells.add(cVolt); cells.add(cVolt + 0.002); cells.add(cVolt - 0.001); cells.add(cVolt);

  // ১৫০০ ওয়াট লোড ম্যাপিং
  doc["loadVoltage"] = 220.0; // এসি গ্রিড ২২০ভি ভোল্টেজ
  doc["loadCurrent"] = 1500.0 / 220.0; // ১৫০০ ওয়াটের লোড অ্যাম্পিয়ার
  doc["loadPower"] = 1500;
  doc["loadSource"] = "Hybrid Grid";

  // সিস্টেম অ্যালার্ট চেকার
  doc["isLowVoltage"] = (batteryVoltage < 10.8);
  doc["isAutoCutTripped"] = (batteryVoltage < 10.8);

  char jsonBuffer[512];
  serializeJson(doc, jsonBuffer);
  
  // এমকিউটিটি-তে রিয়েল-টাইম ডাটা সেন্ড
  client.publish(pub_topic, jsonBuffer);
  Serial.println("Telemetry Published: " + String(jsonBuffer));

  delay(2500); // প্রতি ২.৫ সেকেন্ড পরপর ডাটা সিঙ্ক
}
```

---

## ৫. ড্যাশবোর্ড অ্যাপে লাইভ রিডিং দেখার নিয়ম

Wokwi সিমুলেশনটি সফলভাবে চালু করার পর অ্যাপে ডাটা কানেক্ট করার নিয়ম:

1.  **Wokwi-র স্টার্ট বাটনে ক্লিক করুন:** ভার্চুয়াল ওয়াই-ফাই কানেক্ট হয়ে স্ক্রিনে `Connected to HiveMQ Broker!` লেখা আসা পর্যন্ত অপেক্ষা করুন।
2.  **আপনার অ্যান্ড্রয়েড অ্যাপ ওপেন করুন:**
    *   **সেটিংস (Settings) ট্যাবে যান।**
    *   **MQTT Broker Address** ফিল্ডে লিখুন: `broker.hivemq.com`
    *   **MQTT Port** ফিল্ডে দিন: `1883`
    *   **MQTT Topic Prefix** ফিল্ডে লিখুন: `mehedy/solar/solar01`
3.  **সেভ এবং কানেক্ট করুন:** সেভ বাটনে প্রেস করলেই উপরের **"ESP32 ONLINE"** লেখা সবুজ পালসিং ইন্ডিকেটরটি জ্বলে উঠবে এবং আপনার ৮০৫ ওয়াট সোলার ও ৩১৪Ah ব্যাটারির সমস্ত লাইভ অ্যানিমেশন, তাপমাত্রা এবং গ্রাফ রিয়েল টাইমে রিফ্রেশ হতে থাকবে! পটেনশিওমিটার স্লাইড করার সাথে সাথে অ্যাপের মান কমবে-বাড়বে।
