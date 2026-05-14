# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A single-file web application that displays the current time as a natural Hebrew spoken phrase (e.g., "רבע אחרי שש בבוקר"). No build tools, no dependencies, no package manager — the entire app lives in `index.html`.

**Live demo:** mahashaa.com

## Development

**Web:** Open `index.html` directly in a browser. There is no build step, no server required, and no npm/node dependencies.

**Android:** Build the widget APK with `cd android && ./gradlew :hebClockWidget:assembleRelease` (or `assembleDebug`). Output lands in `android/hebClockWidget/build/outputs/apk/`.

**Parallel agent docs:** `AGENTS.md` (Codex) and `GEMINI.md` cover the same project for other AI tools. When making substantive changes to this file, mirror them there.

## Versioning

Bump the version with **every change**, using a minor decimal scheme: `v1.9` → `v1.91` → ... → `v1.99` → `v2.0`. Always include the version bump in the same commit as the change.

- **Web:** `<span class="version">` in `index.html`
- **Android:** `versionName` in `android/hebClockWidget/build.gradle.kts`

## Architecture

### Web App (`index.html`, ~1700 lines)

- **Lines 1–20**: `<head>` — meta, manifest, GitHub-Pages→mahashaa redirect, analytics, Google Fonts imports
- **Lines 21–707**: Inline `<style>` (theming, layout, settings panel, animations)
- **Lines 709–852**: `<body>` markup (time text, settings panel, splash card)
- **Lines 853–end**: Inline `<script>` (all app logic)

The single custom font is `fonts/fridge_Regular.ttf`.

### Hebrew Time Algorithm

The core of the app. `getHebrewTime(date)` computes:
1. Snaps current time to the nearest 5-minute anchor using a 300-second rounding window
2. Selects the appropriate Hebrew phrase from `PLAIN` / `NIQQUD` data objects
3. Adds a "קצת לפני/אחרי" modifier when within ±60 seconds of an anchor boundary
4. Appends a day-part suffix via a configurable range-based system (7 slots, each with from/until hours); returns the first enabled slot whose range covers the effective hour

**`spec.md` is the authoritative reference** for all algorithm logic, text data (including niqqud variants), edge cases, and display behavior. Read it before modifying time logic or Hebrew text.

### Key JavaScript Components

| Function | Purpose |
|---|---|
| `getHebrewTime(date)` | Fuzzy algorithm → `{modifier, phrase, suffix, forceSplit}` |
| `buildPhrase(anchorHour24, anchorMinute)` | Constructs fuzzy Hebrew phrase for a given 5-min anchor |
| `getExactTime(date)` | Exact-minute algorithm → `{modifier, phrase, suffix, forceSplit}` |
| `buildExactPhrase(hour24, minute)` | Constructs exact Hebrew phrase for any minute 0–59 |
| `isLongExactPhrase(minute)` | Returns true for "N דקות" minutes (1–4, 6–9, 11–14, 16–19) that auto-split |
| `dayPartSuffix(hour24)` | Range-based suffix lookup; returns text of first matching enabled slot, or '' |
| `refreshSuffixRows()` | Rebuilds the suffix settings UI rows from `suffixConfig` |
| `renderTime(modifier, phrase, suffix, forceSplit)` | Updates DOM with 350ms fade transition; forceSplit puts modifier on its own line regardless of user split setting |
| `tick()` | Called every second; drives the clock |
| `getSunTimes(date, lat, lng)` | Solar declination for auto dark/light theme |
| `applyFontFamily()` | Applies selected font; disables niqqud for Fridge font |

### State & Persistence

12 `localStorage` keys persist user settings across sessions:
`hc_font`, `hc_fontVW`, `hc_digital`, `hc_split`, `hc_suffix_on`, `hc_suffixes`, `hc_niqqud`, `hc_theme`, `hc_lat`/`hc_lng`, `hc_seen`, `hc_exact`

- `hc_suffix_on` — master suffix toggle (boolean string, default `true`)
- `hc_suffixes` — JSON array of `{enabled, from, until}` for each of the 7 suffix slots
- `hc_exact` — exact-minute mode (boolean string, default `false`; `true` = "על הדקה")

### Settings Panel

Opened by tapping/clicking the time text itself (added in v2.0); the pill handle at the bottom of the screen still works as a fallback. Controls: font family (7 options), font size (vw-based), digital clock toggle, modifier split, accuracy dropdown (דיוק: "בערך" fuzzy / "על הדקה" exact-minute), configurable suffix settings (master toggle + 7 slots with from/until), niqqud toggle, theme cycle (auto/day/night, default night), demo mode (50× speed), wake lock, fullscreen, PWA install, Android widget link.

### Theming

Dark/light mode can be auto (geolocation + solar calculation), forced day, or forced night. The `body.dark` class applies the dark theme. Auto theme rechecks every 60 seconds.

## Android Widget

Located in `android/hebClockWidget/`. A Jetpack Glance home-screen widget. The APK does include a minimal `MainActivity` (launcher entry), but the real product is the widget — `WidgetConfigActivity` is the screen users actually interact with (launched when adding/long-pressing the widget).

**`spec-android.md` is the authoritative reference** for the Android widget. Read it before modifying widget logic.

Key Kotlin files (under `src/main/java/com/shaik/hebclockwidget/` — the directory is `java/` even though the files are `.kt`):

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
