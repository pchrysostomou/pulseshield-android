package com.pulseshield.app

enum class RiskLevel(val label: String) {
    Low("Low"),
    Guarded("Guarded"),
    Elevated("Elevated"),
    High("High"),
}

enum class ExposureLevel(val label: String, val points: Int) {
    None("No known exposure", 0),
    Nearby("Nearby alert", 18),
    Close("Close contact", 34),
}

enum class Symptom(val label: String, val points: Int) {
    Fever("Fever", 18),
    Cough("Persistent cough", 12),
    Breathless("Breathlessness", 24),
    Fatigue("Strong fatigue", 8),
    Taste("Taste or smell change", 14),
}

enum class PlanType(val label: String) {
    Essentials("Essentials"),
    Commute("Commute"),
    FamilyVisit("Family visit"),
    IndoorEvent("Indoor event"),
}

enum class ProtectionLevel(val label: String, val reduction: Int) {
    Standard("Standard", 0),
    BetterVentilation("Better ventilation", 8),
    Strong("Mask + short visit", 14),
}

enum class HealthFocus(val label: String, val detail: String) {
    Respiratory("Respiratory", "Everyday cough, cold, and local illness signals"),
    CovidLike("COVID-like", "Exposure, cough, fever, and taste or smell changes"),
    FluCold("Flu/cold", "Fever, fatigue, and short-term symptom changes"),
    VulnerableVisit("Vulnerable visit", "Extra caution before close contact"),
}

enum class InsightPriority(val label: String) {
    Calm("Calm"),
    Watch("Watch"),
    Act("Act"),
}

enum class DecisionStatus(val label: String) {
    Go("Go"),
    Adjust("Adjust"),
    Limit("Limit"),
    StayHome("Stay home"),
}

data class RiskInput(
    val symptoms: Set<Symptom>,
    val exposureLevel: ExposureLevel,
    val localAlertLevel: Int,
    val vulnerableProfile: Boolean,
    val recentNegativeTest: Boolean,
    val planType: PlanType = PlanType.Essentials,
    val protectionLevel: ProtectionLevel = ProtectionLevel.Standard,
    val healthFocus: HealthFocus = HealthFocus.Respiratory,
)

data class RiskResult(
    val score: Int,
    val level: RiskLevel,
    val headline: String,
    val summary: String,
    val readinessScore: Int,
    val confidenceScore: Int,
    val primaryRecommendation: String,
    val decisionAdvice: DecisionAdvice,
    val checkAgainIn: String,
    val coachTitle: String,
    val coachSubtitle: String,
    val planAdvice: PlanAdvice,
    val focusAdvice: FocusAdvice,
    val nextActions: List<String>,
    val factors: List<RiskFactor>,
    val insights: List<CoachInsight>,
)

data class RiskFactor(
    val label: String,
    val points: Int,
    val detail: String,
)

data class CoachInsight(
    val title: String,
    val detail: String,
    val priority: InsightPriority,
)

data class PlanAdvice(
    val title: String,
    val detail: String,
    val saferSwap: String,
    val status: String,
)

data class FocusAdvice(
    val title: String,
    val detail: String,
    val action: String,
    val priority: InsightPriority,
)

data class DecisionAdvice(
    val status: DecisionStatus,
    val title: String,
    val detail: String,
    val primaryAction: String,
    val privacyNote: String,
)

data class RiskScenario(
    val title: String,
    val detail: String,
    val projectedScore: Int,
    val scoreDelta: Int,
    val projectedLevel: RiskLevel,
)

