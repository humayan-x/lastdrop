# LastDrop

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-green.svg)](https://developer.android.com)
[![Privacy](https://img.shields.io/badge/Network-Zero--Network%20(No%20Internet)-brightgreen.svg)](README.md)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)

> **Privacy-First, Local-Only, Zero-Network Android Notification Ledger**

**LastDrop** is a private, lightweight notification history and audit app built entirely with Kotlin and Jetpack Compose. It runs as a local background listener service, capturing status bar notifications in real time and archiving them in an indexed on-device SQLite database with Full-Text Search (FTS).

---

## Highlights

* **Guaranteed Zero-Network:** LastDrop strictly omits `android.permission.INTERNET` from its manifest. No tracking, zero analytics, no telemetry, and zero network clients—your notifications never leave your device.
* **Transient Alert Preservation:** Captures alerts that vanish—such as deleted messages (WhatsApp, Signal, Telegram), auto-dismissed status alerts, and swiped notifications.
* **Automatic Privacy & 2FA Filter:** Sensitive authentication alerts (OTPs, login codes, verification PINs) are detected on Dispatchers.IO and filtered out before database insertion.
* **Instant Full-Text Search:** Powered by SQLite FTS, allowing instant sub-millisecond filtering across notification titles, body text, subtext, and package identifiers.
* **Starred Vault:** Star critical notifications to pin and exempt them from periodic retention purges.
* **Daily Activity & Noise Dashboard:** Visualizes notification frequency by hour and aggregates volume rankings per app to help identify notification spam.
* **Bespoke Theme System:** Native Material 3 UI supporting Obsidian Slate (Dark) and Crisp Alabaster (Light) modes with locally bundled Inter and JetBrains Mono typography.

---

## Permissions & Resilience

LastDrop requests only the minimal system permissions required to intercept and preserve alerts locally:

| Permission | Purpose |
|---|---|
| `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE` | Required by Android's `NotificationListenerService` to receive incoming status bar alerts. |
| `android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Requests exemption from standard Doze/battery limits so background capture remains active. |
| `android.permission.POST_NOTIFICATIONS` | Allows status alerts on Android 13+ if needed. |
| `android.permission.INTERNET` | **NEVER REQUESTED.** Guaranteed 100% offline. |

---

## Architecture

```
┌─────────────────────────────────┐
│     Android Status Bar Shade    │
└────────────────┬────────────────┘
                 │ (StatusBarNotification Event)
                 ▼
┌─────────────────────────────────┐
│    LastDropListenerService      │ ◄── Discards ongoing noise & 2FA / OTP codes
└────────────────┬────────────────┘
                 │ (Parsed NotificationRecord on Dispatchers.IO)
                 ▼
┌─────────────────────────────────┐
│    Room Database + SQLite FTS   │ ◄── Stored 100% locally in app-private storage
└────────────────┬────────────────┘
                 │ (Kotlin StateFlow streams)
                 ▼
┌─────────────────────────────────┐
│       Jetpack Compose UI        │ ◄── Timeline, Starred Vault, Analytics, Settings
└─────────────────────────────────┘
```

---

## Project Structure

```
io.github.humayanx.lastdrop/
├── LastDropApp.kt                         # Application container & dependency provider
├── MainActivity.kt                        # Edge-to-edge Activity with theme observer
├── navigation/
│   └── NavRoutes.kt                       # Screen and navigation routing
├── service/
│   ├── LastDropListenerService.kt         # Background NotificationListenerService
│   ├── NotificationParser.kt              # Null-safe parser and noise filter
│   ├── AuthenticationCodeFilter.kt        # Regex-based OTP & 2FA privacy filter
│   └── PermissionManager.kt               # System intent & permission helpers
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt                 # Room database singleton with FTS support
│   │   ├── NotificationDao.kt             # Reactive CRUD & FTS search queries
│   │   ├── entity/NotificationRecord.kt   # Core notification entity
│   │   ├── entity/NotificationFts.kt      # SQLite FTS virtual table
│   │   └── model/                         # Aggregation models (Hourly & App volume)
│   └── repository/
│       ├── NotificationRepository.kt      # Repository abstraction
│       ├── NotificationRepositoryImpl.kt  # Isolated Room DAO implementation
│       └── UserPreferencesRepository.kt   # DataStore theme and retention settings
├── worker/
│   └── RetentionWorker.kt                 # Periodic background cleanup worker
└── ui/
    ├── theme/                             # Obsidian Slate / Alabaster color palettes & typography
    ├── components/                        # Header, FloatingNavBar, NotificationCard, DetailSheet...
    └── screens/
        ├── setup/                         # First-run permission setup gate
        ├── main/MainScaffold.kt           # Navigation container with floating navigation
        ├── timeline/                      # Chronological feed with search and app filters
        ├── starred/                       # Starred vault for pinned records
        ├── analytics/                     # Hourly distribution chart and volume stats
        └── settings/                      # Durability status, retention rules, and theme selector
```

---

## Building from Source

### Prerequisites
* Android Studio Ladybug (2024.2+) or IntelliJ IDEA
* JDK 17
* Android SDK 35 (compileSdk 35, minSdk 26)

### Clone & Compile

```bash
# Clone the repository
git clone https://github.com/humayan-x/lastdrop.git
cd lastdrop

# Run unit tests
./gradlew testDebugUnitTest

# Build debug APK
./gradlew assembleDebug

# Build optimized release APK
./gradlew assembleRelease
```

Generated APKs will be located at:
* Debug: `app/build/outputs/apk/debug/app-debug.apk`
* Release: `app/build/outputs/apk/release/app-release.apk`

---

## License

This project is licensed under the [MIT License](LICENSE).
