# Batman Dashboard — App Writeup

## Overview

Batman Dashboard is a native Android application built for Batman to manage his crime-fighting operations in Gotham City in the year 2089. It replaces paper notes and inefficient communication with a sleek, secure, always-ready command center.

The app is built in **Kotlin with Jetpack Compose** — a modern, declarative UI toolkit. All data is stored locally using **Room (SQLite)**, making the app fully offline and secure.

---

## Design Philosophy

The UI is inspired by tactical command interfaces and the dark, gothic atmosphere of Gotham City:

- **Near-black background** (`#0A0A0F`) representing Gotham's perpetual night
- **Bat-gold accents** (`#F5C518`) for primary actions and highlights
- **Glassmorphism cards** — semi-transparent surfaces with gradient borders
- **Orbitron font** — a futuristic geometric typeface for headers
- **Roboto Mono** — monospaced tech-feel font for data and body text
- **Animated elements** throughout — pulsing indicators, rain effects, waveforms, bat signals

---

## Screen-by-Screen Breakdown

### 🏠 Home Dashboard
The entry point. Features:
- Animated **Bat Signal** icon that pulses
- **Gotham city threat level bar** — computed from active missions × crime incidents
- **Rain effect** rendered on canvas in the background
- Quick stats: active missions, active crime incidents
- Navigation grid to all 8 modules
- Current time and date display

### ✅ Missions & Tasks
A full mission management system:
- **Priority levels**: Critical (red), High (orange), Medium (yellow), Low (green) — color-coded strips
- **Status tracking**: Pending → In Progress → Completed
- **Categories**: Recon, Combat, Infiltration, Investigation
- Filter bar for status and priority
- Add/Edit dialog with full form
- Overflow menu for each mission (edit, change status, delete)
- Pre-seeded with 5 real Gotham missions (Joker hostage rescue, Scarecrow neutralization, etc.)

### 💬 Secure Comms
A chat app within the app:
- 6 **allies**: Alfred, Robin, Nightwing, Oracle, Commissioner Gordon, Lucius Fox
- Online/Offline status indicator with pulse animation
- Chat bubbles with Batman's messages on the right, ally replies on the left
- "ENCRYPTED" lock indicator on all incoming messages
- **Simulated auto-replies** — each ally has 4-5 unique response lines that auto-trigger 1.5 seconds after you send
- Persistent history via Room — messages survive app restarts

### ⚙️ Equipment Control
A gadget management panel:
- 8 **pieces of equipment**: Batsuit Mk VII, Batmobile, Batwing, Batarang Arsenal, Grapple Gun, Detective Mode Goggles, EMP Device, Encrypted Comm Unit
- **Toggle switch** for enable/disable (active/standby)
- **Battery/power bars** with color-coded levels (red < 20%, orange < 40%, green > 40%)
- **Charge button** appears for critically low items
- Status chips: ACTIVE (green), STANDBY (gold), CHARGING (cyan), OFFLINE (red)

### 🗺️ Gotham City Crime Map (Custom Canvas)
The crown jewel of the app. A fully hand-drawn map of fictional Gotham City:

**Map elements:**
- 11 named districts with distinct shading (The Narrows, Crime Alley, Diamond District, Amusement Mile, Arkham Island, Old Gotham, Park Row, Financial District, etc.)
- Gotham River (west side), Gotham Bay (south), East Channel, North Inlet
- 3 bridges (Arkham Bridge, Lower Bridge, Mid Bridge)
- Road grid overlay
- Wayne Tower landmark with gold icon
- Compass rose in the corner

**Crime features:**
- 5 crime types: Robbery (red), Assault (orange), Terrorism (cyan), Drug Trafficking (purple), Kidnapping (yellow)
- **Animated pulsing rings** around each pin
- Tap a pin → detail sheet with district, threat level, description
- Tap empty map area → add crime report dialog
- "RESOLVE" button to mark incidents as cleared
- Pre-seeded with 6 realistic Gotham crime reports
- Crime incident list below map with quick-select

### 🎵 Music Player
A stylish music player for Batman's in-flight entertainment:
- 10-track "Gotham Nightwatch" playlist with cinematic/jazz/orchestral genres
- Animated bat logo **album art**
- **Waveform visualizer** (animated bar chart) when playing
- Album art pulses gently when playing
- Progress slider with timestamps
- Full controls: shuffle, previous, play/pause, next, repeat
- Playlist list with track highlighting and equalizer icon on active track

### 🏢 Wayne Enterprises
Batman's corporate management module:
- **Live stock ticker** — Wayne Enterprises (WNE) stock price animates between values with a mini chart
- Revenue, employee count, division stats
- **Department directory**: R&D, Security, Legal, Finance, PR, Aerospace — each with head and project count
- **Lucius Fox Tech Pipeline**: 5 gadgets in development with progress bars and stages (Design → Prototyping → Manufacturing → Field Testing → Calibration)
- Board meeting reminder card

### 🚨 Emergency SOS
The most critical screen:
- 6 **alert types**: Medical Emergency, Police Backup, Fire Response, Tactical Support, Air Support, Evacuation
- Big **red SOS button** — tapping it begins a 5-second countdown
- Countdown is animated and shows the number prominently
- **Cancel button** available during countdown
- After countdown completes, alert is logged to Room database
- Green success notification appears
- Emergency contacts list (Alfred, Commissioner Gordon, Oracle, Lucius Fox)
- Recent alert log with status (ACTIVE/RESOLVED)

---

## Technical Architecture

### Data Layer
Room database with 6 tables:
- `missions` — task/mission management
- `messages` — ally chat history
- `equipment` — gadget status and battery
- `crime_pins` — Gotham map crime reports
- `emergency_contacts` — SOS contacts
- `emergency_log` — alert history

### ViewModel Pattern
Each screen has a dedicated ViewModel. All ViewModels:
- Receive DAOs through `ViewModelProvider.Factory` (passed from `AppContainer`)
- Expose state as `StateFlow<UiState>`
- Handle business logic (filtering, countdown timers, simulated replies)
- Are independently testable — no global state or singletons

### AppContainer (Manual DI)
`BatmanApp` (Application class) creates one `AppContainer` instance which exposes all DAOs. Navigation code passes specific DAOs to each ViewModel factory. This avoids Hilt/Dagger while maintaining clean architecture.

---

## Pre-loaded Data

On first launch, the database is seeded with:
- 5 missions (from Joker to Scarecrow)
- Alfred's initial messages
- 8 pieces of equipment (mostly online)
- 6 active crime pins across Gotham's districts
- 4 emergency contacts

---

*Built with Kotlin, Jetpack Compose, Room, Material 3, and a deep love for the Dark Knight.*