class HealthRiskEngine {
    fun assess(input: RiskInput): RiskResult {
        val symptomScore = input.symptoms.sumOf { it.points }
        val communityScore = input.localAlertLevel.coerceIn(0, 4) * 8
        val vulnerabilityScore = if (input.vulnerableProfile) 12 else 0
        val negativeTestAdjustment = if (input.recentNegativeTest) 12 else 0
        val protectionAdjustment = input.protectionLevel.reduction
        val focusScore = focusAdjustmentFor(input)

        val score = (
            symptomScore +
                input.exposureLevel.points +
                communityScore +
                focusScore +
                vulnerabilityScore -
                negativeTestAdjustment -
                protectionAdjustment
            ).coerceIn(0, 100)

        val level = when {
            score >= 72 -> RiskLevel.High
            score >= 48 -> RiskLevel.Elevated
            score >= 24 -> RiskLevel.Guarded
            else -> RiskLevel.Low
        }

        return RiskResult(
            score = score,
            level = level,
            headline = headlineFor(level),
            summary = summaryFor(level, input),
            readinessScore = readinessFor(score, input),
            confidenceScore = confidenceFor(input),
            primaryRecommendation = primaryRecommendationFor(level),
            decisionAdvice = decisionAdviceFor(level, input),
            checkAgainIn = checkAgainInFor(level),
            coachTitle = coachTitleFor(level),
            coachSubtitle = coachSubtitleFor(level, input),
            planAdvice = planAdviceFor(level, input),
            focusAdvice = focusAdviceFor(level, input),
            nextActions = actionsFor(level, input),
            factors = factorsFor(
                input = input,
                symptomScore = symptomScore,
                communityScore = communityScore,
                focusScore = focusScore,
                vulnerabilityScore = vulnerabilityScore,
                negativeTestAdjustment = negativeTestAdjustment,
                protectionAdjustment = protectionAdjustment,
            ),
            insights = insightsFor(input, level),
        )
    }

    fun compareSaferChoices(input: RiskInput): List<RiskScenario> {
        val currentScore = assess(input).score
        val scenarios = listOf(
            scenarioFor(
                title = "Take a same-day test",
                detail = "If you can confirm a recent negative test.",
                currentScore = currentScore,
                input = input.copy(recentNegativeTest = true),
            ),
            scenarioFor(
                title = "Avoid close exposure",
                detail = "Switch to no known exposure for this plan.",
                currentScore = currentScore,
                input = input.copy(exposureLevel = ExposureLevel.None),
            ),
            scenarioFor(
                title = "Choose a quieter area",
                detail = "Use a lower-pressure venue or time window.",
                currentScore = currentScore,
                input = input.copy(localAlertLevel = (input.localAlertLevel - 2).coerceAtLeast(0)),
            ),
            scenarioFor(
                title = "Use stronger protection",
                detail = "Add better ventilation, a mask, or a shorter indoor visit.",
                currentScore = currentScore,
                input = input.copy(protectionLevel = ProtectionLevel.Strong),
            ),
        )

        return scenarios
            .filter { it.scoreDelta < 0 }
            .distinctBy { it.projectedScore to it.title }
            .sortedBy { it.projectedScore }
            .take(3)
    }

    private fun scenarioFor(
        title: String,
        detail: String,
        currentScore: Int,
        input: RiskInput,
    ): RiskScenario {
        val projected = assess(input)
        return RiskScenario(
            title = title,
            detail = detail,
            projectedScore = projected.score,
            scoreDelta = projected.score - currentScore,
            projectedLevel = projected.level,
        )
    }

    private fun focusAdjustmentFor(input: RiskInput): Int =
        when (input.healthFocus) {
            HealthFocus.Respiratory -> 0
            HealthFocus.CovidLike -> {
                val covidSymptomScore = when {
                    input.symptoms.contains(Symptom.Taste) -> 10
                    input.symptoms.contains(Symptom.Cough) && input.symptoms.contains(Symptom.Fever) -> 8
                    input.symptoms.contains(Symptom.Cough) -> 4
                    else -> 0
                }
                val exposureScore = if (input.exposureLevel != ExposureLevel.None) 6 else 0
                covidSymptomScore + exposureScore
            }
            HealthFocus.FluCold -> {
                val feverFatigueScore = listOf(Symptom.Fever, Symptom.Fatigue)
                    .count { input.symptoms.contains(it) } * 4
                val areaScore = if (input.localAlertLevel >= 3) 4 else 0
                feverFatigueScore + areaScore
            }
            HealthFocus.VulnerableVisit -> {
                val visitScore = if (input.planType == PlanType.FamilyVisit) 8 else 4
                val symptomScore = if (input.symptoms.isNotEmpty()) 4 else 0
                visitScore + symptomScore
            }
        }

    private fun headlineFor(level: RiskLevel): String =
        when (level) {
            RiskLevel.Low -> "Keep normal precautions"
            RiskLevel.Guarded -> "Watch changes today"
            RiskLevel.Elevated -> "Reduce close contact"
            RiskLevel.High -> "Act now and seek guidance"
        }

