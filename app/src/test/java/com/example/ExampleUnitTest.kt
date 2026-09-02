package com.example

import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun solarMetrics_containsRealtimeAmpereAndWattage() {
        val metrics = SolarMetrics(
            isSolarOn = true,
            isGridOn = true,
            panelVoltage = 18.3f,
            panelCurrent = 1.53f,
            panelPower = 28f,
            gridVoltage = 220f,
            gridCurrent = 0.55f,
            gridPower = 120f,
            batteryVoltage = 12.6f,
            batteryCurrent = 1.2f,
            batteryPower = 15.12f,
            loadVoltage = 12.0f,
            loadCurrent = 1.93f,
            loadPower = 23f
        )

        // Verify Real-time Wattage and Ampere values
        assertEquals(28f, metrics.panelPower, 0.01f)
        assertEquals(1.53f, metrics.panelCurrent, 0.01f)

        assertEquals(120f, metrics.gridPower, 0.01f)
        assertEquals(0.55f, metrics.gridCurrent, 0.01f)

        assertEquals(15.12f, metrics.batteryPower, 0.01f)
        assertEquals(1.2f, metrics.batteryCurrent, 0.01f)

        assertEquals(23f, metrics.loadPower, 0.01f)
        assertEquals(1.93f, metrics.loadCurrent, 0.01f)

        // Verify active source flags
        assertTrue("Solar should be active", metrics.isSolarOn && metrics.panelPower > 0f)
        assertTrue("Grid should be active", metrics.isGridOn && metrics.gridPower > 0f)
        assertTrue("Battery should be active", metrics.batteryCurrent != 0f)
    }
}

