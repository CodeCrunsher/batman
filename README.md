# Batman Dashboard 🦇

> *"I am vengeance. I am the night. I am Batman."*

A fully-featured personal command dashboard for Batman, built natively for Android using **Kotlin + Jetpack Compose**.

## Platform

**Android 9+ (API 28+)** — tested on Android 9 through Android 14.

## Features

### Core Requirements
| Feature | Description |
|---------|-------------|
| ✅ **Missions & Tasks** | Full CRUD mission management with priorities (CRITICAL/HIGH/MEDIUM/LOW), status tracking, categories (Recon/Combat/Infiltration/Investigation), due dates, and filters |
| 💬 **Secure Comms** | Chat interface with 6 allies (Alfred, Robin, Nightwing, Oracle, Commissioner Gordon, Lucius Fox) with AES-256 encryption indicator, simulated replies, and persistent message history |
| ⚙️ **Equipment Control** | Toggle control for 8 gadgets (Batsuit, Batmobile, Batwing, Batarang, Grapple Gun, Detective Goggles, EMP, Comm Unit) with battery levels and status indicators |
| 🗺️ **Gotham City Map** | Custom Canvas-drawn map of Gotham City with 11 accurate fictional districts, rivers, bridges, Wayne Tower, animated pulsing crime pins, and full crime report CRUD |

### Bonus Features
| Feature | Description |
|---------|-------------|
| 🎵 **Music Player** | "Gotham Nightwatch" playlist with 10 tracks, animated waveform visualizer, album art with bat logo, shuffle/repeat controls |
| 🏢 **Wayne Enterprises** | Live stock ticker animation, department directory, Lucius Fox tech pipeline, board meeting scheduler |
| 🚨 **Emergency SOS** | Big red SOS button with 5-second grace period countdown, cancellable, alert type selection (6 types), emergency contacts, persistent alert log |

## Architecture

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Navigation**: AndroidX Navigation3
- **Database**: Room (SQLite) — fully offline, no internet required at runtime
- **State**: ViewModel + StateFlow
- **DI**: Manual `AppContainer` (no Hilt) — each ViewModel receives its DAOs via `ViewModelProvider.Factory`, making all screens independently testable

## Build Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK Platform 28+

### Build Debug APK
```bash
./gradlew assembleDebug
```
APK output: `app/build/outputs/apk/debug/app-debug.apk`

### Build Release APK
```bash
./gradlew assembleRelease
```

### Install on Device
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## External Dependencies

All dependencies are standard AndroidX libraries:
- `androidx.compose:compose-bom:2026.03.01`
- `androidx.navigation3:navigation3-*:1.0.1`
- `androidx.room:room-*:2.7.1`
- `androidx.lifecycle:lifecycle-*:2.10.0`

Fonts bundled locally (no internet required):
- **Orbitron** (Google Fonts) — headers/titles
- **Roboto Mono** (Google Fonts) — body text/data

## Project Structure

```
app/src/main/java/com/batman/dashboard/
├── BatmanApp.kt              # Application class + AppContainer (manual DI)
├── MainActivity.kt           # Entry point
├── NavigationKeys.kt         # Serializable nav key objects  
├── Navigation.kt             # NavDisplay routing
├── data/db/                  # Room: Entities, DAOs, AppDatabase
└── ui/
    ├── theme/                # Color, Typography, Theme
    ├── components/           # Shared: GlassCard, PulseIndicator, BatteryBar, etc.
    ├── home/                 # Dashboard home + bat signal animation
    ├── missions/             # Mission CRUD with filters
    ├── comms/                # Ally chat interface
    ├── equipment/            # Gadget toggle controls
    ├── map/                  # Gotham City canvas map + crime pins
    ├── music/                # Music player UI
    ├── wayne/                # Wayne Enterprises corporate view
    └── emergency/            # SOS screen
```

## Gotham City Map

The Gotham map is drawn entirely using Jetpack Compose `Canvas`. It is based on the fictional DC Comics Gotham City geography, featuring:

- **11 Districts**: Old Gotham, Diamond District, The Narrows, Crime Alley & Bowery, Park Row, Amusement Mile, Arkham Island, Uptown Gotham, Financial District, and more
- **Water Bodies**: Gotham River (west), Gotham Bay (south), East Channel, North Inlet
- **Bridges**: Arkham Bridge, Lower Bridge, Mid Bridge
- **Landmarks**: Wayne Tower (marked with gold icon)
- **Crime Pins**: 5 types (Robbery, Assault, Terrorism, Drug Trafficking, Kidnapping) with animated pulse rings
- **Compass Rose** overlay

No map SDK or internet connection required.

## Notes

- All data is stored locally using Room — no backend or internet connection needed
- Music player is simulated (no real audio files) — focus is on the animated UI
- Emergency SOS sends local alerts (no actual calls/SMS — simulated in-app)
- Pre-seeded with realistic Gotham-themed data on first launch
