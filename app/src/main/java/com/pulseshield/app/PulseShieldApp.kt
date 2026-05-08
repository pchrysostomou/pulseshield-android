package com.pulseshield.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val PulseColors = lightColorScheme(
    primary = Color(0xFF05668D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6F1FF),
    onPrimaryContainer = Color(0xFF012D42),
    secondary = Color(0xFF00A896),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7FFF8),
    tertiary = Color(0xFFF26419),
    background = Color(0xFFF7FAF8),
    surface = Color.White,
    surfaceVariant = Color(0xFFE7EFEA),
    outline = Color(0xFFB7C7C0),
)

@Composable
fun PulseShieldApp() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedSymptoms by remember { mutableStateOf(setOf(Symptom.Cough, Symptom.Fatigue)) }
    var exposureLevel by remember { mutableStateOf(ExposureLevel.Nearby) }
    var vulnerableProfile by remember { mutableStateOf(false) }
    var recentNegativeTest by remember { mutableStateOf(false) }
    var selectedPlan by remember { mutableStateOf(PlanType.Essentials) }
    var protectionLevel by remember { mutableStateOf(ProtectionLevel.Standard) }
    var healthFocus by remember { mutableStateOf(HealthFocus.CovidLike) }
    var localAlertLevel by remember { mutableIntStateOf(2) }
    var checkIns by remember { mutableStateOf(DemoData.checkIns) }

    val engine = remember { HealthRiskEngine() }
    val checkInAnalyzer = remember { CheckInAnalyzer() }
    val riskInput = RiskInput(
        symptoms = selectedSymptoms,
        exposureLevel = exposureLevel,
        localAlertLevel = localAlertLevel,
        vulnerableProfile = vulnerableProfile,
        recentNegativeTest = recentNegativeTest,
        planType = selectedPlan,
        protectionLevel = protectionLevel,
        healthFocus = healthFocus,
    )
    val risk = engine.assess(riskInput)
    val scenarios = engine.compareSaferChoices(riskInput)
    val checkInSummary = checkInAnalyzer.summarize(checkIns)

    PulseShieldTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                PulseNavigation(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                AppHeader()
                when (selectedTab) {
                    0 -> DashboardScreen(
                        risk = risk,
                        scenarios = scenarios,
                        selectedSymptoms = selectedSymptoms,
                        exposureLevel = exposureLevel,
                        selectedPlan = selectedPlan,
                        protectionLevel = protectionLevel,
                        healthFocus = healthFocus,
                        localAlertLevel = localAlertLevel,
                        vulnerableProfile = vulnerableProfile,
                        recentNegativeTest = recentNegativeTest,
                        onSymptomToggle = { symptom ->
                            selectedSymptoms = if (selectedSymptoms.contains(symptom)) {
                                selectedSymptoms - symptom
                            } else {
                                selectedSymptoms + symptom
                            }
                        },
                        onExposureChange = { exposureLevel = it },
                        onPlanChange = { selectedPlan = it },
                        onProtectionChange = { protectionLevel = it },
                        onHealthFocusChange = { healthFocus = it },
                        onLocalAlertChange = { localAlertLevel = ((localAlertLevel + 1) % 5) },
                        onVulnerableChange = { vulnerableProfile = !vulnerableProfile },
                        onTestChange = { recentNegativeTest = !recentNegativeTest },
                    )

                    1 -> CheckInScreen(
                        checkIns = checkIns,
                        summary = checkInSummary,
                        onScanPlace = {
                            checkIns = listOf(
                                PlaceCheckIn(
                                    name = "Pop-up Clinic",
                                    time = "Now",
                                    signal = "Verified health partner",
                                    riskLevel = RiskLevel.Low,
                                    dwellTime = "0 min",
                                    recommendation = "Safe check-in created locally",
                                ),
                            ) + checkIns
                        },
                    )

                    else -> TrustScreen()
                }
            }
        }
    }
}

@Composable
private fun PulseShieldTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PulseColors,
        typography = Typography(),
        content = content,
    )
}