    private fun summaryFor(level: RiskLevel, input: RiskInput): String =
        when (level) {
            RiskLevel.Low -> "Your current signals look calm. Keep your check-ins private and stay aware of local updates."
            RiskLevel.Guarded -> "There are enough signals to be careful today, especially around crowded indoor places."
            RiskLevel.Elevated -> "Symptoms, exposure, or local pressure suggest you should reduce close contact for now."
            RiskLevel.High -> if (input.vulnerableProfile) {
                "Your profile and current signals deserve prompt official or clinical guidance."
            } else {
                "Your current signals are strong enough to pause plans and follow official guidance."
            }
        }

    private fun readinessFor(score: Int, input: RiskInput): Int {
        val recentTestBoost = if (input.recentNegativeTest) 8 else 0
        val protectionBoost = input.protectionLevel.reduction / 2
        val cautionPenalty = if (input.vulnerableProfile && score >= 24) 8 else 0
        return (100 - score + recentTestBoost + protectionBoost - cautionPenalty).coerceIn(0, 100)
    }

    private fun confidenceFor(input: RiskInput): Int {
        val symptomSignal = if (input.symptoms.isNotEmpty()) 14 else 0
        val exposureSignal = if (input.exposureLevel != ExposureLevel.None) 10 else 0
        val areaSignal = input.localAlertLevel.coerceIn(0, 4) * 4
        val testSignal = if (input.recentNegativeTest) 8 else 0
        val protectionSignal = if (input.protectionLevel != ProtectionLevel.Standard) 4 else 0
        val focusSignal = if (input.healthFocus != HealthFocus.Respiratory) 6 else 0
        return (
            62 +
                symptomSignal +
                exposureSignal +
                areaSignal +
                testSignal +
                protectionSignal +
                focusSignal
            ).coerceIn(62, 96)
    }

    private fun primaryRecommendationFor(level: RiskLevel): String =
        when (level) {
            RiskLevel.Low -> "Good for normal plans"
            RiskLevel.Guarded -> "Choose safer spaces"
            RiskLevel.Elevated -> "Delay high-contact plans"
            RiskLevel.High -> "Pause plans and get guidance"
        }

    private fun decisionAdviceFor(level: RiskLevel, input: RiskInput): DecisionAdvice {
        if (level != RiskLevel.High && input.symptoms.contains(Symptom.Breathless)) {
            return DecisionAdvice(
                status = DecisionStatus.Limit,
                title = "Use guidance before going",
                detail = "Breathlessness changes the decision even when the total score is not high.",
                primaryAction = "Pause the plan if breathing feels difficult",
                privacyNote = "No sharing is needed to get this recommendation.",
            )
        }

        return when (level) {
            RiskLevel.Low -> DecisionAdvice(
                status = DecisionStatus.Go,
                title = "Plan can go ahead",
                detail = "Your signals support the plan as long as normal precautions still fit the setting.",
                primaryAction = "Go with normal care",
                privacyNote = "Keep symptoms and check-ins on this device.",
            )
            RiskLevel.Guarded -> DecisionAdvice(
                status = DecisionStatus.Adjust,
                title = "Keep the plan, make it safer",
                detail = "A smaller change should lower risk without cancelling the whole plan.",
                primaryAction = "Choose a quieter, shorter, or better-ventilated version",
                privacyNote = "Use private place memory; no community notice is needed yet.",
            )
            RiskLevel.Elevated -> DecisionAdvice(
                status = DecisionStatus.Limit,
                title = "Limit close contact",
                detail = "The safer move is to reduce crowded indoor time and delay high-contact plans.",
                primaryAction = if (input.planType == PlanType.FamilyVisit) {
                    "Delay the visit or move it outdoors"
                } else {
                    "Shorten the plan or switch to a low-contact option"
                },
                privacyNote = "Keep a local record in case symptoms or guidance change.",
            )
            RiskLevel.High -> DecisionAdvice(
                status = DecisionStatus.StayHome,
                title = "Stay home and get guidance",
                detail = "Current signals are strong enough that trusted guidance should come before the plan.",
                primaryAction = "Pause non-essential activity",
                privacyNote = "Use guidance first; sharing any place notice should stay optional.",
            )
        }
    }

    private fun checkAgainInFor(level: RiskLevel): String =
        when (level) {
            RiskLevel.Low -> "Tomorrow morning"
            RiskLevel.Guarded -> "This evening"
            RiskLevel.Elevated -> "In 4-6 hours"
            RiskLevel.High -> "After official guidance"
        }

