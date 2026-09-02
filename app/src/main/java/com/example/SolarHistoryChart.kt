package com.example

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SolarCardBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SolarHistoryChart(
    points: List<Float>,
    labels: List<String>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val maxVal = (points.maxOrNull() ?: 100f).coerceAtLeast(10f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SolarCardBg, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Solar Power Output Trend (W)",
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val spacing = width / (points.size - 1).coerceAtLeast(1)

                val pointsCoord = points.mapIndexed { idx, value ->
                    val x = idx * spacing
                    val y = height - (value / maxVal) * (height - 20f)
                    Offset(x, y)
                }

                // Draw background grid lines
                val gridLines = 4
                for (i in 0..gridLines) {
                    val yVal = height * i / gridLines
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(0f, yVal),
                        end = Offset(width, yVal),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                if (points.size > 1) {
                    // Create path for stroke line
                    val path = Path().apply {
                        moveTo(pointsCoord.first().x, pointsCoord.first().y)
                        for (i in 1 until pointsCoord.size) {
                            lineTo(pointsCoord[i].x, pointsCoord[i].y)
                        }
                    }

                    // Create closed path for gradient fill under the line
                    val fillPath = Path().apply {
                        moveTo(pointsCoord.first().x, height)
                        for (i in 0 until pointsCoord.size) {
                            lineTo(pointsCoord[i].x, pointsCoord[i].y)
                        }
                        lineTo(pointsCoord.last().x, height)
                        close()
                    }

                    // Draw area gradient fill
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // Draw smooth line
                    drawPath(
                        path = path,
                        color = accentColor,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Draw circles on point coordinates
                    pointsCoord.forEach { offset ->
                        drawCircle(
                            color = Color.White,
                            radius = 5.dp.toPx(),
                            center = offset
                        )
                        drawCircle(
                            color = accentColor,
                            radius = 3.dp.toPx(),
                            center = offset
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Draw horizontal label row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun BatteryChargeBarChart(
    values: List<Float>,
    labels: List<String>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    if (values.isEmpty()) return
    val maxVal = (values.maxOrNull() ?: 100f).coerceAtLeast(10f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SolarCardBg, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Battery Charge Level History (Wh)",
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val barWidth = 40.dp.toPx()
                val gap = (width - (barWidth * values.size)) / (values.size + 1)

                // Draw background grid lines
                val gridLines = 4
                for (i in 0..gridLines) {
                    val yVal = height * i / gridLines
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(0f, yVal),
                        end = Offset(width, yVal),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                values.forEachIndexed { index, valRaw ->
                    val barHeight = (valRaw / maxVal) * (height - 20f)
                    val left = gap + index * (barWidth + gap)
                    val top = height - barHeight

                    // Draw bar with gradient
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                accentColor,
                                accentColor.copy(alpha = 0.4f)
                            ),
                            startY = top,
                            endY = height
                        ),
                        topLeft = Offset(left, top),
                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                    )

                    // Draw small highlight border top of each bar
                    drawLine(
                        color = accentColor.copy(alpha = 0.8f),
                        start = Offset(left, top),
                        end = Offset(left + barWidth, top),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Draw horizontal labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