@Composable
private fun AppHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "PulseShield",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "Private health radar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = CircleShape,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.padding(12.dp),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun DashboardScreen(
    risk: RiskResult,
    scenarios: List<RiskScenario>,
    selectedSymptoms: Set<Symptom>,
    exposureLevel: ExposureLevel,
    selectedPlan: PlanType,
    protectionLevel: ProtectionLevel,
    healthFocus: HealthFocus,
    localAlertLevel: Int,
    vulnerableProfile: Boolean,
    recentNegativeTest: Boolean,
    onSymptomToggle: (Symptom) -> Unit,
    onExposureChange: (ExposureLevel) -> Unit,
    onPlanChange: (PlanType) -> Unit,
    onProtectionChange: (ProtectionLevel) -> Unit,
    onHealthFocusChange: (HealthFocus) -> Unit,
    onLocalAlertChange: () -> Unit,
    onVulnerableChange: () -> Unit,
    onTestChange: () -> Unit,
) {
    RiskHero(risk = risk)
    DecisionCard(decision = risk.decisionAdvice)
    FocusLensCard(focus = healthFocus, advice = risk.focusAdvice)
    CoachBriefCard(risk = risk)
    ReadinessCard(risk = risk)
    PlanAdvisorCard(
        advice = risk.planAdvice,
        selectedPlan = selectedPlan,
        onPlanChange = onPlanChange,
    )
    WhatIfCard(scenarios = scenarios)
    TrendCard(points = DemoData.weeklyTrend, currentScore = risk.score)
    RiskFactorPanel(factors = risk.factors)
    MetricRow(risk = risk, localAlertLevel = localAlertLevel, exposureLevel = exposureLevel)
    ActionList(actions = risk.nextActions)
    DailyPlanCard(tasks = DemoData.safetyTasks)
    GuidancePanel(resources = DemoData.guidanceResources, riskLevel = risk.level)
    CareResourcesPanel(resources = DemoData.careResources, riskLevel = risk.level)
    AssessmentPanel(
        selectedSymptoms = selectedSymptoms,
        exposureLevel = exposureLevel,
        protectionLevel = protectionLevel,
        healthFocus = healthFocus,
        vulnerableProfile = vulnerableProfile,
        recentNegativeTest = recentNegativeTest,
        localAlertLevel = localAlertLevel,
        onSymptomToggle = onSymptomToggle,
        onExposureChange = onExposureChange,
        onProtectionChange = onProtectionChange,
        onHealthFocusChange = onHealthFocusChange,
        onLocalAlertChange = onLocalAlertChange,
        onVulnerableChange = onVulnerableChange,
        onTestChange = onTestChange,
    )
}

@Composable
private fun DecisionCard(decision: DecisionAdvice) {
    val color = statusColor(decision.status.label)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Decision",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = decision.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                MiniPill(text = decision.status.label, color = color)
            }
            Text(
                text = decision.detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = color.copy(alpha = 0.11f),
                shape = RoundedCornerShape(8.dp),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = decision.primaryAction,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = decision.privacyNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FocusLensCard(focus: HealthFocus, advice: FocusAdvice) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = priorityColor(advice.priority).copy(alpha = 0.14f),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = advice.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = focus.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                MiniPill(text = focus.label, color = priorityColor(advice.priority))
            }
            Text(
                text = advice.detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = priorityColor(advice.priority),
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = advice.action,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun RiskHero(risk: RiskResult) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = levelColor(risk.level).copy(alpha = 0.13f),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = risk.headline,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = "${risk.level.label} risk",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ScoreBadge(score = risk.score, color = levelColor(risk.level))
            }
            Text(
                text = risk.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LinearProgressIndicator(
                progress = { risk.score / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = levelColor(risk.level),
                trackColor = Color.White.copy(alpha = 0.72f),
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MiniPill(text = "Private", color = MaterialTheme.colorScheme.secondary)
                MiniPill(text = "Updated", color = levelColor(risk.level))
                MiniPill(text = "No login", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun MiniPill(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.widthIn(min = 72.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ScoreBadge(score: Int, color: Color) {
    Surface(
        color = color,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.White,
            )
            Text(
                text = "score",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.88f),
            )
        }
    }
}

@Composable
private fun CoachBriefCard(risk: RiskResult) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Health coach",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.72f),
                    )
                    Text(
                        text = risk.coachTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                    )
                    Text(
                        text = risk.coachSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.82f),
                    )
                }
                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "${risk.confidenceScore}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                        )
                        Text(
                            text = "confidence",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.72f),
                        )
                    }
                }
            }
            risk.insights.forEach { insight ->
                CoachInsightRow(insight = insight)
            }
        }
    }
}

