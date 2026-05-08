package com.pulseshield.app

data class CheckInSummary(
    val totalCheckIns: Int,
    val minutesLogged: Int,
    val highestRiskLevel: RiskLevel,
    val attentionCount: Int,
    val privateWindow: String,
    val alertReadiness: String,
    val anonymousAlertDetail: String,
    val canShareAnonymousAlert: Boolean,
    val headline: String,
    val detail: String,
    val recommendation: String,
)

class CheckInAnalyzer {
    fun summarize(checkIns: List<PlaceCheckIn>): CheckInSummary {
        if (checkIns.isEmpty()) {
            return CheckInSummary(
                totalCheckIns = 0,
                minutesLogged = 0,
                highestRiskLevel = RiskLevel.Low,
                attentionCount = 0,
                privateWindow = "No exposure window",
                alertReadiness = "Not ready",
                anonymousAlertDetail = "No places have been stored on this device.",
                canShareAnonymousAlert = false,
                headline = "No local visits yet",
                detail = "Scan a place code to build a private timeline on this device.",
                recommendation = "Start with short, low-density check-ins.",
            )
        }

        val highestRisk = checkIns.maxBy { it.riskLevel.ordinal }.riskLevel
        val attentionCount = checkIns.count { it.riskLevel.ordinal >= RiskLevel.Guarded.ordinal }
        val minutesLogged = checkIns.sumOf { it.dwellMinutes() }
        val canShareAnonymousAlert = highestRisk.ordinal >= RiskLevel.Elevated.ordinal || attentionCount >= 2

        return CheckInSummary(
            totalCheckIns = checkIns.size,
            minutesLogged = minutesLogged,
            highestRiskLevel = highestRisk,
            attentionCount = attentionCount,
            privateWindow = privateWindowFor(minutesLogged),
            alertReadiness = if (canShareAnonymousAlert) "Anonymous alert ready" else "Keep local",
            anonymousAlertDetail = anonymousAlertDetailFor(canShareAnonymousAlert),
            canShareAnonymousAlert = canShareAnonymousAlert,
            headline = headlineFor(highestRisk),
            detail = detailFor(highestRisk, attentionCount),
            recommendation = recommendationFor(highestRisk),
        )
    }

    private fun PlaceCheckIn.dwellMinutes(): Int =
        dwellTime
            .takeWhile { it.isDigit() }
            .toIntOrNull()
            ?: 0

    private fun privateWindowFor(minutesLogged: Int): String =
        if (minutesLogged >= 60) {
            "Same-day venue window"
        } else {
            "Recent local window"
        }

    private fun anonymousAlertDetailFor(canShareAnonymousAlert: Boolean): String =
        if (canShareAnonymousAlert) {
            "You can warn recent venues without sending your identity or full timeline."
        } else {
            "Keep this timeline private unless symptoms or trusted guidance change."
        }

    private fun headlineFor(level: RiskLevel): String =
        when (level) {
            RiskLevel.Low -> "Your places look calm"
            RiskLevel.Guarded -> "Some places need attention"
            RiskLevel.Elevated -> "One recent place stands out"
            RiskLevel.High -> "High-risk place detected"
        }

    private fun detailFor(level: RiskLevel, attentionCount: Int): String =
        when (level) {
            RiskLevel.Low -> "No check-ins are above low risk."
            RiskLevel.Guarded -> "$attentionCount check-ins need light caution."
            RiskLevel.Elevated -> "$attentionCount check-ins deserve symptom monitoring."
            RiskLevel.High -> "$attentionCount check-ins need official guidance if symptoms appear."
        }

    private fun recommendationFor(level: RiskLevel): String =
        when (level) {
            RiskLevel.Low -> "Keep using local check-ins for private memory."
            RiskLevel.Guarded -> "Prefer quieter windows for your next stop."
            RiskLevel.Elevated -> "Avoid stacking more crowded indoor visits today."
            RiskLevel.High -> "Pause new check-ins and follow trusted guidance."
        }
}
