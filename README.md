# Hebrew Spoken-Time Clock

A web clock that displays the current time as a natural Hebrew phrase, the way you'd say it in conversation.

**Live demo:** [mahashaa.com](https://mahashaa.com)

## Examples

| Time    | Display                    |
|---------|----------------------------|
| 3:00 AM | שלוש בבוקר                  |
| 7:15 AM | שבע ורבע בבוקר              |
| 7:17 AM | קצת אחרי שבע ורבע בבוקר     |
| 9:45 PM | רבע לעשר בערב               |
| 11:00 PM| אחת עשרה בלילה              |

## Features

- **Spoken Hebrew time** — 5-minute anchors with natural phrasing (:00 through :55)
- **Second-precision** — smoothly transitions between "קצת לפני" (a bit before), exact, and "קצת אחרי" (a bit after)
- **Configurable day-part suffixes** — 7 suffix slots (לפנות בוקר, בבוקר, בצהריים, אחרי הצהריים, לפנות ערב, בערב, בלילה), each with custom from/until times
- **Niqqud mode** — optional vowel diacritics (e.g., שֶׁבַע וְרֶבַע בַּבֹּקֶר)
- **Font family & size** — 7 font choices, size from 1.5 to 20vw
- **Demo mode** — fast-forward through time to see all phrase transitions
- **Installable PWA** — install to home screen on Android/desktop Chrome
- **Offline support** — works without a network connection once loaded
- **No dependencies** — single HTML file, runs entirely client-side

## Usage

Open `index.html` in a browser, or visit the live demo. Tap or click the time text to open the settings panel.

## Settings

Tap or click anywhere on the time text to open the settings panel:

| Control       | Description                                         |
|---------------|-----------------------------------------------------|
| גופן           | Choose from 7 font families                         |
| גודל +/−      | Adjust font size (1.5–20vw) with buttons or dropdown|
| שעון דיגיטלי  | Show or hide the digital clock                      |
| קצת — שורה           | Show "קצת לפני/אחרי" on a separate line or inline                  |
| השתמש בסיומת שעה    | Master toggle + 7 configurable suffix slots with from/until times  |
| ניקוד                | Toggle Hebrew vowel diacritics                                      |
| תצוגה                | Light / dark / auto (solar-based) theme (default: dark)             |
| מסך דלוק      | Keep the screen from going to sleep                 |
| מסך מלא       | Toggle fullscreen mode                              |
| דמו            | Fast-forward demo mode (50x speed)                  |
| התקנה / Install | Install the app to your home screen               |

All settings (except demo) are saved in localStorage.

## Tech

Single `index.html` with inline CSS and JavaScript. Uses [Heebo](https://fonts.google.com/specimen/Heebo) and other Google Fonts. PWA-ready with a service worker and web app manifest.

---

## What's new in v2.19

- **Custom domain** — now live at [mahashaa.com](https://mahashaa.com)
- **Default dark theme** — night mode is now the default for new visitors
- **Configurable suffix settings** — each of the 7 Hebrew day-part suffixes (לפנות בוקר, בבוקר, בצהריים, אחרי הצהריים, לפנות ערב, בערב, בלילה) can be independently enabled with custom from/until hours; a master toggle shows/hides all suffixes; a "ברירת מחדל" button resets to defaults

---

## What's new in v2.0

- **Tap to open settings** — tap or click anywhere on the time text to open the settings panel (no more hunting for the handle)
- **Redesigned settings panel** — clean bottom sheet with labeled rows, much easier to read and use on any screen size
- **Live preview** — the current time phrase is shown at the top of the settings panel while it's open, so you can see font, size, and niqqud changes instantly
- **Scrollable panel** — the settings panel now scrolls, so all controls are always reachable on small screens and landscape mobile
- **Font size dropdown** — pick a size directly from a dropdown (1.5–20vw) in addition to the +/− buttons; maximum size raised from 10 to 20vw for large-screen and mobile use
- **Keep screen on** — new toggle that prevents the screen from going to sleep while the clock is open (uses the Wake Lock API; supported on Android Chrome and desktop)
- **Fullscreen** — new button to go fullscreen directly from the settings panel
- **Installable app** — the clock can now be installed to your Android or desktop home screen via the Install button (appears automatically when supported)
- **Offline support** — once loaded, the app works without an internet connection
