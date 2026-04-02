# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A single-file web application that displays the current time as a natural Hebrew spoken phrase (e.g., "רבע אחרי שש בבוקר"). No build tools, no dependencies, no package manager — the entire app lives in `index.html`.

**Live demo:** shaik.github.io/heb-clock-web

## Development

Open `index.html` directly in a browser. There is no build step, no server required, and no npm/node dependencies.

## Versioning

Bump the version with **every change**, using a minor decimal scheme: `v1.9` → `v1.91` → ... → `v1.99` → `v2.0`. Always include the version bump in the same commit as the change.

- **Web:** `<span class="version">` in `index.html`
- **Android:** `versionName` in `android/hebClockWidget/build.gradle.kts`

## Architecture

### Web App (`index.html`, 1113 lines)

- **Lines 1–17**: HTML structure and Google Fonts imports
- **Lines 18–431**: Inline CSS (theming, layout, settings panel, animations)
- **Lines 529–1113**: Inline JavaScript (all app logic)

The single custom font is `fonts/fridge_Regular.ttf`.

### Hebrew Time Algorithm

The core of the app. `getHebrewTime(date)` computes:
1. Snaps current time to the nearest 5-minute anchor using a 300-second rounding window
2. Selects the appropriate Hebrew phrase from `PLAIN` / `NIQQUD` data objects
3. Adds a "קצת לפני/אחרי" modifier when within ±60 seconds of an anchor boundary
4. Appends a day-part suffix (בבוקר/בערב/בלילה) based on hour ranges; midday (10–17h) has no suffix

**`spec.md` is the authoritative reference** for all algorithm logic, text data (including niqqud variants), edge cases, and display behavior. Read it before modifying time logic or Hebrew text.

### Key JavaScript Components

| Function | Purpose |
|---|---|
| `getHebrewTime(date)` | Main algorithm → `{modifier, phrase, suffix}` |
| `buildPhrase(anchorHour24, anchorMinute)` | Constructs Hebrew phrase for a given anchor |
| `dayPartSuffix(hour24)` | Returns the בבוקר/בערב/בלילה suffix |
| `renderTime(modifier, phrase, suffix)` | Updates DOM with 350ms fade transition |
| `tick()` | Called every second; drives the clock |
| `getSunTimes(date, lat, lng)` | Solar declination for auto dark/light theme |
| `applyFontFamily()` | Applies selected font; disables niqqud for Fridge font |

### State & Persistence

9 `localStorage` keys persist user settings across sessions:
`hc_font`, `hc_fontVW`, `hc_digital`, `hc_split`, `hc_suffix`, `hc_niqqud`, `hc_theme`, `hc_lat`/`hc_lng`, `hc_seen`

### Settings Panel

Hidden behind a pill handle at the bottom of the screen. Revealed on hover (desktop) or tap (mobile). Controls: font family (7 options), font size (vw-based), digital clock toggle, modifier split, niqqud toggle, theme cycle (auto/day/night), demo mode (50× speed), wake lock, fullscreen, PWA install, Android widget link.

### Theming

Dark/light mode can be auto (geolocation + solar calculation), forced day, or forced night. The `body.dark` class applies the dark theme. Auto theme rechecks every 60 seconds.

## Android Widget

Located in `android/hebClockWidget/`. A Jetpack Glance home-screen widget — no launcher activity, widget-only APK.

**`spec-android.md` is the authoritative reference** for the Android widget. Read it before modifying widget logic.

Key Kotlin files:

| File | Purpose |
|---|---|
| `HebTimeAlgorithm.kt` | Kotlin port of the JS time algorithm |
| `HebClockWidget.kt` | Glance Composable widget UI |
| `HebClockWidgetReceiver.kt` | GlanceAppWidgetReceiver bridge |
| `ClockUpdateReceiver.kt` | AlarmManager scheduling for ~1-min updates |
| `WidgetPrefs.kt` | SharedPreferences helpers for widget settings |
| `WidgetConfigActivity.kt` | Settings screen (theme, niqqud, font size, etc.) |

The algorithm in `HebTimeAlgorithm.kt` is a direct port of `getHebrewTime()` from `index.html` and must stay in sync with `spec.md`.

## Widget Landing Page (`widget.html`)

Marketing/download page for the Android APK. Contains a self-contained copy of the time algorithm for its live phone mockup.
