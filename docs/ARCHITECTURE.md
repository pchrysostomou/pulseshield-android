# Architecture

PulseShield starts as a small Android app, but the code is shaped so the valuable parts can move into Kotlin Multiplatform later. The Android package now uses the product namespace `com.pulseshield.app`.

## Layers

- UI: Jetpack Compose screens in `PulseShieldApp.kt`.
- Domain: risk scoring, focus lenses, and score explanations in `HealthRiskEngine.kt`.
- Data: static prototype data in `DemoData.kt`.
- Tests: JVM unit tests for domain behavior in `HealthRiskEngineTest.kt`.

## Direction

The next production step is to split the package into:

- `core:model` for plain Kotlin data classes.
- `core:risk` for scoring and explanation logic.
- `feature:today` for the daily check screen.
- `feature:places` for QR scanning and local venue logs.
- `feature:trust` for privacy, governance, and public signals.

That keeps the Android UI replaceable while the Kotlin health logic becomes the product engine.
