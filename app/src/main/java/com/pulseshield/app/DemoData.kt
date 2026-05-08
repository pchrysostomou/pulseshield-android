package com.pulseshield.app

data class PlaceCheckIn(
    val name: String,
    val time: String,
    val signal: String,
    val riskLevel: RiskLevel,
    val dwellTime: String,
    val recommendation: String,
)

data class CommunitySignal(
    val title: String,
    val detail: String,
    val confidence: Int,
)

data class SafetyTask(
    val title: String,
    val detail: String,
    val complete: Boolean,
)

data class TrustMetric(
    val label: String,
    val value: String,
    val detail: String,
)

data class TrendPoint(
    val day: String,
    val score: Int,
)

data class CareResource(
    val name: String,
    val distance: String,
    val waitTime: String,
    val detail: String,
    val openNow: Boolean,
)

data class GuidanceResource(
    val title: String,
    val region: String,
    val detail: String,
    val action: String,
    val urgent: Boolean,
)

data class DataPractice(
    val signal: String,
    val storage: String,
    val retention: String,
    val sharing: String,
)

data class TimeWindow(
    val label: String,
    val crowdLevel: String,
    val recommendation: String,
    val riskLevel: RiskLevel,
)

data class VenueSuggestion(
    val name: String,
    val category: String,
    val distance: String,
    val ventilation: String,
    val bestTime: String,
    val riskLevel: RiskLevel,
)

object DemoData {
    val checkIns = listOf(
        PlaceCheckIn(
            name = "Central Library",
            time = "08:42",
            signal = "Quiet indoor space",
            riskLevel = RiskLevel.Low,
            dwellTime = "22 min",
            recommendation = "Good place for a short visit",
        ),
        PlaceCheckIn(
            name = "Market Hall",
            time = "12:10",
            signal = "Crowded indoor alert",
            riskLevel = RiskLevel.Guarded,
            dwellTime = "41 min",
            recommendation = "Keep distance and leave if it gets busier",
        ),
        PlaceCheckIn(
            name = "North Station",
            time = "17:35",
            signal = "Short exposure window",
            riskLevel = RiskLevel.Elevated,
            dwellTime = "14 min",
            recommendation = "Watch symptoms for the next 48 hours",
        ),
    )

    val safetyTasks = listOf(
        SafetyTask(
            title = "Morning check complete",
            detail = "Risk score updated from local-only answers.",
            complete = true,
        ),
        SafetyTask(
            title = "Plan safer route",
            detail = "Prefer low-density indoor spaces today.",
            complete = false,
        ),
        SafetyTask(
            title = "Review before family visit",
            detail = "Run the check again if symptoms change.",
            complete = false,
        ),
    )

    val weeklyTrend = listOf(
        TrendPoint(day = "Mon", score = 18),
        TrendPoint(day = "Tue", score = 21),
        TrendPoint(day = "Wed", score = 19),
        TrendPoint(day = "Thu", score = 28),
        TrendPoint(day = "Fri", score = 32),
        TrendPoint(day = "Sat", score = 36),
        TrendPoint(day = "Now", score = 38),
    )

    val careResources = listOf(
        CareResource(
            name = "Official health advice",
            distance = "Remote",
            waitTime = "Now",
            detail = "Best first step for non-emergency symptom guidance.",
            openNow = true,
        ),
        CareResource(
            name = "Westside Pharmacy",
            distance = "0.6 mi",
            waitTime = "12 min",
            detail = "Testing kits and pharmacist advice available.",
            openNow = true,
        ),
        CareResource(
            name = "Riverside Clinic",
            distance = "1.4 mi",
            waitTime = "Tomorrow",
            detail = "Book if symptoms continue or risk rises.",
            openNow = false,
        ),
    )

    val guidanceResources = listOf(
        GuidanceResource(
            title = "Official health advice",
            region = "Your region",
            detail = "Non-emergency symptom guidance and next steps.",
            action = "Use first",
            urgent = true,
        ),
        GuidanceResource(
            title = "Local public health updates",
            region = "Local area",
            detail = "Respiratory illness advisories, venue notices, and school updates.",
            action = "Review today",
            urgent = false,
        ),
        GuidanceResource(
            title = "Emergency care",
            region = "Local services",
            detail = "Use emergency services when symptoms become severe or urgent.",
            action = "Urgent only",
            urgent = true,
        ),
    )

    val timeWindows = listOf(
        TimeWindow(
            label = "Now",
            crowdLevel = "Medium",
            recommendation = "Fine for short essential stops.",
            riskLevel = RiskLevel.Guarded,
        ),
        TimeWindow(
            label = "14:00",
            crowdLevel = "Low",
            recommendation = "Best window for errands today.",
            riskLevel = RiskLevel.Low,
        ),
        TimeWindow(
            label = "18:00",
            crowdLevel = "High",
            recommendation = "Avoid crowded indoor queues.",
            riskLevel = RiskLevel.Elevated,
        ),
    )

    val venueSuggestions = listOf(
        VenueSuggestion(
            name = "Greenway Pharmacy",
            category = "Pharmacy",
            distance = "0.4 mi",
            ventilation = "Good",
            bestTime = "14:00",
            riskLevel = RiskLevel.Low,
        ),
        VenueSuggestion(
            name = "Open Air Market",
            category = "Groceries",
            distance = "0.8 mi",
            ventilation = "Outdoor",
            bestTime = "10:30",
            riskLevel = RiskLevel.Low,
        ),
        VenueSuggestion(
            name = "City Mall",
            category = "Shopping",
            distance = "1.1 mi",
            ventilation = "Mixed",
            bestTime = "Avoid 18:00",
            riskLevel = RiskLevel.Elevated,
        ),
    )

    val signals = listOf(
        CommunitySignal(
            title = "Respiratory illness rising",
            detail = "Local trend is above the 14-day baseline.",
            confidence = 86,
        ),
        CommunitySignal(
            title = "Hospital pressure stable",
            detail = "Admissions remain within expected seasonal range.",
            confidence = 78,
        ),
        CommunitySignal(
            title = "Public venue ventilation",
            detail = "Recent reports favor low-density indoor routes.",
            confidence = 72,
        ),
    )

    val trustMetrics = listOf(
        TrustMetric(
            label = "Identity",
            value = "None",
            detail = "No account needed for core safety tools.",
        ),
        TrustMetric(
            label = "Check-ins",
            value = "Local",
            detail = "Venue history stays on this device.",
        ),
        TrustMetric(
            label = "Sharing",
            value = "Opt-in",
            detail = "Alerts can be anonymous and limited.",
        ),
        TrustMetric(
            label = "Logic",
            value = "Open",
            detail = "Risk rules are plain Kotlin and testable.",
        ),
    )

    val dataPractices = listOf(
        DataPractice(
            signal = "Symptoms",
            storage = "On device",
            retention = "User controlled",
            sharing = "Not shared",
        ),
        DataPractice(
            signal = "Place check-ins",
            storage = "On device",
            retention = "Auto-expire planned",
            sharing = "Anonymous optional",
        ),
        DataPractice(
            signal = "Area signals",
            storage = "Public aggregate",
            retention = "No personal record",
            sharing = "Read only",
        ),
        DataPractice(
            signal = "What-if plans",
            storage = "In memory",
            retention = "Session only",
            sharing = "Not shared",
        ),
    )
}