    private fun coachTitleFor(level: RiskLevel): String =
        when (level) {
            RiskLevel.Low -> "Your day looks clear"
            RiskLevel.Guarded -> "Make one safer swap"
            RiskLevel.Elevated -> "Lower contact for now"
            RiskLevel.High -> "Use official guidance first"
        }

    private fun coachSubtitleFor(level: RiskLevel, input: RiskInput): String =
        when (level) {
            RiskLevel.Low -> "Keep normal plans, then check local signals tomorrow."
            RiskLevel.Guarded -> if (input.exposureLevel == ExposureLevel.Nearby) {
                "Pick quieter spaces and keep your next check close."
            } else {
                "Your signals are manageable if plans stay low-density."
            }
            RiskLevel.Elevated -> "Move social plans outdoors or delay high-contact visits."
            RiskLevel.High -> "Pause non-essential plans until you have trusted advice."
        }

    private fun planAdviceFor(level: RiskLevel, input: RiskInput): PlanAdvice {
        if (level == RiskLevel.High) {
            return PlanAdvice(
                title = "Pause this plan",
                detail = "Your current signals are too strong for non-essential activity.",
                saferSwap = "Use remote help or official guidance first.",
                status = "Hold",
            )
        }

        return when (input.planType) {
            PlanType.Essentials -> when (level) {
                RiskLevel.Low -> PlanAdvice(
                    title = "Good to go",
                    detail = "Keep it short and choose a quiet time if possible.",
                    saferSwap = "Use the 14:00 low-crowd window.",
                    status = "Go",
                )
                RiskLevel.Guarded -> PlanAdvice(
                    title = "Go with care",
                    detail = "Essential errands are fine if you reduce indoor time.",
                    saferSwap = "Pick the pharmacy or outdoor market option.",
                    status = "Care",
                )
                else -> PlanAdvice(
                    title = "Only if necessary",
                    detail = "Keep the trip short and avoid peak queues.",
                    saferSwap = "Ask someone else or choose delivery if available.",
                    status = "Limit",
                )
            }

            PlanType.Commute -> when (level) {
                RiskLevel.Low -> PlanAdvice(
                    title = "Commute normally",
                    detail = "Your signals support normal travel precautions.",
                    saferSwap = "Use quieter carriages if available.",
                    status = "Go",
                )
                RiskLevel.Guarded -> PlanAdvice(
                    title = "Shift the commute",
                    detail = "Avoid the busiest windows and keep the journey direct.",
                    saferSwap = "Travel after the morning peak.",
                    status = "Adjust",
                )
                else -> PlanAdvice(
                    title = "Reduce travel",
                    detail = "Your current risk makes crowded transport a poor fit.",
                    saferSwap = "Work remotely or use a low-contact route.",
                    status = "Limit",
                )
            }

            PlanType.FamilyVisit -> when (level) {
                RiskLevel.Low -> PlanAdvice(
                    title = "Visit looks reasonable",
                    detail = "Keep ventilation and symptom awareness in mind.",
                    saferSwap = "Meet outdoors if anyone is vulnerable.",
                    status = "Go",
                )
                RiskLevel.Guarded -> PlanAdvice(
                    title = "Make the visit safer",
                    detail = "The visit can work if it is short and ventilated.",
                    saferSwap = "Meet outside or delay if symptoms rise.",
                    status = "Adjust",
                )
                else -> PlanAdvice(
                    title = "Delay the visit",
                    detail = "Close contact with family is not ideal with current signals.",
                    saferSwap = "Call today and check again later.",
                    status = "Delay",
                )
            }

            PlanType.IndoorEvent -> when (level) {
                RiskLevel.Low -> PlanAdvice(
                    title = "Choose a safer spot",
                    detail = "Indoor events are okay if crowding and ventilation are good.",
                    saferSwap = "Stay near airflow and avoid long queues.",
                    status = "Care",
                )
                RiskLevel.Guarded -> PlanAdvice(
                    title = "Consider skipping",
                    detail = "A crowded indoor event adds avoidable risk today.",
                    saferSwap = "Pick an outdoor plan or off-peak venue.",
                    status = "Avoid",
                )
                else -> PlanAdvice(
                    title = "Skip this event",
                    detail = "Your current risk does not match crowded indoor contact.",
                    saferSwap = "Choose a remote or outdoor alternative.",
                    status = "Avoid",
                )
            }
        }
    }

