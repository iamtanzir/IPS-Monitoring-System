package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Immutable
data class BomItem(
    val id: String,
    val nameEn: String,
    val nameBn: String,
    val partNumber: String,
    val category: String,
    val imageRes: Int,
    val quantity: Int,
    val unitPriceBdt: Int,
    val esp32PinConnection: String,
    val voltageRating: String,
    val descriptionBn: String,
    val whyNeededBn: String,
    val buyUrl: String = "",
    val storeName: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardwareBomDialog(
    onDismiss: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .padding(vertical = 12.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SolarBgDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, SolarCyan.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top App Bar / Header
                Surface(
                    color = SolarCardBg,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(SolarCyan.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = SolarCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Hardware Bill of Materials (BOM)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "সার্কিট ডায়াগ্রাম ও কম্পোনেন্ট তালিকা (ছবিসহ)",
                                    fontSize = 11.sp,
                                    color = SolarCyan
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .background(SolarCardElevated, CircleShape)
                                .border(1.dp, SolarBorder, CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Scrollable Content
                HardwareBomContent()
            }
        }
    }
}

@Composable
fun HardwareBomContent(modifier: Modifier = Modifier) {
    val bomItems = remember {
        listOf(
            BomItem(
                id = "esp32",
                nameEn = "ESP32-C6 DevKit-C N16R8 Development Board",
                nameBn = "ESP32-C6 N16R8 ডেভেলপমেন্ট বোর্ড (WiFi 6 + BLE 5 + Zigbee)",
                partNumber = "ESP32-C6-WROOM-1-N16R8 (16MB Flash / 8MB PSRAM)",
                category = "Controller",
                imageRes = R.drawable.ic_bom_esp32,
                quantity = 1,
                unitPriceBdt = 890,
                esp32PinConnection = "Master Controller (RISC-V 160MHz • Wi-Fi 6 • BLE 5.0 • Type-C)",
                voltageRating = "5V Input (USB-C / VIN) • 3.3V Logic Level",
                descriptionBn = "সিস্টেমের মূল প্রসেসর। এতে রয়েছে দ্রুতগতির ১৬০MHz RISC-V কোর, ১৬MB ফ্ল্যাশ মেমোরি, ৮MB PSRAM এবং আধুনিক ওয়াইফাই ৬ (802.11ax), ব্লুটুথ ৫ ও জিগবি/ম্যাটার সাপোর্ট।",
                whyNeededBn = "বিডিট্রনিক্স (BDtronics) অনুমোদিত অফিসিয়াল বোর্ড। ইউএসবি টাইপ-সি ইন্টারফেস ও বিশাল মেমোরির ফলে ক্লাউড আইওটি ও ফাস্ট ব্লুটুথ পেয়ারিং সবচেয়ে নির্ভরযোগ্য।",
                buyUrl = "https://www.bdtronics.com/esp32-c6-devkit-c-n16r8-esp32-development-board-wifi-bluetooth.html",
                storeName = "BDtronics Bangladesh"
            ),
            BomItem(
                id = "acs712",
                nameEn = "ACS712 Current Sensor Module",
                nameBn = "ACS712 কারেন্ট সেন্সর (অ্যাম্পিয়ার পরিমাপক)",
                partNumber = "ACS712-20A / 30A Module",
                category = "Sensors",
                imageRes = R.drawable.ic_bom_acs712,
                quantity = 1,
                unitPriceBdt = 180,
                esp32PinConnection = "OUT -> 1k/2k Divider -> ESP32-C6 GPIO 1 (ADC1_CH1)",
                voltageRating = "5V VCC, 0-20A/30A Hall Effect",
                descriptionBn = "সোলার ও লোডের মধ্য দিয়ে প্রবাহিত রিয়েল-টাইম কারেন্ট (অ্যাম্পিয়ার) নির্ভুলভাবে পরিমাপ করে।",
                whyNeededBn = "৫ ভোল্ট সেন্সর সিগন্যালকে ESP32-C6 এর নিরাপদ ৩.৩V এডিসি ইনপুটে সীমাবদ্ধ রাখতে ১k ও ২k রেজিস্টর ব্যবহার করা হয়েছে।"
            ),
            BomItem(
                id = "voltage_sensor",
                nameEn = "DC 0-25V Voltage Sensor Module",
                nameBn = "ভোল্টেজ ডিটেকশন সেন্সর মডিউল (০-২৫V)",
                partNumber = "Standard 5:1 Resistor Divider Module",
                category = "Sensors",
                imageRes = R.drawable.ic_bom_voltage_sensor,
                quantity = 1,
                unitPriceBdt = 90,
                esp32PinConnection = "Signal (S) -> ESP32-C6 GPIO 2 (ADC1_CH2)",
                voltageRating = "Input: 0 - 25V DC",
                descriptionBn = "সোলার প্যানেল এবং ব্যাটারির টার্মিনাল ভোল্টেজ নিখুঁতভাবে সেন্স করার জন্য ৫:১ অনুপাতের ভোল্টেজ ডিভায়ডার।",
                whyNeededBn = "ব্যাটারি ফুল চার্জ নাকি লো ভোল্টেজ তা জানার জন্য এটি অপরিহার্য।"
            ),
            BomItem(
                id = "ds18b20",
                nameEn = "DS18B20 Digital Temp Sensor",
                nameBn = "DS18B20 ডিজিটাল তাপমাত্রা সেন্সর",
                partNumber = "DS18B20 Waterproof Steel Probe",
                category = "Sensors",
                imageRes = R.drawable.ic_bom_ds18b20,
                quantity = 1,
                unitPriceBdt = 160,
                esp32PinConnection = "Data Wire -> ESP32-C6 GPIO 18 (with 4.7kΩ Pull-up to 3.3V)",
                voltageRating = "3.0V - 5.5V DC",
                descriptionBn = "ব্যাটারি ও ইনভার্টার হিটসিংকের তাপমাত্রা রিয়েল-টাইম পর্যবেক্ষণ করে ওভার-হিটিং প্রটেকশন দেয়।",
                whyNeededBn = "স্টেইনলেস স্টিলের ওয়াটারপ্রুফ প্রোব সরাসরি ব্যাটারি সেলে লাগিয়ে রাখা যায়।"
            ),
            BomItem(
                id = "lcd_i2c",
                nameEn = "16x2 LCD Display with I2C Backpack",
                nameBn = "১৬x২ এলসিডি ডিসপ্লে (I2C অ্যাডাপ্টারসহ)",
                partNumber = "LCD1602 + PCF8574T I2C Interface",
                category = "Display",
                imageRes = R.drawable.ic_bom_lcd_i2c,
                quantity = 1,
                unitPriceBdt = 320,
                esp32PinConnection = "SDA -> ESP32-C6 GPIO 6, SCL -> ESP32-C6 GPIO 7, VCC -> 5V",
                voltageRating = "5V DC Operating",
                descriptionBn = "মোবাইল ফোন ছাড়াও সরাসরি ডিভাইসের সামনে দাঁড়িয়ে সোলার ভোল্টেজ, কারেন্ট ও পাওয়ার দেখার স্ক্রিন।",
                whyNeededBn = "I2C বাস ব্যবহারের ফলে মাত্র ২টি তারের (GPIO 6, 7) মাধ্যমে সব ডেটা প্রদর্শিত হয়।"
            ),
            BomItem(
                id = "bh1750",
                nameEn = "BH1750 Ambient Light / Lux Sensor",
                nameBn = "BH1750 সূর্যের আলো ও লাক্স পরিমাপক সেন্সর",
                partNumber = "BH1750FVI Digital Light Sensor",
                category = "Sensors",
                imageRes = R.drawable.ic_bom_bh1750,
                quantity = 1,
                unitPriceBdt = 150,
                esp32PinConnection = "SDA -> ESP32-C6 GPIO 6, SCL -> ESP32-C6 GPIO 7, VCC -> 3.3V/5V",
                voltageRating = "3.3V - 5V DC",
                descriptionBn = "সূর্যের তীব্রতা (Solar Irradiance in Lux) পরিমাপ করে সিস্টেম এফিসিয়েন্সি গণনা করে।",
                whyNeededBn = "মেঘলা আকাশ নাকি সরাসরি কড়া রোদ তা অটোমেটিক ডিটেক্ট করে।"
            ),
            BomItem(
                id = "buck_converter",
                nameEn = "LM2596 / 5V DC Step-Down Buck Converter",
                nameBn = "৫V স্টেপ-ডাউন বাক কনভার্টার (পাওয়ার সাপ্লাই)",
                partNumber = "LM2596 DC-DC Step Down (3A)",
                category = "Power",
                imageRes = R.drawable.ic_bom_buck_converter,
                quantity = 1,
                unitPriceBdt = 130,
                esp32PinConnection = "OUT+ -> ESP32-C6 VIN & 5V Rail, OUT- -> ESP32-C6 GND",
                voltageRating = "Input: 7-35V DC, Output: 5.0V Regulated",
                descriptionBn = "১২V ব্যাটারি বা সোলার প্যানেল থেকে স্টেবল ৫ ভোল্ট তৈরি করে ESP32-C6 ও সেন্সরকে পাওয়ার সাপ্লাই দেয়।",
                whyNeededBn = "হাই ভোল্টেজ স্পাইক থেকে ESP32-C6 ও সার্কিট রক্ষা করতে ৯২% হাই এফিশিয়েন্ট বাক রেগুলেটর।"
            ),
            BomItem(
                id = "solar_panel",
                nameEn = "Solar Photovoltaic Panel",
                nameBn = "সোলার ফটোভোলটাইক প্যানেল",
                partNumber = "12V / 18V Monocrystalline Panel (20W-100W)",
                category = "Power",
                imageRes = R.drawable.ic_bom_solar_panel,
                quantity = 1,
                unitPriceBdt = 1800,
                esp32PinConnection = "+ to Voltage Sensor & ACS712, - to Common GND",
                voltageRating = "12V-21V Open Circuit Voltage",
                descriptionBn = "সূর্যের আলো থেকে সরাসরি ডিসি বিদ্যুৎ তৈরি করে ব্যাটারি চার্জ ও লোড ড্রাইভ করে।",
                whyNeededBn = "সৌরবিদ্যুৎ সিস্টেমের মূল শক্তি উৎপাদক।"
            ),
            BomItem(
                id = "passives",
                nameEn = "Resistors & Dupont Jumper Wires Kit",
                nameBn = "রেজিস্টর ও ডুপন্ট জাম্পার ওয়্যার কিট",
                partNumber = "1kΩ (x1), 2kΩ (x1), 4.7kΩ (x1) + Jumper Wires",
                category = "Power",
                imageRes = R.drawable.ic_bom_resistors_wires,
                quantity = 1,
                unitPriceBdt = 90,
                esp32PinConnection = "Voltage Divider for ACS712 (GPIO 1) & Pullup for DS18B20 (GPIO 18)",
                voltageRating = "1/4 Watt Metal Film Resistors",
                descriptionBn = "ACS712 এর ৫V সিগন্যালকে ESP32-C6 ৩.৩V এডিসি সেফটিতে ডিভাইড করতে ১কে ও ২কে রেজিস্টর এবং DS18B20 এর জন্য ৪.৭কে পুল-আপ।",
                whyNeededBn = "ESP32-C6 পিন সুরক্ষা ও নির্ভরযোগ্য ওয়ান-ওয়্যার ডেটা কমিউনিকেশন নিশ্চিত করে।"
            )
        )
    }

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Sensors", "Controller", "Display", "Power")

    val filteredItems = remember(selectedCategory) {
        if (selectedCategory == "All") bomItems else bomItems.filter { it.category == selectedCategory }
    }

    val totalCost = remember { bomItems.sumOf { it.quantity * it.unitPriceBdt } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        // 1. FULL CIRCUIT DIAGRAM & SCHEMATIC OVERVIEW CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SolarCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, SolarCyan.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountTree, contentDescription = null, tint = SolarCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "IoT Solar Monitoring Circuit Diagram",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(SolarGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("ESP32 Cloud", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SolarGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // The Circuit Diagram Visual Illustration
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0A1118))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_bom_full_schematic),
                            contentDescription = "IoT Solar Power Monitoring Circuit Diagram with ESP32",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "সার্কিট ডায়াগ্রাম অনুযায়ী সংযোগ গাইড (ESP32-C6 Pinout Quick Guide):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SolarCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick Pinout Summary Chips
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SolarCardElevated, RoundedCornerShape(8.dp))
                            .border(1.dp, SolarBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        PinoutRow(pin = "GPIO 1 (ADC1)", wireColor = "#EC4899", desc = "ACS712 কারেন্ট সেন্সর (১k/২k ভোল্টেজ ডিভায়ডার সহ)")
                        PinoutRow(pin = "GPIO 2 (ADC1)", wireColor = "#3B82F6", desc = "ভোল্টেজ সেন্সর সিগন্যাল (০-২৫V)")
                        PinoutRow(pin = "GPIO 18 (1-Wire)", wireColor = "#A855F7", desc = "DS18B20 তাপমাত্রা সেন্সর (৪.৭k পুল-আপ সহ)")
                        PinoutRow(pin = "GPIO 6 (SDA)", wireColor = "#22C55E", desc = "I2C ডেটা লাইন (LCD1602 এবং BH1750 লাইট সেন্সর)")
                        PinoutRow(pin = "GPIO 7 (SCL)", wireColor = "#2563EB", desc = "I2C ক্লক লাইন (LCD1602 এবং BH1750 লাইট সেন্সর)")
                        PinoutRow(pin = "GPIO 8 (WS2812)", wireColor = "#F59E0B", desc = "অনবোর্ড অ্যাড্রেসেবল RGB স্ট্যাটাস এলইডি")
                        PinoutRow(pin = "VIN & GND", wireColor = "#EF4444", desc = "৫V DC পাওয়ার সাপ্লাই (LM2596 বাক কনভার্টার)")
                    }
                }
            }
        }

        // 2. TOTAL BOM SUMMARY & BUDGET CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SolarCardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, SolarBorder),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TOTAL BILL OF MATERIALS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("৳ $totalCost", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = SolarGold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("BDT (আনুমানিক)", fontSize = 10.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 2.dp))
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("মোট কম্পোনেন্ট সংখ্যা", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Text("${bomItems.size} টি প্রধান পার্টস", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SolarCyan)
                    }
                }
            }
        }

        // 3. CATEGORY SELECTOR CHIPS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) SolarCyan else SolarCardBg)
                            .border(1.dp, if (isSelected) SolarCyan else SolarBorder, RoundedCornerShape(20.dp))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when (cat) {
                                "All" -> "সবগুলো (${bomItems.size})"
                                "Sensors" -> "সেন্সরসমূহ"
                                "Controller" -> "কন্ট্রোলার"
                                "Display" -> "ডিসপ্লে"
                                "Power" -> "পাওয়ার"
                                else -> cat
                            },
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF0D1B2A) else TextPrimary
                        )
                    }
                }
            }
        }

        // 4. BOM ITEMS LIST (CARDS WITH VISUAL PICTURES)
        items(filteredItems, key = { it.id }) { item ->
            BomItemCard(item = item)
        }

        // 5. SAFETY & FABRICATION TIPS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E242C)),
                border = androidx.compose.foundation.BorderStroke(1.dp, SolarGold.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = SolarGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "হার্ডওয়্যার কানেকশন সতর্কতা ও বিশেষ টিপস",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SolarGold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• ১k ও ২k ভোল্টেজ ডিভায়ডার: ACS712 এর অ্যানালগ আউটপুট সর্বোচ্চ ৫ ভোল্ট পর্যন্ত যেতে পারে, কিন্তু ESP32-C6 এর অ্যানালগ পিন (GPIO 1) সর্বোচ্চ ৩.৩ ভোল্ট সহ্য করতে পারে। তাই রেজিস্টর ডিভায়ডার মিস করলে ESP32-C6 এর এডিসি চ্যানেল ক্ষতিগ্রস্ত হতে পারে!\n• কমন গ্রাউন্ড (Common GND): পাওয়ার সাপ্লাই, সোলার সেন্সর এবং ESP32-C6 এর GND পিন অবশ্যই একসাথে কমন গ্রাউন্ড সংযোগ দিতে হবে।\n• ৪.৭k পুল-আপ রেজিস্টর: DS18B20 সেন্সরের ডেটা তারের (হলুদ) সাথে ৩.৩V লাইনে ৪.৭k রেজিস্টর যুক্ত করতে ভুলবেন না।\n• টাইপ-সি ইউএসবি ও মেমোরি: ESP32-C6 DevKit-C বোর্ডে আধুনিক Type-C পোর্ট, ১৬MB Flash ও ৮MB PSRAM রয়েছে, যা ফার্মওয়্যার ও ক্লাউড ডেটা প্রসেসিং দ্রুততর করে।",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PinoutRow(pin: String, wireColor: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(Color(android.graphics.Color.parseColor(wireColor)), CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = pin,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = SolarCyan,
            modifier = Modifier.width(78.dp)
        )
        Text(
            text = "→  $desc",
            fontSize = 10.sp,
            color = TextPrimary,
            maxLines = 1
        )
    }
}