@Composable
private fun CoachInsightRow(insight: CoachInsight) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        val color = priorityColor(insight.priority)
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
                .padding(top = 6.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = insight.title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = insight.detail,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.72f),
            )
        }
        Text(
            text = insight.priority.label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ReadinessCard(risk: RiskResult) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(88.dp),
                shape = CircleShape,
                color = levelColor(risk.level).copy(alpha = 0.13f),
                border = androidx.compose.foundation.BorderStroke(2.dp, levelColor(risk.level)),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = risk.readinessScore.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = levelColor(risk.level),
                    )
                    Text(
                        text = "ready",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Plan readiness",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = risk.primaryRecommendation,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "Check again: ${risk.checkAgainIn}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PlanAdvisorCard(
    advice: PlanAdvice,
    selectedPlan: PlanType,
    onPlanChange: (PlanType) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Before you go",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Pick a plan and PulseShield adjusts the advice.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                MiniPill(text = advice.status, color = statusColor(advice.status))
            }
            ChipGroup {
                PlanType.entries.forEach { plan ->
                    FilterChip(
                        selected = selectedPlan == plan,
                        onClick = { onPlanChange(plan) },
                        label = { Text(plan.label) },
                        leadingIcon = if (selectedPlan == plan) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        } else {
                            null
                        },
                    )
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = statusColor(advice.status).copy(alpha = 0.11f),
                shape = RoundedCornerShape(8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = advice.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = advice.detail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.72f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = statusColor(advice.status),
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = advice.saferSwap,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WhatIfCard(scenarios: List<RiskScenario>) {
    if (scenarios.isEmpty()) {
        return
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "What would help most?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Compare safer choices before changing your plan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            scenarios.forEachIndexed { index, scenario ->
                if (index > 0) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
                ScenarioRow(scenario = scenario)
            }
        }
    }
}

@Composable
private fun ScenarioRow(scenario: RiskScenario) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            color = levelColor(scenario.projectedLevel).copy(alpha = 0.13f),
            shape = RoundedCornerShape(8.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = scenario.projectedScore.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = levelColor(scenario.projectedLevel),
                )
                Text(
                    text = scenario.projectedLevel.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = scenario.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = scenario.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = scenario.scoreDelta.toString(),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun TrendCard(points: List<TrendPoint>, currentScore: Int) {
    val plottedPoints = if (points.isEmpty()) {
        listOf(TrendPoint("Now", currentScore))
    } else {
        points.dropLast(1) + points.last().copy(score = currentScore)
    }
    val latest = plottedPoints.last().score
    val previous = plottedPoints.getOrNull(plottedPoints.lastIndex - 1)?.score ?: latest
    val direction = when {
        latest > previous + 2 -> "Rising"
        latest < previous - 2 -> "Falling"
        else -> "Stable"
    }
    val average = plottedPoints.map { it.score }.average().toInt()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "7-day signal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "$direction trend, $average average score",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                MiniPill(text = direction, color = levelColor(levelForScore(latest)))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                plottedPoints.forEach { point ->
                    TrendBar(point = point, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TrendBar(point: TrendPoint, modifier: Modifier = Modifier) {
    val barHeight = (12f + point.score.coerceIn(0, 100) * 0.82f).dp
    Column(
        modifier = modifier.height(122.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        Box(
            modifier = Modifier
                .width(18.dp)
                .height(barHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(levelColor(levelForScore(point.score))),
        )
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = point.day,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun RiskFactorPanel(factors: List<RiskFactor>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Why this score",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Transparent",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            factors.forEachIndexed { index, factor ->
                if (index > 0) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
                RiskFactorRow(factor = factor)
            }
        }
    }
}

@Composable
private fun RiskFactorRow(factor: RiskFactor) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val factorColor = if (factor.points <= 0) {
            MaterialTheme.colorScheme.secondary
        } else {
            MaterialTheme.colorScheme.tertiary
        }
        Surface(
            color = factorColor.copy(alpha = 0.13f),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(
                text = if (factor.points > 0) "+${factor.points}" else factor.points.toString(),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = factorColor,
                fontWeight = FontWeight.Black,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = factor.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = factor.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MetricRow(risk: RiskResult, localAlertLevel: Int, exposureLevel: ExposureLevel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Favorite,
            label = "Health",
            value = risk.level.label,
            tint = levelColor(risk.level),
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.LocationOn,
            label = "Area",
            value = "L$localAlertLevel",
            tint = MaterialTheme.colorScheme.primary,
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Notifications,
            label = "Signal",
            value = exposureLevel.label,
            tint = MaterialTheme.colorScheme.tertiary,
        )
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint)
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ActionList(actions: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Next actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        actions.forEach { action ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = action,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DailyPlanCard(tasks: List<SafetyTask>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Daily plan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${tasks.count { it.complete }} of ${tasks.size} done",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                LinearProgressIndicator(
                    progress = { tasks.count { it.complete } / tasks.size.toFloat() },
                    modifier = Modifier
                        .width(84.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
            tasks.forEach { task ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = if (task.complete) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                        contentDescription = null,
                        tint = if (task.complete) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        },
                        modifier = Modifier.size(20.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = task.detail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CareResourcesPanel(resources: List<CareResource>, riskLevel: RiskLevel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Care options",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            MiniPill(
                text = if (riskLevel == RiskLevel.High) "Prioritize now" else "Available",
                color = if (riskLevel == RiskLevel.High) levelColor(RiskLevel.High) else MaterialTheme.colorScheme.primary,
            )
        }
        resources.forEach { resource ->
            CareResourceCard(resource = resource)
        }
    }
}

@Composable
private fun GuidancePanel(resources: List<GuidanceResource>, riskLevel: RiskLevel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Trusted guidance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            MiniPill(
                text = if (riskLevel == RiskLevel.High) "Use first" else "Ready",
                color = if (riskLevel == RiskLevel.High) levelColor(RiskLevel.High) else MaterialTheme.colorScheme.primary,
            )
        }
        resources.forEach { resource ->
            GuidanceResourceCard(resource = resource)
        }
    }
}

@Composable
private fun GuidanceResourceCard(resource: GuidanceResource) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (resource.urgent) {
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f)
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (resource.urgent) Icons.Filled.Warning else Icons.Filled.Info,
                    contentDescription = null,
                    tint = if (resource.urgent) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resource.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = resource.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = resource.action,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (resource.urgent) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = resource.region,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CareResourceCard(resource: CareResource) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (resource.openNow) {
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (resource.openNow) Icons.Filled.CheckCircle else Icons.Filled.Info,
                    contentDescription = null,
                    tint = if (resource.openNow) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resource.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = resource.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = resource.waitTime,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = resource.distance,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AssessmentPanel(
    selectedSymptoms: Set<Symptom>,
    exposureLevel: ExposureLevel,
    protectionLevel: ProtectionLevel,
    healthFocus: HealthFocus,
    vulnerableProfile: Boolean,
    recentNegativeTest: Boolean,
    localAlertLevel: Int,
    onSymptomToggle: (Symptom) -> Unit,
    onExposureChange: (ExposureLevel) -> Unit,
    onProtectionChange: (ProtectionLevel) -> Unit,
    onHealthFocusChange: (HealthFocus) -> Unit,
    onLocalAlertChange: () -> Unit,
    onVulnerableChange: () -> Unit,
    onTestChange: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Daily check",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Focus",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ChipGroup {
                HealthFocus.entries.forEach { focus ->
                    FilterChip(
                        selected = healthFocus == focus,
                        onClick = { onHealthFocusChange(focus) },
                        label = { Text(focus.label) },
                        leadingIcon = if (healthFocus == focus) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        } else {
                            null
                        },
                    )
                }
            }
            Text(
                text = healthFocus.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ChipGroup {
                Symptom.entries.forEach { symptom ->
                    FilterChip(
                        selected = selectedSymptoms.contains(symptom),
                        onClick = { onSymptomToggle(symptom) },
                        label = { Text(symptom.label) },
                        leadingIcon = if (selectedSymptoms.contains(symptom)) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        } else {
                            null
                        },
                    )
                }
            }
            Text(
                text = "Exposure",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ChipGroup {
                ExposureLevel.entries.forEach { exposure ->
                    FilterChip(
                        selected = exposureLevel == exposure,
                        onClick = { onExposureChange(exposure) },
                        label = { Text(exposure.label) },
                    )
                }
            }
            Text(
                text = "Protection",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ChipGroup {
                ProtectionLevel.entries.forEach { protection ->
                    FilterChip(
                        selected = protectionLevel == protection,
                        onClick = { onProtectionChange(protection) },
                        label = { Text(protection.label) },
                        leadingIcon = if (protectionLevel == protection) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        } else {
                            null
                        },
                    )
                }
            }
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AssistChip(
                    onClick = onLocalAlertChange,
                    label = { Text("Area alert L$localAlertLevel") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
                FilterChip(
                    selected = vulnerableProfile,
                    onClick = onVulnerableChange,
                    label = { Text("At-risk profile") },
                )
            }
            FilterChip(
                selected = recentNegativeTest,
                onClick = onTestChange,
                label = { Text("Recent negative test") },
                leadingIcon = if (recentNegativeTest) {
                    {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                } else {
                    null
                },
            )
        }
    }
}

@Composable
private fun ChipGroup(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = { content() },
    )
}

@Composable
private fun CheckInScreen(
    checkIns: List<PlaceCheckIn>,
    summary: CheckInSummary,
    onScanPlace: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        PlaceScanHero()
        CheckInSummaryCard(summary = summary)
        BestWindowPanel(windows = DemoData.timeWindows)
        Button(
            onClick = onScanPlace,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Scan place code")
        }
        SuggestedVenuesPanel(venues = DemoData.venueSuggestions)
        Text(
            text = "Recent places",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        checkIns.forEach { checkIn ->
            CheckInCard(checkIn = checkIn)
        }
    }
}

@Composable
private fun CheckInSummaryCard(summary: CheckInSummary) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Local check-in memory",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = summary.headline,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                MiniPill(
                    text = summary.highestRiskLevel.label,
                    color = levelColor(summary.highestRiskLevel),
                )
            }
            Text(
                text = summary.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SummaryMetric(
                    modifier = Modifier.weight(1f),
                    label = "Places",
                    value = summary.totalCheckIns.toString(),
                )
                SummaryMetric(
                    modifier = Modifier.weight(1f),
                    label = "Minutes",
                    value = summary.minutesLogged.toString(),
                )
                SummaryMetric(
                    modifier = Modifier.weight(1f),
                    label = "Watch",
                    value = summary.attentionCount.toString(),
                )
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = if (summary.canShareAnonymousAlert) {
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f)
                },
                shape = RoundedCornerShape(8.dp),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = if (summary.canShareAnonymousAlert) Icons.Filled.Notifications else Icons.Filled.Lock,
                        contentDescription = null,
                        tint = if (summary.canShareAnonymousAlert) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(20.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = summary.alertReadiness,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = summary.anonymousAlertDetail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = summary.privateWindow,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(19.dp),
                )
                Text(
                    text = summary.recommendation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(modifier: Modifier = Modifier, label: String, value: String) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun BestWindowPanel(windows: List<TimeWindow>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Best time windows",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            windows.forEach { window ->
                TimeWindowCard(window = window)
            }
        }
    }
}

@Composable
private fun TimeWindowCard(window: TimeWindow) {
    Surface(
        modifier = Modifier.width(156.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = window.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(levelColor(window.riskLevel)),
                )
            }
            Text(
                text = window.crowdLevel,
                style = MaterialTheme.typography.labelLarge,
                color = levelColor(window.riskLevel),
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = window.recommendation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SuggestedVenuesPanel(venues: List<VenueSuggestion>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Safer nearby options",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        venues.forEach { venue ->
            VenueSuggestionCard(venue = venue)
        }
    }
}

@Composable
private fun VenueSuggestionCard(venue: VenueSuggestion) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(levelColor(venue.riskLevel).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = levelColor(venue.riskLevel),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = venue.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${venue.category} - ${venue.ventilation} ventilation",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = venue.bestTime,
                    style = MaterialTheme.typography.labelLarge,
                    color = levelColor(venue.riskLevel),
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = venue.distance,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PlaceScanHero() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QrPreview()
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Place check-in",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "Scan venue codes without sharing your identity. Alerts can stay anonymous.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun QrPreview() {
    val cells = listOf(
        "11101",
        "10010",
        "11111",
        "01001",
        "10111",
    )
    Column(
        modifier = Modifier
            .size(88.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        cells.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                row.forEach { cell ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (cell == '1') {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckInCard(checkIn: PlaceCheckIn) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(levelColor(checkIn.riskLevel).copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = levelColor(checkIn.riskLevel),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = checkIn.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = checkIn.signal,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = checkIn.recommendation,
                    style = MaterialTheme.typography.bodySmall,
                    color = levelColor(checkIn.riskLevel),
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = checkIn.time,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = checkIn.dwellTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TrustScreen() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TrustMetricGrid(metrics = DemoData.trustMetrics)
        PrivacyPanel()
        DataPracticePanel(practices = DemoData.dataPractices)
        DemoData.signals.forEach { signal ->
            SignalCard(signal = signal)
        }
    }
}

@Composable
private fun TrustMetricGrid(metrics: List<TrustMetric>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Trust dashboard",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        metrics.chunked(2).forEach { rowMetrics ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                rowMetrics.forEach { metric ->
                    TrustMetricCard(
                        modifier = Modifier.weight(1f),
                        metric = metric,
                    )
                }
                if (rowMetrics.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TrustMetricCard(modifier: Modifier = Modifier, metric: TrustMetric) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = metric.label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = metric.value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = metric.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PrivacyPanel() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Local-first privacy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = "The best health app is the one people can trust before they need it.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            listOf(
                "Check-ins stay on the device by default",
                "Risk math is readable and testable",
                "Anonymous venue alerts do not need identity data",
            ).forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = item, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun DataPracticePanel(practices: List<DataPractice>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Data map",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        practices.forEach { practice ->
            DataPracticeRow(practice = practice)
        }
    }
}

@Composable
private fun DataPracticeRow(practice: DataPractice) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = practice.signal,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                MiniPill(text = practice.sharing, color = MaterialTheme.colorScheme.secondary)
            }
            Text(
                text = "${practice.storage} - ${practice.retention}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SignalCard(signal: CommunitySignal) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = signal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${signal.confidence}%",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = signal.detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LinearProgressIndicator(
                progress = { signal.confidence / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Composable
private fun PulseNavigation(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf(
        TabItem("Today", Icons.Filled.Favorite),
        TabItem("Places", Icons.Filled.LocationOn),
        TabItem("Trust", Icons.Filled.Lock),
    )
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        tabs.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                label = { Text(tab.label) },
            )
        }
    }
}

private data class TabItem(val label: String, val icon: ImageVector)

private fun levelColor(level: RiskLevel): Color =
    when (level) {
        RiskLevel.Low -> Color(0xFF00A896)
        RiskLevel.Guarded -> Color(0xFFF2C94C)
        RiskLevel.Elevated -> Color(0xFFF26419)
        RiskLevel.High -> Color(0xFFC1121F)
    }

private fun priorityColor(priority: InsightPriority): Color =
    when (priority) {
        InsightPriority.Calm -> Color(0xFF7AE7C7)
        InsightPriority.Watch -> Color(0xFFF2C94C)
        InsightPriority.Act -> Color(0xFFFF8A65)
    }

private fun statusColor(status: String): Color =
    when (status) {
        "Go" -> Color(0xFF00A896)
        "Care", "Adjust" -> Color(0xFF05668D)
        "Limit", "Delay" -> Color(0xFFF26419)
        "Avoid", "Hold", "Stay home" -> Color(0xFFC1121F)
        else -> Color(0xFF05668D)
    }

private fun levelForScore(score: Int): RiskLevel =
    when {
        score >= 72 -> RiskLevel.High
        score >= 48 -> RiskLevel.Elevated
        score >= 24 -> RiskLevel.Guarded
        else -> RiskLevel.Low
    }

@Preview(showBackground = true)
@Composable
private fun PulseShieldPreview() {
    PulseShieldApp()
}
