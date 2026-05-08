# Originality Review

This is a practical engineering review, not legal advice. For a commercial launch, get a lawyer to review trademarks, licences, medical claims, and jurisdiction-specific health regulations.

## Reviewed Risk

This review checks whether PulseShield contains signs of copied code, branding, wording, package names, assets, or implementation patterns from the England/Wales COVID app source code that is publicly available on GitHub.

## Public Reference Checked

Public sources describe the England/Wales COVID app as a contact-tracing app that used Bluetooth Low Energy, random IDs, exposure notifications, positive-test reporting, and location-dependent guidance. The public Android package name was reported as `uk.nhs.covid19.production`.

## Local Project Findings

PulseShield currently uses:

- Package/application id: `com.pulseshield.app`.
- Kotlin + Jetpack Compose prototype code.
- A local decision-support risk engine.
- Private place memory and local check-in summaries.
- No Bluetooth permissions.
- No location permissions.
- No camera permission.
- No internet permission.
- No exposure-notification API.
- No positive-test upload workflow.
- No government branding.
- No NHS/GOV.UK strings, package names, assets, or source files.

## Local Source Search

The repository was searched for obvious copy-risk indicators, including:

- `NHS`
- `UK COVID`
- `covid-19-app`
- `nhsx`
- `uk.nhs`
- `ExposureNotification`
- `TemporaryExposureKey`
- `DiagnosisKey`
- `contact tracing`
- `Bluetooth`
- `self-isolate`
- `England`
- `Wales`
- `Crown copyright`
- `Open Government Licence`

No implementation copy indicators were found in source files.

## Practical Conclusion

Based on the current codebase, PulseShield does not appear to copy the England/Wales COVID app implementation. It has a different package name, different architecture, different UI copy, different feature model, and no contact-tracing or exposure-notification implementation.

The remaining legal-sensitive areas for a future public release are:

- Avoid using government, NHS, or public-health-agency branding.
- Avoid claiming clinical accuracy without evidence and governance.
- Add a clear licence file before publishing.
- Add a medical disclaimer anywhere the app is distributed.
- Review regional health-data rules before adding storage, sync, or sharing.