@Composable
fun BomItemCard(item: BomItem) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SolarCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, SolarBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // COMPONENT PICTURE / DRAWABLE IMAGE
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F1722))
                        .border(1.dp, SolarCyan.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = item.imageRes),
                        contentDescription = item.nameEn,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Spec Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.nameBn,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = item.nameEn,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(SolarCyan.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(item.partNumber, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SolarCyan)
                        }

                        Box(
                            modifier = Modifier
                                .background(SolarGold.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text("৳ ${item.unitPriceBdt}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SolarGold)
                        }
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Details",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Expandable Technical & Pinout Details
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    HorizontalDivider(color = SolarBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.descriptionBn,
                        fontSize = 11.sp,
                        color = TextPrimary,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Pinout Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SolarCardElevated, RoundedCornerShape(8.dp))
                            .border(1.dp, SolarCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Cable, contentDescription = null, tint = SolarCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ESP32-C6 কানেকশন পিন:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SolarCyan)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(item.esp32PinConnection, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("রেটিং: ${item.voltageRating}", fontSize = 9.sp, color = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "💡 কেন প্রয়োজন: ${item.whyNeededBn}",
                        fontSize = 10.sp,
                        color = SolarGreen,
                        lineHeight = 14.sp
                    )

                    if (item.buyUrl.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val context = androidx.compose.ui.platform.LocalContext.current
                        Button(
                            onClick = {
                                try {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse(item.buyUrl)
                                    )
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SolarGold),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BDtronics থেকে অর্ডার করুন (৳${item.unitPriceBdt})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