    private fun focusAdviceFor(level: RiskLevel, input: RiskInput): FocusAdvice =
        when (input.healthFocus) {
            HealthFocus.Respiratory -> FocusAdvice(
                title = "Respiratory watch",
                detail = "Use symptoms, exposure, and local pressure to choose the safest version of the plan.",
                action = if (level == RiskLevel.Low) "Keep watching" else "Reduce indoor time",
                priority = if (level == RiskLevel.Low) InsightPriority.Calm else InsightPriority.Watch,
            )
            HealthFocus.CovidLike -> FocusAdvice(
                title = "COVID-like lens",
                detail = "Exposure, cough, fever, or taste and smell changes make testing and close-contact caution more important.",
                action = if (level.ordinal >= RiskLevel.Elevated.ordinal) "Test and limit contact" else "Test if symptoms rise",
                priority = if (level.ordinal >= RiskLevel.Elevated.ordinal) InsightPriority.Act else InsightPriority.Watch,
            )
            HealthFocus.FluCold -> FocusAdvice(
                title = "Flu/cold lens",
                detail = "Fever and fatigue are weighted toward rest, shorter errands, and avoiding crowded indoor stops.",
                action = if (level == RiskLevel.Low) "Keep plans light" else "Rest and shorten plans",
                priority = if (level == RiskLevel.Low) InsightPriority.Calm else InsightPriority.Watch,
            )
            HealthFocus.VulnerableVisit -> FocusAdvice(
                title = "Vulnerable visit lens",
                detail = "Advice becomes stricter before close contact with elderly, immunocompromised, or otherwise at-risk people.",
                action = if (level == RiskLevel.Low) "Ventilate and check" else "Delay or move outdoors",
                priority = if (level == RiskLevel.Low) InsightPriority.Watch else InsightPriority.Act,
            )
        }

    private fun actionsFor(level: RiskLevel, input: RiskInput): List<String> {
        val coreActions = when (level) {
            RiskLevel.Low -> listOf(
                "Keep check-ins local",
                "Review area updates",
                "Use indoor ventilation reminders",
            )

            RiskLevel.Guarded -> listOf(
                "Repeat the check-in later today",
                "Avoid crowded indoor spaces",
                "Keep a private log of close contacts",
            )

            RiskLevel.Elevated -> listOf(
                "Limit high-risk visits",
                "Consider testing if symptoms continue",
                "Share an anonymous warning with recent venues",
            )

            RiskLevel.High -> listOf(
                "Follow official local health guidance",
                "Avoid contact with vulnerable people",
                "Get clinical help if symptoms worsen",
            )
        }

        val breathingSafetyAction = if (input.symptoms.contains(Symptom.Breathless)) {
            listOf("Use trusted guidance if breathing becomes difficult")
        } else {
            emptyList()
        }

        val profileAction = if (input.vulnerableProfile && level != RiskLevel.Low) {
            listOf("Prioritize medical advice for your profile")
        } else {
            emptyList()
        }

        val focusActions = when (input.healthFocus) {
            HealthFocus.Respiratory -> emptyList()
            HealthFocus.CovidLike -> listOf("Treat COVID-like signals as test-and-limit-contact until clearer")
            HealthFocus.FluCold -> listOf("Keep plans short while fever or fatigue is active")
            HealthFocus.VulnerableVisit -> listOf("Avoid vulnerable close contact if symptoms or exposure are active")
        }

        return coreActions + focusActions + breathingSafetyAction + profileAction
    }

