# Privacy Model

PulseShield should be designed as if every health signal is sensitive.

## Principles

- Local first: symptoms, check-ins, and exposure logs stay on device by default.
- Data minimization: collect only what directly changes user guidance.
- Explainability: risk rules must be readable and covered by tests.
- User control: sharing is explicit, scoped, and reversible.
- No identity requirement for basic protection.

## Current prototype

- No network calls.
- No account system.
- No Android permissions.
- Backup and device transfer are disabled for future sensitive state.
- Risk scoring lives in pure Kotlin for review and tests.
- The trust dashboard includes a data map for symptoms, place check-ins, area signals, and what-if plans.

## Production additions

- Encrypted local storage.
- Threat model document.
- Security review before any network feature.
- Open privacy impact assessment.
- Accessibility and localization review.
- Separate clinical governance review for any medical claims.
