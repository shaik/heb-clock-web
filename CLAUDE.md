# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A single-file web application that displays the current time as a natural Hebrew spoken phrase (e.g., "רבע אחרי שש בבוקר"). No build tools, no dependencies, no package manager — the entire app lives in `index.html`.

**Live demo:** shaik.github.io/heb-clock-web

## Development

Open `index.html` directly in a browser. There is no build step, no server required, and no npm/node dependencies.

## Versioning

Bump the version in `index.html` (the `<span class="version">` element) with **every change**, using a minor decimal scheme: `v1.9` → `v1.91` → `v1.92` → ... → `v1.99` → `v2.0`. Always include the version bump in the same commit as the change.

## Architecture

Everything is in `index.html`:
- **Lines 1–17**: HTML structure and Google Fonts imports
- **Lines 18–400**: Inline CSS (theming, layout, settings panel, animations)
- **Lines 400–855**: Inline JavaScript (all app logic)

The single custom font is `fonts/fridge_Regular.ttf`.

### Hebrew Time Algorithm

The core of the app. `getHebrewTime(date)` computes:
1. Snaps current time to the nearest 5-minute anchor using a 300-second rounding window
2. Selects the appropriate Hebrew phrase from `PLAIN` / `NIQQUD` data objects
3. Adds a "קצת לפני/אחרי" modifier when within ±60 seconds of an anchor boundary
4. Appends a day-part suffix (בבוקר/בצהריים/בערב/בלילה) based on hour ranges

The `spec.md` file is the authoritative reference for all algorithm logic, text data (including niqqud variants), edge cases, and display behavior. **Read `spec.md` before modifying time logic or Hebrew text.**

### Key JavaScript Components

| Function | Purpose |
|---|---|
| `getHebrewTime(date)` | Main algorithm → `{modifier, phrase, suffix}` |
| `buildPhrase(anchorHour24, anchorMinute)` | Constructs Hebrew phrase for a given anchor |
| `renderTime(modifier, phrase, suffix)` | Updates DOM with 350ms fade transition |
| `tick()` | Called every second; drives the clock |
| `getSunTimes(date, lat, lng)` | Solar declination for auto dark/light theme |
| `applyFontFamily()` | Applies selected font; disables niqqud for Fridge font |

### State & Persistence

8 `localStorage` keys persist user settings across sessions:
`fontFamily`, `fontVW`, `digitalVisible`, `splitModifier`, `suffixVisible`, `useNiqqud`, `themeMode`, `userLat`/`userLng`

### Settings Panel

Hidden by default behind a pill handle at the bottom of the screen. Revealed on hover (desktop) or tap (mobile). Controls: font family (7 options), font size (vw-based), digital clock toggle, modifier split, niqqud toggle, theme cycle (auto/day/night), demo mode (50× speed).

### Theming

Dark/light mode can be auto (geolocation + solar calculation), forced day, or forced night. The `body.dark` class applies the dark theme. Auto theme rechecks every 60 seconds.