    private fun factorsFor(
        input: RiskInput,
        symptomScore: Int,
        communityScore: Int,
        focusScore: Int,
        vulnerabilityScore: Int,
        negativeTestAdjustment: Int,
        protectionAdjustment: Int,
    ): List<RiskFactor> {
        val factors = mutableListOf<RiskFactor>()

        if (symptomScore > 0) {
            val symptomNames = input.symptoms
                .sortedBy { it.ordinal }
                .joinToString { it.label.lowercase() }
            factors += RiskFactor(
                label = "Symptoms",
                points = symptomScore,
                detail = symptomNames,
            )
        }

        if (input.exposureLevel.points > 0) {
            factors += RiskFactor(
                label = "Exposure",
                points = input.exposureLevel.points,
                detail = input.exposureLevel.label,
            )
        }

        if (communityScore > 0) {
            factors += RiskFactor(
                label = "Area pressure",
                points = communityScore,
                detail = "Local alert level ${input.localAlertLevel.coerceIn(0, 4)}",
            )
        }

        if (focusScore > 0) {
            factors += RiskFactor(
                label = "Focus",
                points = focusScore,
                detail = input.healthFocus.label,
            )
        }

        if (vulnerabilityScore > 0) {
            factors += RiskFactor(
                label = "Profile",
                points = vulnerabilityScore,
                detail = "Extra caution selected",
            )
        }

        if (negativeTestAdjustment > 0) {
            factors += RiskFactor(
                label = "Recent test",
                points = -negativeTestAdjustment,
                detail = "Risk reduced, not removed",
            )
        }

        if (protectionAdjustment > 0) {
            factors += RiskFactor(
                label = "Protection",
                points = -protectionAdjustment,
                detail = input.protectionLevel.label,
            )
        }

        return if (factors.isEmpty()) {
            listOf(
                RiskFactor(
                    label = "Baseline",
                    points = 0,
                    detail = "No symptoms, exposure, or local pressure selected",
                ),
            )
        } else {
            factors
        }
    }

    private fun insightsFor(input: RiskInput, level: RiskLevel): List<CoachInsight> {
        val insights = mutableListOf<CoachInsight>()

        if (input.symptoms.isEmpty()) {
            insights += CoachInsight(
                title = "Symptoms baseline is clear",
                detail = "No symptoms selected in this check.",
                priority = InsightPriority.Calm,
            )
        } else {
            insights += CoachInsight(
                title = "${input.symptoms.size} symptom signals",
                detail = input.symptoms.sortedBy { it.ordinal }.joinToString { it.label },
                priority = if (level == RiskLevel.High) InsightPriority.Act else InsightPriority.Watch,
            )
        }

        if (input.symptoms.contains(Symptom.Breathless)) {
            insights += CoachInsight(
                title = "Breathing symptom needs attention",
                detail = "Use trusted guidance quickly if breathing becomes difficult.",
                priority = InsightPriority.Act,
            )
        }

        if (input.exposureLevel != ExposureLevel.None) {
            insights += CoachInsight(
                title = "Exposure is active",
                detail = "${input.exposureLevel.label} is included in today's score.",
                priority = if (input.exposureLevel == ExposureLevel.Close) InsightPriority.Act else InsightPriority.Watch,
            )
        }

        if (input.healthFocus != HealthFocus.Respiratory) {
            insights += CoachInsight(
                title = "${input.healthFocus.label} lens is active",
                detail = input.healthFocus.detail,
                priority = input.focusAdvicePriority(level),
            )
        }

        if (input.localAlertLevel >= 3) {
            insights += CoachInsight(
                title = "Area pressure is high",
                detail = "Local alert level ${input.localAlertLevel.coerceIn(0, 4)} means quieter venues matter more.",
                priority = InsightPriority.Watch,
            )
        }

        if (input.recentNegativeTest) {
            insights += CoachInsight(
                title = "Recent test helps",
                detail = "It reduces the score, but does not replace symptom monitoring.",
                priority = InsightPriority.Calm,
            )
        }

        if (input.protectionLevel != ProtectionLevel.Standard) {
            insights += CoachInsight(
                title = "Protection is reducing risk",
                detail = "${input.protectionLevel.label} is included in this score.",
                priority = InsightPriority.Calm,
            )
        }

        if (input.vulnerableProfile) {
            insights += CoachInsight(
                title = "Profile needs extra caution",
                detail = "Guidance is stricter because an at-risk profile is selected.",
                priority = InsightPriority.Act,
            )
        }

        return insights.take(4)
    }

    private fun RiskInput.focusAdvicePriority(level: RiskLevel): InsightPriority =
        when (healthFocus) {
            HealthFocus.Respiratory -> InsightPriority.Calm
            HealthFocus.CovidLike -> if (level.ordinal >= RiskLevel.Elevated.ordinal) InsightPriority.Act else InsightPriority.Watch
            HealthFocus.FluCold -> if (level == RiskLevel.Low) InsightPriority.Calm else InsightPriority.Watch
            HealthFocus.VulnerableVisit -> if (level == RiskLevel.Low) InsightPriority.Watch else InsightPriority.Act
        }
}
