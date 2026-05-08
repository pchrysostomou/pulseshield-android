# PulseShield

PulseShield is a privacy-first Android prototype for everyday COVID and respiratory illness decisions. It helps a person decide whether to go ahead, adjust, limit, or stay home before entering a crowded place, commuting, visiting family, or responding to new symptoms.

The app is built with Kotlin, Jetpack Compose, and a local-only risk engine. It is not a contact-tracing app, does not use Bluetooth exposure notifications, and does not require an account.
 
## What It Does

- Gives a clear decision: `Go`, `Adjust`, `Limit`, or `Stay home`.
- Scores local risk from symptoms, exposure level, area pressure, protection choices, and plan type.
- Supports illness focus modes: respiratory, COVID-like, flu/cold, and vulnerable visit.
- Explains why the score changed with transparent risk factors.
- Suggests safer alternatives before a plan is cancelled.
- Keeps place check-ins local by default.
- Shows whether a private place timeline is calm or needs attention.
- Provides trusted-guidance prompts without collecting identity data.
- Shows a privacy dashboard and data map for sensitive signals.

## Why This Exists

Most health apps either feel too clinical or too surveillance-heavy. PulseShield explores a different shape: a calm decision-support tool that makes private health signals useful without turning them into identity tracking.

The product promise is simple:

> Choose the safest version of the plan you already had.

## Originality And Scope

PulseShield is an original prototype. It is not a clone of any government COVID app, contact-tracing app, or open-source public-health repository.

This project does not include:

- Bluetooth exposure notification APIs.
- Contact-tracing encounter exchange.
- Government or NHS branding.
- Positive test upload flows.
- Official isolation-rule logic.
- Copied visual assets, package names, text, or source files from public COVID app repositories.

See [docs/ORIGINALITY_REVIEW.md](docs/ORIGINALITY_REVIEW.md) for the practical copy-risk review.

## Current Prototype

The Android app currently includes:

- Jetpack Compose UI with Today, Places, and Trust tabs.
- Pure Kotlin risk engine with unit tests.
- Decision layer above the score.
- COVID-like, flu/cold, respiratory, and vulnerable-visit focus modes.
- Local place check-in memory.
- Private alert readiness for recent place check-ins.
- Health coach insights with confidence scoring.
- What-if simulator for safer choices.
- Plan advisor for errands, commute, family visits, and indoor events.
- Backup and device-transfer exclusions for future sensitive local state.

## Architecture

- `PulseShieldApp.kt`: Jetpack Compose UI.
- `HealthRiskEngine.kt`: local scoring, focus lenses, decisions, and explanations.
- `CheckInAnalytics.kt`: local place-memory summary and private alert readiness.
- `DemoData.kt`: prototype data for the current UI.
- `app/src/test`: JVM unit tests for the core behavior.

The valuable product logic is intentionally plain Kotlin so it can later move into a shared Kotlin Multiplatform module.

## Privacy Model

PulseShield is designed around local-first health data:

- Symptoms stay on device.
- Place check-ins stay on device.
- What-if plans are session-only prototype state.
- No account is required.
- No Android permissions are requested in the current prototype.
- No network calls are made in the current prototype.

## Build

Open the project in Android Studio with JDK 17 or newer, then sync Gradle.

From the project root:

```bash
./gradlew test
./gradlew assembleDebug
```

If your terminal uses Java 11 by default, set Android Studio's Gradle JDK to JDK 17 or newer, or run Gradle with `JAVA_HOME` pointing to JDK 17+.

## Tech Stack

- Kotlin 2.2.10.
- Android Gradle Plugin 9.2.1.
- Jetpack Compose BOM 2024.12.01.
- AndroidX Activity Compose 1.9.3.
- JUnit 4.13.2.

## Roadmap

- Replace demo data with encrypted local storage.
- Add real QR scanning for private place memory.
- Add configurable regional guidance links.
- Add accessibility and localization review.
- Add Kotlin Multiplatform shared domain module.
- Add a formal threat model before any network feature.

## Important Note

PulseShield is not a medical device and does not diagnose illness. It is a decision-support prototype that can route people toward trusted official guidance and clinical care when needed.
