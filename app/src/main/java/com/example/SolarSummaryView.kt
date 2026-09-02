package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SolarBorder
import com.example.ui.theme.SolarCardBg
import com.example.ui.theme.SolarCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SolarSummaryView(
    viewModel: SolarViewModel,
    modifier: Modifier = Modifier
) {
    val items = viewModel.historicalSummaries

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "SYSTEM SUMMARY",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SolarCyan,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Table Header
        Card(
            colors = CardDefaults.cardColors(containerColor = SolarCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, SolarBorder),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Parameter",
                    modifier = Modifier.weight(1.4f),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Today",
                    modifier = Modifier.weight(1f),
                    color = SolarCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
                Text(
                    text = "Yesterday",
                    modifier = Modifier.weight(1f),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
                Text(
                    text = "Before yesterday",
                    modifier = Modifier.weight(1.1f),
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
            }
        }

        // Table Content Rows
        Card(
            colors = CardDefaults.cardColors(containerColor = SolarCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, SolarBorder),
            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                val rowConfigs = getRowConfigs(items)

                items(rowConfigs) { row ->
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = row.title,
                                modifier = Modifier.weight(1.4f),
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = row.col1,
                                modifier = Modifier.weight(1f),
                                color = SolarCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = row.col2,
                                modifier = Modifier.weight(1f),
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = row.col3,
                                modifier = Modifier.weight(1.1f),
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.End
                            )
                        }
                        Divider(color = SolarBorder.copy(alpha = 0.3f))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Map summaries into dynamic vertical list rows to render the table effortlessly
data class SummaryRow(
    val title: String,
    val col1: String,
    val col2: String,
    val col3: String
)

private fun getRowConfigs(items: List<GridHistoricalSummary>): List<SummaryRow> {
    if (items.size < 3) return emptyList()
    val t = items[0]
    val y = items[1]
    val b = items[2]

    return listOf(
        SummaryRow("Max PV Power (W)", "${t.maxPvPower.toInt()} W", "${y.maxPvPower.toInt()} W", "${b.maxPvPower.toInt()} W"),
        SummaryRow("Min PV Power (W)", "${t.minPvPower.toInt()} W", "${y.minPvPower.toInt()} W", "${b.minPvPower.toInt()} W"),
        SummaryRow("Avg PV Power (W)", "${t.avgPvPower} W", "${y.avgPvPower} W", "${b.avgPvPower} W"),
        SummaryRow("Max PV Voltage (V)", "${t.maxPvVoltage} V", "${y.maxPvVoltage} V", "${b.maxPvVoltage} V"),
        SummaryRow("Min PV Voltage (V)", "${t.minPvVoltage} V", "${y.minPvVoltage} V", "${b.minPvVoltage} V"),
        SummaryRow("Avg PV Voltage (V)", "${t.avgPvVoltage} V", "${y.avgPvVoltage} V", "${b.avgPvVoltage} V"),
        SummaryRow("Max PV Ampere (A)", "${t.maxPvAmpere} A", "${y.maxPvAmpere} A", "${b.maxPvAmpere} A"),
        SummaryRow("Min PV Ampere (A)", "${t.minPvAmpere} A", "${y.minPvAmpere} A", "${b.minPvAmpere} A"),
        SummaryRow("Avg PV Ampere (A)", "${t.avgPvAmpere} A", "${y.avgPvAmpere} A", "${b.avgPvAmpere} A"),
        SummaryRow("Total Battery Charge (Wh)", "${t.totalBatteryChargeWh} Wh", "${y.totalBatteryChargeWh} Wh", "${b.totalBatteryChargeWh} Wh"),
        SummaryRow("Total Battery Charge (Ah)", "${t.totalBatteryChargeAh} Ah", "${y.totalBatteryChargeAh} Ah", "${b.totalBatteryChargeAh} Ah"),
        SummaryRow("Max Battery Charge (A)", "${t.maxBatteryChargeAmp} A", "${y.maxBatteryChargeAmp} A", "${b.maxBatteryChargeAmp} A"),
        SummaryRow("Min Battery Charge (A)", "${t.minBatteryChargeAmp} A", "${y.minBatteryChargeAmp} A", "${b.minBatteryChargeAmp} A"),
        SummaryRow("Avg Battery Charge (A)", "${t.avgBatteryChargeAmp} A", "${y.avgBatteryChargeAmp} A", "${b.avgBatteryChargeAmp} A"),
        SummaryRow("Max Discharge (W)", "${t.maxDischargeW.toInt()} W", "${y.maxDischargeW.toInt()} W", "${b.maxDischargeW.toInt()} W"),
        SummaryRow("Min Discharge (W)", "${t.minDischargeW.toInt()} W", "${y.minDischargeW.toInt()} W", "${b.minDischargeW.toInt()} W"),
        SummaryRow("Total Discharge (Wh)", "${t.totalDischargeWh} Wh", "${y.totalDischargeWh} Wh", "${b.totalDischargeWh} Wh"),
        SummaryRow("Load From Panel (Wh)", "${t.loadFromPanelWh} Wh", "${y.loadFromPanelWh} Wh", "${b.loadFromPanelWh} Wh"),
        SummaryRow("Load From Battery (Wh)", "${t.loadFromBatteryWh} Wh", "${y.loadFromBatteryWh} Wh", "${b.loadFromBatteryWh} Wh"),
        SummaryRow("Max Discharge (A)", "${t.maxDischargeAmp} A", "${y.maxDischargeAmp} A", "${b.maxDischargeAmp} A"),
        SummaryRow("Min Discharge (A)", "${t.minDischargeAmp} A", "${y.minDischargeAmp} A", "${b.minDischargeAmp} A"),
        SummaryRow("Avg Discharge (A)", "${t.avgDischargeAmp} A", "${y.avgDischargeAmp} A", "${b.avgDischargeAmp} A")
    )
}
