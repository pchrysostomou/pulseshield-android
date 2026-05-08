package com.pulseshield.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckInAnalyzerTest {
    private val analyzer = CheckInAnalyzer()

    @Test
    fun emptyCheckInsReturnCalmPrivateMemoryState() {
        val summary = analyzer.summarize(emptyList())

        assertEquals(0, summary.totalCheckIns)
        assertEquals(0, summary.minutesLogged)
        assertEquals(RiskLevel.Low, summary.highestRiskLevel)
        assertEquals("No local visits yet", summary.headline)
        assertEquals("Not ready", summary.alertReadiness)
        assertFalse(summary.canShareAnonymousAlert)
    }

    @Test
    fun summaryCountsMinutesAndAttentionPlaces() {
        val summary = analyzer.summarize(DemoData.checkIns)

        assertEquals(3, summary.totalCheckIns)
        assertEquals(77, summary.minutesLogged)
        assertEquals(RiskLevel.Elevated, summary.highestRiskLevel)
        assertEquals(2, summary.attentionCount)
        assertEquals("One recent place stands out", summary.headline)
        assertEquals("Same-day venue window", summary.privateWindow)
        assertEquals("Anonymous alert ready", summary.alertReadiness)
        assertTrue(summary.canShareAnonymousAlert)
    }
}
