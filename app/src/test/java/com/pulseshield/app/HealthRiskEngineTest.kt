package com.pulseshield.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthRiskEngineTest {
    private val engine = HealthRiskEngine()

    @Test
    fun lowRiskWhenNoSymptomsAndNoExposure() {
        val result = engine.assess(
            RiskInput(
                symptoms = emptySet(),
                exposureLevel = ExposureLevel.None,
                localAlertLevel = 0,
                vulnerableProfile = false,
                recentNegativeTest = false,
            ),
        )

        assertEquals(RiskLevel.Low, result.level)
        assertEquals(0, result.score)
        assertEquals(100, result.readinessScore)
        assertTrue(result.confidenceScore >= 62)
        assertEquals("Good for normal plans", result.primaryRecommendation)
        assertEquals(DecisionStatus.Go, result.decisionAdvice.status)
        assertEquals("Plan can go ahead", result.decisionAdvice.title)
        assertEquals("Good to go", result.planAdvice.title)
        assertEquals("Go", result.planAdvice.status)
        assertEquals("Your day looks clear", result.coachTitle)
        assertEquals("Baseline", result.factors.single().label)
        assertTrue(result.insights.any { it.priority == InsightPriority.Calm })
    }

    @Test
    fun highRiskWhenSymptomsExposureAndAreaPressureCombine() {
        val result = engine.assess(
            RiskInput(
                symptoms = setOf(Symptom.Fever, Symptom.Breathless, Symptom.Taste),
                exposureLevel = ExposureLevel.Close,
                localAlertLevel = 4,
                vulnerableProfile = true,
                recentNegativeTest = false,
                planType = PlanType.FamilyVisit,
            ),
        )

        assertEquals(RiskLevel.High, result.level)
        assertTrue(result.score >= 72)
        assertTrue(result.nextActions.any { it.contains("official local health guidance") })
        assertTrue(result.summary.contains("guidance"))
        assertTrue(result.readinessScore < 30)
        assertTrue(result.confidenceScore > 80)
        assertEquals("Pause plans and get guidance", result.primaryRecommendation)
        assertEquals(DecisionStatus.StayHome, result.decisionAdvice.status)
        assertEquals("Stay home and get guidance", result.decisionAdvice.title)
        assertEquals("Pause this plan", result.planAdvice.title)
        assertEquals("Hold", result.planAdvice.status)
        assertEquals("Use official guidance first", result.coachTitle)
        assertTrue(result.factors.any { it.label == "Symptoms" })
        assertTrue(result.factors.any { it.label == "Profile" })
        assertTrue(result.insights.any { it.priority == InsightPriority.Act })
    }

    @Test
    fun recentNegativeTestReducesButDoesNotEraseRisk() {
        val withoutTest = engine.assess(
            RiskInput(
                symptoms = setOf(Symptom.Cough, Symptom.Fatigue),
                exposureLevel = ExposureLevel.Nearby,
                localAlertLevel = 2,
                vulnerableProfile = false,
                recentNegativeTest = false,
            ),
        )
        val withTest = engine.assess(
            RiskInput(
                symptoms = setOf(Symptom.Cough, Symptom.Fatigue),
                exposureLevel = ExposureLevel.Nearby,
                localAlertLevel = 2,
                vulnerableProfile = false,
                recentNegativeTest = true,
            ),
        )

        assertTrue(withTest.score < withoutTest.score)
        assertTrue(withTest.readinessScore > withoutTest.readinessScore)
        assertTrue(withTest.score > 0)
        assertTrue(withTest.factors.any { it.points < 0 })
        assertTrue(withTest.insights.any { it.title == "Recent test helps" })
    }

    @Test
    fun familyVisitAdviceDelaysWhenRiskIsElevated() {
        val result = engine.assess(
            RiskInput(
                symptoms = setOf(Symptom.Cough, Symptom.Fatigue),
                exposureLevel = ExposureLevel.Nearby,
                localAlertLevel = 3,
                vulnerableProfile = false,
                recentNegativeTest = false,
                planType = PlanType.FamilyVisit,
            ),
        )

        assertEquals(RiskLevel.Elevated, result.level)
        assertEquals("Delay the visit", result.planAdvice.title)
        assertEquals("Delay", result.planAdvice.status)
    }

    @Test
    fun saferChoiceComparisonShowsOnlyImprovingScenarios() {
        val scenarios = engine.compareSaferChoices(
            RiskInput(
                symptoms = setOf(Symptom.Cough, Symptom.Fatigue),
                exposureLevel = ExposureLevel.Nearby,
                localAlertLevel = 3,
                vulnerableProfile = false,
                recentNegativeTest = false,
            ),
        )

        assertTrue(scenarios.isNotEmpty())
        assertTrue(scenarios.all { it.scoreDelta < 0 })
        assertEquals(scenarios.sortedBy { it.projectedScore }, scenarios)
    }

    @Test
    fun strongerProtectionReducesScoreAndAppearsInExplanations() {
        val standard = engine.assess(
            RiskInput(
                symptoms = setOf(Symptom.Cough, Symptom.Fatigue),
                exposureLevel = ExposureLevel.Nearby,
                localAlertLevel = 3,
                vulnerableProfile = false,
                recentNegativeTest = false,
                protectionLevel = ProtectionLevel.Standard,
            ),
        )
        val protected = engine.assess(
            RiskInput(
                symptoms = setOf(Symptom.Cough, Symptom.Fatigue),
                exposureLevel = ExposureLevel.Nearby,
                localAlertLevel = 3,
                vulnerableProfile = false,
                recentNegativeTest = false,
                protectionLevel = ProtectionLevel.Strong,
            ),
        )

        assertTrue(protected.score < standard.score)
        assertTrue(protected.readinessScore > standard.readinessScore)
        assertTrue(protected.factors.any { it.label == "Protection" && it.points < 0 })
        assertTrue(protected.insights.any { it.title == "Protection is reducing risk" })
    }

    @Test
    fun breathlessnessAddsSafetyGuidanceEvenAtGuardedRisk() {
        val result = engine.assess(
            RiskInput(
                symptoms = setOf(Symptom.Breathless),
                exposureLevel = ExposureLevel.None,
                localAlertLevel = 0,
                vulnerableProfile = false,
                recentNegativeTest = false,
            ),
        )

        assertEquals(RiskLevel.Guarded, result.level)
        assertEquals(DecisionStatus.Limit, result.decisionAdvice.status)
        assertTrue(result.nextActions.any { it.contains("breathing becomes difficult") })
        assertTrue(
            result.insights.any {
                it.title == "Breathing symptom needs attention" &&
                    it.priority == InsightPriority.Act
            },
        )
    }

    @Test
    fun covidLikeFocusRaisesPriorityAndExplainsTheLens() {
        val respiratory = engine.assess(
            RiskInput(
                symptoms = setOf(Symptom.Cough, Symptom.Taste),
                exposureLevel = ExposureLevel.Nearby,
                localAlertLevel = 1,
                vulnerableProfile = false,
                recentNegativeTest = false,
                healthFocus = HealthFocus.Respiratory,
            ),
        )
        val covidLike = engine.assess(
            RiskInput(
                symptoms = setOf(Symptom.Cough, Symptom.Taste),
                exposureLevel = ExposureLevel.Nearby,
                localAlertLevel = 1,
                vulnerableProfile = false,
                recentNegativeTest = false,
                healthFocus = HealthFocus.CovidLike,
            ),
        )

        assertTrue(covidLike.score > respiratory.score)
        assertEquals("COVID-like lens", covidLike.focusAdvice.title)
        assertTrue(covidLike.nextActions.any { it.contains("COVID-like signals") })
        assertTrue(covidLike.factors.any { it.label == "Focus" && it.points > 0 })
        assertTrue(covidLike.insights.any { it.title == "COVID-like lens is active" })
    }
}
